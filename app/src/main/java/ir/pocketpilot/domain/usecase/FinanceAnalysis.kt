package ir.pocketpilot.domain.usecase

import ir.pocketpilot.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import ir.pocketpilot.domain.model.FinancialMonth
import java.util.Locale
import kotlin.math.abs

fun String.persianDigits(): String = map { if (it in '0'..'9') "۰۱۲۳۴۵۶۷۸۹"[it - '0'] else it }.joinToString("")
fun String.asciiDigits(): String = map { c -> when { c in '۰'..'۹' -> '0' + (c - '۰'); c in '٠'..'٩' -> '0' + (c - '٠'); c == '٫' -> '.'; else -> c } }.joinToString("")
fun money(amount: Long, currency: Currency): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = '٬'; decimalSeparator = '٫' }
    val format = DecimalFormat(if (currency == Currency.USD || currency == Currency.EUR) "#,##0.00" else "#,##0.##", symbols)
    return "${format.format(BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(currency.factor))).persianDigits()} ${currency.unit}"
}
fun parseAmount(text: String, currency: Currency): Long? = runCatching {
    BigDecimal(text.asciiDigits().replace("٬", "").replace(",", "").trim())
        .multiply(BigDecimal.valueOf(currency.factor)).setScale(0, RoundingMode.UNNECESSARY).longValueExact().takeIf { it in 1..1_000_000_000_000_000L }
}.getOrNull()

class FinanceAnalysis(private val calendar: FinancialCalendar) {
    fun totals(items: List<Transaction>) = Totals(items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }, items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount })
    fun monthly(items: List<Transaction>, month: FinancialMonth) = items.filter { calendar.contains(month, it.date) }
    fun spending(items: List<Transaction>): Map<String, Long> = items.filter { it.type == TransactionType.EXPENSE }.groupBy { it.categoryId }.mapValues { (_, rows) -> rows.sumOf { it.amount } }.toList().sortedByDescending { it.second }.toMap()
    fun budgetStatus(budget: Budget, items: List<Transaction>) = BudgetStatus(budget, monthly(items, budget.month).filter { it.type == TransactionType.EXPENSE && it.categoryId == budget.categoryId && it.currency == budget.currency }.sumOf { it.amount })
    fun report(data: FinanceData, month: FinancialMonth, currency: Currency): MonthlyReport {
        val items = data.transactions.filter { it.currency == currency.storage }
        val current = monthly(items, month)
        return MonthlyReport(totals(current), totals(monthly(items, month.minusMonths(1))), spending(current), current.filter { it.type == TransactionType.EXPENSE }.maxByOrNull { it.amount }, data.budgets.filter { it.month == month && it.currency == currency.storage }.map { budgetStatus(it, items) })
    }
    fun period(items: List<Transaction>, period: Period, today: LocalDate): List<Transaction> = items.filter {
        !it.date.isAfter(today) && when (period) {
            Period.WEEK -> !it.date.isBefore(today.minusDays(((today.dayOfWeek.value + 1) % 7).toLong()))
            Period.MONTH -> calendar.monthOf(it.date) == calendar.monthOf(today)
            Period.LAST_MONTH -> calendar.monthOf(it.date) == calendar.monthOf(today).minusMonths(1)
            Period.YEAR -> calendar.monthOf(it.date).year == calendar.monthOf(today).year
        }
    }
    fun insights(data: FinanceData, currency: Currency, today: LocalDate = LocalDate.now()): List<String> {
        val month = calendar.monthOf(today)
        val report = report(data, month, currency)
        val names = data.categories.associate { it.id to it.name }
        if (monthly(data.transactions.filter { it.currency == currency.storage }, month).isEmpty()) return listOf("با ثبت تراکنش‌های این ماه، تحلیل مالی شما آماده می‌شود.")
        return buildList {
            report.topCategory?.let { add("بیشترین هزینه این ماه مربوط به ${names[it] ?: it} بوده است.") }
            add("خالص پس‌انداز شما این ماه ${money(report.totals.savings, currency)} و نرخ پس‌انداز ${report.totals.savingsRate.toInt().toString().persianDigits()}٪ است.")
            report.expenseChange?.let { add("هزینه‌های شما نسبت به ماه گذشته ${abs(it).toInt().toString().persianDigits()}٪ ${if (it >= 0) "افزایش" else "کاهش"} داشته است.") }
            report.budgets.filter { it.percentage >= 80 }.forEach { add("شما تاکنون ${it.percentage.toInt().toString().persianDigits()}٪ از بودجه ${names[it.budget.categoryId]} را مصرف کرده‌اید. ${it.warning}") }
            val previous = spending(monthly(data.transactions.filter { it.currency == currency.storage }, month.minusMonths(1)))
            report.categories.forEach { (id, amount) -> previous[id]?.takeIf { it > 0 && it != amount }?.let { add("هزینه‌های ${names[id]} نسبت به ماه گذشته ${if (amount > it) "افزایش" else "کاهش"} داشته است.") } }
        }
    }
}

