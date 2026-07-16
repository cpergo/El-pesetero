package com.pesetas.data.repository

import com.pesetas.data.local.dao.BudgetDao
import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.local.dao.TransactionDao
import com.pesetas.data.local.entity.BudgetEntity
import com.pesetas.data.mapper.toDomain
import com.pesetas.domain.model.Budget
import com.pesetas.domain.model.BudgetStatus
import com.pesetas.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) : BudgetRepository {

    override fun observeBudgets(): Flow<List<Budget>> =
        budgetDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeBudgetStatuses(month: YearMonth): Flow<List<BudgetStatus>> =
        combine(
            budgetDao.observeAll(),
            categoryDao.observeAll(),
            transactionDao.observeExpenseByCategory(
                month.atDay(1).toEpochDay(),
                month.atEndOfMonth().toEpochDay(),
            ),
        ) { budgets, categories, spendingRows ->
            val categoryById = categories.associateBy { it.id }
            val spentByCategory = spendingRows.associate { it.categoryId to it.total }
            budgets.mapNotNull { budget ->
                val category = categoryById[budget.categoryId] ?: return@mapNotNull null
                BudgetStatus(
                    category = category.toDomain(),
                    monthlyLimit = budget.monthlyLimit,
                    spent = spentByCategory[budget.categoryId] ?: 0.0,
                )
            }.sortedByDescending { it.ratio }
        }

    override suspend fun setBudget(categoryId: Long, monthlyLimit: Double) {
        budgetDao.upsert(BudgetEntity(categoryId = categoryId, monthlyLimit = monthlyLimit))
    }

    override suspend fun clearBudget(categoryId: Long) {
        budgetDao.deleteByCategory(categoryId)
    }
}
