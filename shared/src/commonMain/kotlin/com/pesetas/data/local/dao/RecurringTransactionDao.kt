package com.pesetas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.pesetas.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {

    @Query("SELECT * FROM recurring_transactions ORDER BY dayOfMonth ASC, id ASC")
    fun observeAll(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions ORDER BY dayOfMonth ASC, id ASC")
    suspend fun getAll(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Upsert
    suspend fun upsert(rule: RecurringTransactionEntity): Long

    @Delete
    suspend fun delete(rule: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET lastGeneratedEpochDay = :epochDay WHERE id = :id")
    suspend fun updateLastGenerated(id: Long, epochDay: Long)
}
