package ir.pocketpilot.data.mapper
import ir.pocketpilot.data.local.*
import ir.pocketpilot.domain.model.*
import java.time.LocalDate
fun CategoryEntity.domain() = Category(id, name, TransactionType.valueOf(type), icon)
fun TransactionEntity.domain() = Transaction(id, amount, categoryId, TransactionType.valueOf(type), LocalDate.ofEpochDay(date), description, recurring, currency, sourceId)
fun Transaction.entity() = TransactionEntity(id, amount, categoryId, type.name, date.toEpochDay(), description, recurring, currency, sourceId)
fun BudgetEntity.domain() = Budget(id, categoryId, limit, FinancialMonth.parse(month), currency)
fun Budget.entity() = BudgetEntity(id, categoryId, limit, month.toString(), currency)
