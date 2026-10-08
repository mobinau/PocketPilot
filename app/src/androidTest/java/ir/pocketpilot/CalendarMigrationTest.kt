package ir.pocketpilot

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import ir.pocketpilot.data.local.*
import ir.pocketpilot.domain.model.*
import org.json.JSONObject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.util.TimeZone

class CalendarMigrationTest {
    @Test fun androidIcuHandlesNowruzLeapEsfandAndTimeZones() {
        val calendar = AndroidFinancialCalendar
        val original = TimeZone.getDefault()
        try {
            listOf("UTC", "Asia/Tehran", "America/Los_Angeles").forEach {
                TimeZone.setDefault(TimeZone.getTimeZone(it))
                assertEquals(FinancialMonth(1405,1), calendar.monthOf(LocalDate.of(2026,3,21)))
                assertEquals(FinancialMonth(1404,12), calendar.monthOf(LocalDate.of(2026,3,20)))
                assertEquals(LocalDate.of(2026,4,20), calendar.atDay(FinancialMonth(1405,1),31))
                assertEquals(30, calendar.lengthOfMonth(FinancialMonth(1403,12)))
                assertEquals(29, calendar.lengthOfMonth(FinancialMonth(1404,12)))
            }
        } finally { TimeZone.setDefault(original) }
    }
    @Test fun migrationPreservesExistingBudgetAndTransaction() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "calendar-migration-${System.nanoTime()}.db"
        val schema = JSONObject(instrumentation.context.assets.open("ir.pocketpilot.data.local.PocketDatabase/1.json").bufferedReader().use { it.readText() }).getJSONObject("database")
        context.openOrCreateDatabase(name, 0, null).use { legacy ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                legacy.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices")
                if (indices != null) for (j in 0 until indices.length()) legacy.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) legacy.execSQL(setup.getString(i))
            legacy.execSQL("INSERT INTO categories VALUES ('food', 'خوراک و رستوران', 'EXPENSE', 'food')")
            legacy.execSQL("INSERT INTO budgets VALUES (7, 'food', 100000, '2026-10', 'IRR')")
            legacy.execSQL("INSERT INTO transactions VALUES (9, 25000, 'food', 'EXPENSE', ?, 'اطلاعات قبلی', 0, 'IRR', NULL)", arrayOf<Any>(LocalDate.of(2026,10,5).toEpochDay()))
            legacy.version = 1
        }
        val db = Room.databaseBuilder(context, PocketDatabase::class.java, name).addMigrations(CalendarMigrations.MIGRATION_1_2).build()
        try {
            val dao = db.financeDao()
            val transaction = dao.allTransactions().single()
            assertEquals(9L, transaction.id); assertEquals(25000L, transaction.amount)
            assertEquals("2026-10", dao.metadata("legacy_budget_period_7"))
            assertEquals(7L, dao.budgetId("food", "JALALI-1405-07", "IRR"))
            assertEquals(LocalDate.of(2026,10,5).toEpochDay(), transaction.date)
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
