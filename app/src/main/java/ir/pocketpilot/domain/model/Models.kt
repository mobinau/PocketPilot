package ir.pocketpilot.domain.model

import java.time.LocalDate

enum class TransactionType(val label: String) { EXPENSE("هزینه"), INCOME("درآمد") }
enum class Currency(val label: String, val unit: String, val storage: String, val factor: Long) {
    IRR("ریال", "ریال", "IRR", 1), TOMAN("تومان", "تومان", "IRR", 10),
    USD("دلار", "دلار", "USD", 100), EUR("یورو", "یورو", "EUR", 100)
}
enum class ThemeMode(val label: String) { SYSTEM("همگام با سیستم"), LIGHT("حالت روشن"), DARK("حالت تاریک") }
data class Category(val id: String, val name: String, val type: TransactionType, val icon: String)
data class Transaction(
    val id: Long = 0, val amount: Long, val categoryId: String, val type: TransactionType,
    val date: LocalDate, val description: String, val recurring: Boolean = false,
    val currency: String = "IRR", val sourceId: Long? = null
)
data class Budget(val id: Long = 0, val categoryId: String, val limit: Long, val month: FinancialMonth, val currency: String = "IRR")
data class Preferences(val onboarding: Boolean = false, val theme: ThemeMode = ThemeMode.SYSTEM, val currency: Currency = Currency.IRR, val provider: String = "", val ready: Boolean = false)
data class FinanceData(val transactions: List<Transaction> = emptyList(), val categories: List<Category> = emptyList(), val budgets: List<Budget> = emptyList())
data class Totals(val income: Long, val expense: Long) {
    val balance: Long get() = income - expense
    val savings: Long get() = balance
    val savingsRate: Double get() = if (income == 0L) 0.0 else savings.toDouble() / income * 100
}
data class BudgetStatus(val budget: Budget, val spent: Long) {
    val remaining get() = budget.limit - spent
    val percentage get() = if (budget.limit <= 0) 0.0 else spent.toDouble() / budget.limit * 100
    val warning get() = when { percentage >= 100 -> "بودجه این دسته تمام شده است."; percentage >= 80 -> "هشدار: بیشتر بودجه این دسته مصرف شده است."; else -> null }
}
data class MonthlyReport(val totals: Totals, val previous: Totals, val categories: Map<String, Long>, val largest: Transaction?, val budgets: List<BudgetStatus>) {
    val topCategory get() = categories.maxByOrNull { it.value }?.key
    val expenseChange: Double? get() = if (previous.expense == 0L) null else (totals.expense - previous.expense).toDouble() / previous.expense * 100
}
enum class Period(val label: String) { WEEK("این هفته"), MONTH("این ماه"), LAST_MONTH("ماه گذشته"), YEAR("امسال") }
