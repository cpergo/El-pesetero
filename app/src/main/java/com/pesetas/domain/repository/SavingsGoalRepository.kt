package com.pesetas.domain.repository

import com.pesetas.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun observeGoals(): Flow<List<SavingsGoal>>
    suspend fun getGoal(id: Long): SavingsGoal?
    suspend fun upsert(goal: SavingsGoal): Long
    suspend fun delete(goal: SavingsGoal)
    suspend fun contribute(goalId: Long, amount: Double)
}
