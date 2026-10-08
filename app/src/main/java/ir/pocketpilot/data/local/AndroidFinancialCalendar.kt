package ir.pocketpilot.data.local

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import ir.pocketpilot.domain.model.*
import java.time.LocalDate

/** UTC is only an encoding of LocalDate fields. No device-zone conversion of stored dates. */
object AndroidFinancialCalendar : FinancialCalendar {
    private fun calendar() = Calendar.getInstance(TimeZone.getTimeZone("UTC"), ULocale("fa_IR@calendar=persian")).apply { clear(); isLenient = false }
    private fun calendar(date: LocalDate) = calendar().apply { timeInMillis = date.toEpochDay() * 86_400_000L }
    override fun monthOf(date: LocalDate): FinancialMonth = calendar(date).let { FinancialMonth(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1) }
    override fun dayOfMonth(date: LocalDate) = calendar(date).get(Calendar.DAY_OF_MONTH)
    override fun atDay(month: FinancialMonth, day: Int): LocalDate {
        val value = calendar().apply { set(month.year, month.month - 1, day) }
        return LocalDate.ofEpochDay(Math.floorDiv(value.timeInMillis, 86_400_000L))
    }
}
