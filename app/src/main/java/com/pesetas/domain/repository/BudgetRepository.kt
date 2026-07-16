package com.pesetas.domain.repository

import com.pesetas.domain.model.Budget
import com.pesetas.domain.model.BudgetStatus
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    fun observeBudgets(): Flow<List<Budget>>
    fun observeBudgetStatuses(month: YearMonth): Flow<List<BudgetStatus>>
    suspend fun setBudget(categoryId: Long, monthlyLimit: Double)
    suspend fun clearBudget(categoryId: Long)
}
