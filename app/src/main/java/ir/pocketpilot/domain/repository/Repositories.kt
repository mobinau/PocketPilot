package ir.pocketpilot.domain.repository

import ir.pocketpilot.domain.model.*
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    val data: Flow<FinanceData>
    suspend fun initialize()
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long)
    suspend fun saveBudget(budget: Budget)
    suspend fun deleteBudget(id: Long)
    suspend fun reloadDemo()
    suspend fun clear()
}
interface AiAssistantRepository {
    suspend fun answer(question: String, data: FinanceData, currency: Currency): String
}
