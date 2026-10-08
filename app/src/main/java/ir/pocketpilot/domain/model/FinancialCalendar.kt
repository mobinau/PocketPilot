package ir.pocketpilot.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A financial period is a Persian year/month, never a Gregorian YearMonth. */
data class FinancialMonth(val year: Int, val month: Int) : Comparable<FinancialMonth> {
    init { require(year in 1..9999 && month in 1..12) }
    fun plusMonths(count: Int): FinancialMonth {
        val index = year * 12 + month - 1 + count
        return FinancialMonth(Math.floorDiv(index, 12), Math.floorMod(index, 12) + 1)
    }
    fun minusMonths(count: Int) = plusMonths(-count)
    override fun compareTo(other: FinancialMonth) = compareValuesBy(this, other, { it.year }, { it.month })
    override fun toString() = "JALALI-$year-${month.toString().padStart(2, '0')}"
    companion object {
        fun parse(value: String): FinancialMonth {
            require(value.startsWith("JALALI-"))
            val parts = value.removePrefix("JALALI-").split('-')
            require(parts.size == 2)
            return FinancialMonth(parts[0].toInt(), parts[1].toInt())
        }
    }
}
interface FinancialCalendar {
    fun monthOf(date: LocalDate): FinancialMonth
    fun dayOfMonth(date: LocalDate): Int
    fun atDay(month: FinancialMonth, day: Int): LocalDate
    fun lengthOfMonth(month: FinancialMonth): Int = ChronoUnit.DAYS.between(atDay(month, 1), atDay(month.plusMonths(1), 1)).toInt()
    fun currentMonth(today: LocalDate = LocalDate.now()) = monthOf(today)
    fun contains(month: FinancialMonth, date: LocalDate) = date >= atDay(month, 1) && date < atDay(month.plusMonths(1), 1)
}
