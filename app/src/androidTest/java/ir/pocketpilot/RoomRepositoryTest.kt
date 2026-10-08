package ir.pocketpilot

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import ir.pocketpilot.data.local.PocketDatabase
import ir.pocketpilot.data.repository.RoomFinanceRepository
import ir.pocketpilot.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import ir.pocketpilot.data.local.AndroidFinancialCalendar

class RoomRepositoryTest {
    @Test fun recurringOccurrencesAndBudgetUpsertsAreIdempotent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, PocketDatabase::class.java).build()
        val repo = RoomFinanceRepository(db)
        try {
            repo.initialize(); repo.clear()
            val today = LocalDate.now()
            repo.saveTransaction(Transaction(amount = 5000, categoryId = "salary", type = TransactionType.INCOME, date = AndroidFinancialCalendar.atDay(AndroidFinancialCalendar.currentMonth().minusMonths(2), 1), description = "حقوق تکراری", recurring = true))
            assertEquals(3, repo.data.first().transactions.size)
            repo.initialize(); assertEquals(3, repo.data.first().transactions.size)
            val template = repo.data.first().transactions.first { it.recurring }
            repo.deleteTransaction(template.id); assertTrue(repo.data.first().transactions.isEmpty())
            repo.saveBudget(Budget(categoryId = "food", limit = 1000, month = AndroidFinancialCalendar.currentMonth()))
            repo.saveBudget(Budget(categoryId = "food", limit = 2000, month = AndroidFinancialCalendar.currentMonth()))
            assertEquals(1, repo.data.first().budgets.size); assertEquals(2000L, repo.data.first().budgets.single().limit)
        } finally { db.close() }
    }
    @Test fun seedCrudAndClearArePersistentAndIdempotent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "repository-test-${System.nanoTime()}.db"
        var db = Room.databaseBuilder(context, PocketDatabase::class.java, name).build()
        var repo = RoomFinanceRepository(db)
        try {
            repo.initialize(); val seeded = repo.data.first().transactions.size
            assertTrue(seeded > 0); repo.initialize(); assertEquals(seeded, repo.data.first().transactions.size)
            repo.saveTransaction(Transaction(amount = 12345, categoryId = "food", type = TransactionType.EXPENSE, date = LocalDate.now(), description = "تست ذخیره"))
            val saved = repo.data.first().transactions.first { it.description == "تست ذخیره" }
            repo.saveTransaction(saved.copy(amount = 999)); assertEquals(999L, repo.data.first().transactions.first { it.id == saved.id }.amount)
            db.close(); db = Room.databaseBuilder(context, PocketDatabase::class.java, name).build(); repo = RoomFinanceRepository(db)
            assertTrue(repo.data.first().transactions.any { it.id == saved.id && it.amount == 999L })
            repo.deleteTransaction(saved.id); assertFalse(repo.data.first().transactions.any { it.id == saved.id })
            repo.clear(); repo.initialize(); assertTrue(repo.data.first().transactions.isEmpty()); assertTrue(repo.data.first().budgets.isEmpty())
        } finally { db.close(); context.deleteDatabase(name) }
    }
}

