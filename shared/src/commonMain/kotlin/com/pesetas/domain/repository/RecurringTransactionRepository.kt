package com.pesetas.domain.repository

import com.pesetas.domain.model.GeneratedRecurring
import com.pesetas.domain.model.RecurringTransaction
import com.pesetas.domain.model.RecurringTransactionDetails
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    fun observeRules(): Flow<List<RecurringTransactionDetails>>
    suspend fun getRule(id: Long): RecurringTransaction?
    suspend fun upsert(rule: RecurringTransaction)
    suspend fun delete(rule: RecurringTransaction)
    suspend fun generatePendingOnce(): List<GeneratedRecurring>
    suspend fun undoGenerated(transactionId: Long)
}
