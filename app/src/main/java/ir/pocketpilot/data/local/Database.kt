package ir.pocketpilot.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "categories")
data class CategoryEntity(@PrimaryKey val id: String, val name: String, val type: String, val icon: String)
@Entity(tableName = "transactions", foreignKeys = [ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.RESTRICT)], indices = [Index("categoryId"), Index(value = ["sourceId", "date"], unique = true)])
data class TransactionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val amount: Long, val categoryId: String, val type: String, val date: Long, val description: String, val recurring: Boolean, val currency: String, val sourceId: Long? = null)
@Entity(tableName = "budgets", foreignKeys = [ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.RESTRICT)], indices = [Index("categoryId"), Index(value = ["categoryId", "month", "currency"], unique = true)])
data class BudgetEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val categoryId: String, val limit: Long, val month: String, val currency: String)
@Entity(tableName = "metadata")
data class Metadata(@PrimaryKey val key: String, val value: String)

@Dao
interface FinanceDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC") fun transactions(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM categories ORDER BY rowid") fun categories(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM budgets ORDER BY id") fun budgets(): Flow<List<BudgetEntity>>
    @Query("SELECT * FROM transactions") suspend fun allTransactions(): List<TransactionEntity>
    @Query("SELECT value FROM metadata WHERE `key` = :key") suspend fun metadata(key: String): String?
    @Upsert suspend fun putMetadata(value: Metadata)
    @Upsert suspend fun putCategories(rows: List<CategoryEntity>)
    @Upsert suspend fun putTransaction(row: TransactionEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertOccurrence(row: TransactionEntity)
    @Upsert suspend fun putBudget(row: BudgetEntity)
    @Query("SELECT id FROM budgets WHERE categoryId=:category AND month=:month AND currency=:currency LIMIT 1") suspend fun budgetId(category: String, month: String, currency: String): Long?
    @Query("DELETE FROM transactions WHERE id=:id OR sourceId=:id") suspend fun deleteTransaction(id: Long)
    @Query("DELETE FROM budgets WHERE id=:id") suspend fun deleteBudget(id: Long)
    @Query("DELETE FROM transactions") suspend fun clearTransactions()
    @Query("DELETE FROM budgets") suspend fun clearBudgets()
}
@Database(entities = [CategoryEntity::class, TransactionEntity::class, BudgetEntity::class, Metadata::class], version = 2, exportSchema = true)
abstract class PocketDatabase : RoomDatabase() { abstract fun financeDao(): FinanceDao }
