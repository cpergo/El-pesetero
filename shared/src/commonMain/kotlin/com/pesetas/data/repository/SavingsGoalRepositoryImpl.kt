package com.pesetas.data.repository

import com.pesetas.data.local.dao.SavingsGoalDao
import com.pesetas.data.mapper.toDomain
import com.pesetas.data.mapper.toEntity
import com.pesetas.domain.model.SavingsGoal
import com.pesetas.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingsGoalRepositoryImpl(
    private val savingsGoalDao: SavingsGoalDao,
) : SavingsGoalRepository {

    override fun observeGoals(): Flow<List<SavingsGoal>> =
        savingsGoalDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getGoal(id: Long): SavingsGoal? =
        savingsGoalDao.getById(id)?.toDomain()

    override suspend fun upsert(goal: SavingsGoal): Long =
        savingsGoalDao.upsert(goal.toEntity())

    override suspend fun delete(goal: SavingsGoal) =
        savingsGoalDao.delete(goal.toEntity())

    override suspend fun contribute(goalId: Long, amount: Double) =
        savingsGoalDao.addContribution(goalId, amount)
}
