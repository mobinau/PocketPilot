package ir.pocketpilot

import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.usecase.FinanceAnalysis
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.util.TimeZone

class FinancialCalendarTest {
    private val calendar = IcuFinancialCalendar
    private val analysis = FinanceAnalysis(calendar)
    private fun row(date: String, amount: Long = 100) = Transaction(amount = amount, categoryId = "food", type = TransactionType.EXPENSE, date = LocalDate.parse(date), description = "مرز ماه")
    @Test fun gregorianDatesMapToKnownPersianMonths() {
        assertEquals(FinancialMonth(1405, 1), calendar.monthOf(LocalDate.of(2026,3,21)))
        assertEquals(FinancialMonth(1405, 1), calendar.monthOf(LocalDate.of(2026,4,20)))
        assertEquals(FinancialMonth(1405, 2), calendar.monthOf(LocalDate.of(2026,4,21)))
        assertEquals(FinancialMonth(1405, 7), calendar.monthOf(LocalDate.of(2026,10,5)))
    }
    @Test fun currentMonthAndPreviousMonthUsePersianYear() {
        val month = calendar.currentMonth(LocalDate.of(2026,3,21))
        assertEquals(FinancialMonth(1405,1), month)
        assertEquals(FinancialMonth(1404,12), month.minusMonths(1))
        assertEquals(month, month.minusMonths(1).plusMonths(1))
    }
    @Test fun periodIdentifiersRoundTripWithoutGregorianAmbiguity() { assertEquals("JALALI-1405-01", FinancialMonth(1405,1).toString()); assertEquals(FinancialMonth(1405,1), FinancialMonth.parse("JALALI-1405-01")) }
    @Test fun monthLengthsIncludeLeapEsfand() {
        assertEquals(31, calendar.lengthOfMonth(FinancialMonth(1405,1)))
        assertEquals(30, calendar.lengthOfMonth(FinancialMonth(1405,7)))
        assertEquals(30, calendar.lengthOfMonth(FinancialMonth(1403,12)))
        assertEquals(29, calendar.lengthOfMonth(FinancialMonth(1404,12)))
    }
    @Test fun farvardinSpansMarchAndAprilButExcludesNeighbours() {
        val rows = listOf(row("2026-03-20"), row("2026-03-21"), row("2026-04-20"), row("2026-04-21"))
        assertEquals(listOf(LocalDate.parse("2026-03-21"), LocalDate.parse("2026-04-20")), analysis.monthly(rows, FinancialMonth(1405,1)).map { it.date })
    }
    @Test fun monthlyComparisonAndBudgetUseSameBoundaries() {
        val month = FinancialMonth(1405,1)
        val rows = listOf(row("2026-03-20", 400), row("2026-03-21", 300), row("2026-04-20", 500), row("2026-04-21", 999))
        val report = analysis.report(FinanceData(rows, budgets = listOf(Budget(categoryId = "food", limit = 1000, month = month))), month, Currency.IRR)
        assertEquals(800L, report.totals.expense); assertEquals(400L, report.previous.expense)
        assertEquals(100.0, report.expenseChange!!, .001)
        assertEquals(80.0, report.budgets.single().percentage, .001)
    }
    @Test fun filtersUseNowruzYearInsteadOfJanuary() {
        val rows = listOf(row("2026-03-20"), row("2026-03-21"), row("2026-04-05"))
        val today = LocalDate.parse("2026-04-05")
        assertEquals(2, analysis.period(rows, Period.MONTH, today).size)
        assertEquals(1, analysis.period(rows, Period.LAST_MONTH, today).size)
        assertEquals(2, analysis.period(rows, Period.YEAR, today).size)
    }
    @Test fun localDateConversionDoesNotDependOnDeviceZone() {
        val original = TimeZone.getDefault()
        try { listOf("UTC", "Asia/Tehran", "America/Los_Angeles", "Pacific/Kiritimati").forEach {
            TimeZone.setDefault(TimeZone.getTimeZone(it))
            assertEquals(FinancialMonth(1405,1), calendar.monthOf(LocalDate.parse("2026-03-21")))
            assertEquals(LocalDate.parse("2026-03-21"), calendar.atDay(FinancialMonth(1405,1),1))
        } } finally { TimeZone.setDefault(original) }
    }
}
