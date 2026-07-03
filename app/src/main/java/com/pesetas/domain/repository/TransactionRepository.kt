package com.pesetas.domain.repository

import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.MonthlyTotals
import com.pesetas.domain.model.Transaction
import com.pesetas.domain.model.TransactionDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface TransactionRepository {
    fun observeTransactions(): Flow<List<TransactionDetails>>
    fun observeTransactions(month: YearMonth): Flow<List<TransactionDetails>>
    fun observeMonthlyTotals(month: YearMonth): Flow<MonthlyTotals>
    fun observeExpenseByCategory(month: YearMonth): Flow<List<CategorySpending>>
    fun observeMonthlyTotalsRange(from: YearMonth, to: YearMonth): Flow<List<MonthlyTotals>>
    fun observeCategoryEvolution(categoryId: Long, from: LocalDate, to: LocalDate): Flow<List<MonthlyTotals>>
    suspend fun getTransaction(id: Long): Transaction?
    suspend fun upsert(transaction: Transaction): Long
    suspend fun delete(transaction: Transaction)
}
