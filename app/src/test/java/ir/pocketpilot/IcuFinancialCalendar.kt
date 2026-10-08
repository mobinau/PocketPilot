package ir.pocketpilot

import com.ibm.icu.util.Calendar
import com.ibm.icu.util.TimeZone
import com.ibm.icu.util.ULocale
import ir.pocketpilot.domain.model.*
import java.time.LocalDate

/** JVM ICU adapter for the same calendar contract used by the Android adapter. */
object IcuFinancialCalendar : FinancialCalendar {
    private fun calendar() = Calendar.getInstance(TimeZone.getTimeZone("UTC"), ULocale("fa_IR@calendar=persian")).apply { clear(); isLenient = false }
    private fun calendar(date: LocalDate) = calendar().apply { timeInMillis = date.toEpochDay() * 86_400_000L }
    override fun monthOf(date: LocalDate) = calendar(date).let { FinancialMonth(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1) }
    override fun dayOfMonth(date: LocalDate) = calendar(date).get(Calendar.DAY_OF_MONTH)
    override fun atDay(month: FinancialMonth, day: Int): LocalDate = calendar().apply { set(month.year, month.month - 1, day) }.let { LocalDate.ofEpochDay(Math.floorDiv(it.timeInMillis, 86_400_000L)) }
}
