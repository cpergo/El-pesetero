package com.pesetas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.pesetas.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE dateEpochDay BETWEEN :from AND :to " +
            "ORDER BY dateEpochDay DESC, id DESC",
    )
    fun observeInRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query(
        "SELECT categoryId AS categoryId, SUM(amount) AS total FROM transactions " +
            "WHERE type = 'EXPENSE' AND categoryId IS NOT NULL AND dateEpochDay BETWEEN :from AND :to " +
            "GROUP BY categoryId",
    )
    fun observeExpenseByCategory(from: Long, to: Long): Flow<List<CategorySpendingRow>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS income,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS expense
        FROM transactions
        WHERE dateEpochDay BETWEEN :from AND :to
        """,
    )
    fun observeTotals(from: Long, to: Long): Flow<TotalsRow>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Upsert
    suspend fun upsert(transaction: TransactionEntity): Long

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}
