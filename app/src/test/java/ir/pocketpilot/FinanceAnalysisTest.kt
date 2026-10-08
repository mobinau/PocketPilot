package ir.pocketpilot

import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate


class FinanceAnalysisTest {
    private val calendar = IcuFinancialCalendar
    private val analysis = FinanceAnalysis(calendar)
    private val month = FinancialMonth(1405, 7)
    private fun row(amount: Long, type: TransactionType = TransactionType.EXPENSE, category: String = "food", date: LocalDate = calendar.atDay(month, 5), currency: String = "IRR") = Transaction(amount = amount, categoryId = category, type = type, date = date, description = "آزمایش", currency = currency)
    @Test fun balanceSubtractsExpenses() { assertEquals(650L, analysis.totals(listOf(row(1000, TransactionType.INCOME), row(350))).balance) }
    @Test fun incomeOnlyIncludesIncome() { assertEquals(1300L, analysis.totals(listOf(row(1000, TransactionType.INCOME), row(300, TransactionType.INCOME), row(350))).income) }
    @Test fun expenseOnlyIncludesExpenses() { assertEquals(550L, analysis.totals(listOf(row(1000, TransactionType.INCOME), row(350), row(200))).expense) }
    @Test fun savingsCanBeNegative() { val totals = analysis.totals(listOf(row(100, TransactionType.INCOME), row(350))); assertEquals(-250L, totals.savings); assertEquals(-250.0, totals.savingsRate, .001) }
    @Test fun zeroIncomeHasFiniteRate() { assertEquals(0.0, analysis.totals(listOf(row(350))).savingsRate, .001) }
    @Test fun savingsRateUsesIncome() { assertEquals(24.0, analysis.totals(listOf(row(1000, TransactionType.INCOME), row(760))).savingsRate, .001) }
    @Test fun budgetTracksCorrectMonthCategoryAndCurrency() {
        val b = Budget(categoryId = "food", limit = 1000, month = month)
        val status = analysis.budgetStatus(b, listOf(row(820), row(300, category = "housing"), row(200, date = calendar.atDay(month.minusMonths(1), 5)), row(500, currency = "USD"), row(100, TransactionType.INCOME)))
        assertEquals(820L, status.spent); assertEquals(180L, status.remaining); assertEquals(82.0, status.percentage, .001); assertNotNull(status.warning)
    }
    @Test fun budgetThresholds() {
        val b = Budget(categoryId = "food", limit = 1000, month = month)
        assertNull(analysis.budgetStatus(b, listOf(row(799))).warning)
        assertEquals("هشدار: بیشتر بودجه این دسته مصرف شده است.", analysis.budgetStatus(b, listOf(row(800))).warning)
        assertEquals("بودجه این دسته تمام شده است.", analysis.budgetStatus(b, listOf(row(1000))).warning)
        assertEquals(-200L, analysis.budgetStatus(b, listOf(row(1200))).remaining)
    }
    @Test fun reportComparesMonthsAndExcludesOtherCurrency() {
        val data = FinanceData(transactions = listOf(row(2000, TransactionType.INCOME), row(700), row(200, category = "housing"), row(500, date = calendar.atDay(month.minusMonths(1), 5)), row(90000, currency = "EUR")))
        val report = analysis.report(data, month, Currency.IRR)
        assertEquals(2000L, report.totals.income); assertEquals(900L, report.totals.expense); assertEquals(1100L, report.totals.savings); assertEquals(80.0, report.expenseChange!!, .001); assertEquals("food", report.topCategory); assertEquals(700L, report.largest!!.amount)
    }
    @Test fun reportHandlesYearBoundary() { assertEquals(50L, analysis.report(FinanceData(transactions = listOf(row(50, date = LocalDate.of(2026,3,20)))), FinancialMonth(1405,1), Currency.IRR).previous.expense) }
    @Test fun reportWithoutPreviousExpensesDoesNotDivideByZero() { assertNull(analysis.report(FinanceData(transactions = listOf(row(200))), month, Currency.IRR).expenseChange) }
    @Test fun categorySpendingAggregatesExpenses() { assertEquals(mapOf("food" to 400L, "housing" to 300L), analysis.spending(listOf(row(150), row(250), row(300, category = "housing"), row(400, TransactionType.INCOME)))) }
    @Test fun smartInsightsReflectActualData() {
        val data = FinanceData(listOf(row(1000, TransactionType.INCOME), row(820), row(400, date = calendar.atDay(month.minusMonths(1), 1))), listOf(Category("food", "خوراک و رستوران", TransactionType.EXPENSE, "food")), listOf(Budget(categoryId = "food", limit = 1000, month = month)))
        val text = analysis.insights(data, Currency.IRR, calendar.atDay(month, 5)).joinToString()
        assertTrue(text.contains("خوراک و رستوران")); assertTrue(text.contains("۸۲٪")); assertTrue(text.contains("افزایش")); assertTrue(text.contains("۱۸٪"))
    }
    @Test fun emptyInsightsInviteDataEntry() { assertTrue(analysis.insights(FinanceData(), Currency.IRR, calendar.atDay(month, 1)).single().contains("ثبت تراکنش")) }
    @Test fun persianAndArabicDigitsParse() { assertEquals(12500000L, parseAmount("۱۲٬۵۰۰٬۰۰۰", Currency.IRR)); assertEquals(1230L, parseAmount("١٢٣", Currency.TOMAN)); assertEquals(1234L, parseAmount("۱۲٫۳۴", Currency.USD)) }
    @Test fun invalidAmountsAreRejected() { listOf("", "0", "-5", "abc", "99999999999999999999", "1.234").forEach { assertNull(parseAmount(it, Currency.USD)) } }
    @Test fun rialAndTomanFormatting() { assertEquals("۱۲٬۵۰۰٬۰۰۰ ریال", money(12500000, Currency.IRR)); assertEquals("۱٬۲۵۰٬۰۰۰ تومان", money(12500000, Currency.TOMAN)); assertEquals("۱۲٫۳۴ دلار", money(1234, Currency.USD)) }
    @Test fun periodStartsSaturdayAndExcludesFuture() { val today = LocalDate.of(2026,10,5); val rows = listOf(row(1, date = today.minusDays(2)), row(2, date = today.minusDays(3)), row(3, date = today.plusDays(1))); assertEquals(listOf(1L), analysis.period(rows, Period.WEEK, today).map { it.amount }) }
}

