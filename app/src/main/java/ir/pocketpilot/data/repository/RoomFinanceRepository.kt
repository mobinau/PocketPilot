package ir.pocketpilot.data.repository

import androidx.room.withTransaction
import ir.pocketpilot.data.local.*
import ir.pocketpilot.data.mapper.*
import ir.pocketpilot.domain.model.*
import ir.pocketpilot.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class RoomFinanceRepository(private val db: PocketDatabase, private val calendar: FinancialCalendar = AndroidFinancialCalendar) : FinanceRepository {
    private val dao = db.financeDao()
    override val data = combine(dao.transactions(), dao.categories(), dao.budgets()) { t, c, b -> FinanceData(t.map { it.domain() }, c.map { it.domain() }, b.map { it.domain() }) }
    override suspend fun initialize() {
        db.withTransaction {
            dao.putCategories(DemoData.categories.map { CategoryEntity(it.id, it.name, it.type.name, it.icon) })
            if (dao.metadata("initialized") == null) { seed(); dao.putMetadata(Metadata("initialized", "true")) }
            materializeRecurring()
        }
    }
    private suspend fun seed() {
        DemoData.transactions(calendar = calendar).forEach { dao.putTransaction(it.entity()) }
        DemoData.budgets(calendar = calendar).forEach { dao.putBudget(it.entity()) }
    }
    private suspend fun materializeRecurring() {
        val today = LocalDate.now()
        val rows = dao.allTransactions()
        rows.filter { it.recurring && it.sourceId == null }.forEach { template ->
            val original = LocalDate.ofEpochDay(template.date)
            var month = calendar.monthOf(original).plusMonths(1)
            while (month <= calendar.monthOf(today)) {
                val date = calendar.atDay(month, calendar.dayOfMonth(original).coerceAtMost(calendar.lengthOfMonth(month)))
                // Existing occurrences from the Gregorian release already cover their Persian month.
                val covered = rows.any { it.sourceId == template.id && calendar.contains(month, LocalDate.ofEpochDay(it.date)) }
                if (!date.isAfter(today) && !covered) dao.insertOccurrence(template.copy(id = 0, date = date.toEpochDay(), sourceId = template.id, recurring = false))
                month = month.plusMonths(1)
            }
        }
    }
    override suspend fun saveTransaction(transaction: Transaction) {
        require(transaction.amount > 0 && transaction.amount <= 1_000_000_000_000_000L)
        db.withTransaction { dao.putTransaction(transaction.entity()); materializeRecurring() }
    }
    override suspend fun deleteTransaction(id: Long) = db.withTransaction { dao.deleteTransaction(id) }
    override suspend fun saveBudget(budget: Budget) {
        require(budget.limit > 0)
        db.withTransaction {
            val existing = dao.budgetId(budget.categoryId, budget.month.toString(), budget.currency)
            if (budget.id != 0L && existing != null && existing != budget.id) dao.deleteBudget(budget.id)
            dao.putBudget(budget.copy(id = existing ?: budget.id).entity())
        }
    }
    override suspend fun deleteBudget(id: Long) = dao.deleteBudget(id)
    override suspend fun reloadDemo() = db.withTransaction { dao.clearTransactions(); dao.clearBudgets(); seed(); dao.putMetadata(Metadata("initialized", "true")) }
    override suspend fun clear() = db.withTransaction { dao.clearTransactions(); dao.clearBudgets(); dao.putMetadata(Metadata("initialized", "true")) }
}
