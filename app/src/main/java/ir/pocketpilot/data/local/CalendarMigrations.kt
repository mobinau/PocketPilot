package ir.pocketpilot.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.YearMonth

object CalendarMigrations {
    /** Keep IDs/amounts/categories; retain the original Gregorian label as migration metadata. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val rows = mutableListOf<Pair<Long, String>>()
            db.query("SELECT id, month FROM budgets").use { cursor -> while (cursor.moveToNext()) rows.add(cursor.getLong(0) to cursor.getString(1)) }
            rows.filterNot { it.second.startsWith("JALALI-") }.forEach { (id, legacy) ->
                // The middle of the legacy Gregorian month identifies its dominant Persian month.
                val month = AndroidFinancialCalendar.monthOf(YearMonth.parse(legacy).atDay(15))
                db.execSQL("INSERT OR REPLACE INTO metadata (`key`, value) VALUES (?, ?)", arrayOf<Any>("legacy_budget_period_$id", legacy))
                db.execSQL("UPDATE budgets SET month = ? WHERE id = ?", arrayOf<Any>(month.toString(), id))
            }
        }
    }
}
