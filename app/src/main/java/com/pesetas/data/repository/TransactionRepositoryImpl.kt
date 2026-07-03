package com.pesetas.data.repository

import com.pesetas.data.local.dao.AccountDao
import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.local.dao.TransactionDao
import com.pesetas.data.mapper.toDomain
import com.pesetas.data.mapper.toEntity
import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.model.MonthlyTotals
import com.pesetas.domain.model.Transaction
import com.pesetas.domain.model.TransactionDetails
import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<TransactionDetails>> =
        combine(
            transactionDao.observeAll(),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { transactions, categories, accounts ->
            val categoryById = categories.associateBy { it.id }
            val accountById = accounts.associateBy { it.id }
            transactions.mapNotNull { entity ->
                val account = accountById[entity.accountId] ?: return@mapNotNull null
                TransactionDetails(
                    transaction = entity.toDomain(),
                    category = entity.categoryId?.let { categoryById[it] }?.toDomain(),
                    account = account.toDomain(),
                    transferAccount = entity.transferAccountId?.let { accountById[it] }?.toDomain(),
                )
            }
        }

    override fun observeTransactions(month: YearMonth): Flow<List<TransactionDetails>> =
        combine(
            transactionDao.observeInRange(month.startEpochDay(), month.endEpochDay()),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { transactions, categories, accounts ->
            val categoryById = categories.associateBy { it.id }
            val accountById = accounts.associateBy { it.id }
            transactions.mapNotNull { entity ->
                val account = accountById[entity.accountId] ?: return@mapNotNull null
                TransactionDetails(
                    transaction = entity.toDomain(),
                    category = entity.categoryId?.let { categoryById[it] }?.toDomain(),
                    account = account.toDomain(),
                    transferAccount = entity.transferAccountId?.let { accountById[it] }?.toDomain(),
                )
            }
        }

    override fun observeMonthlyTotals(month: YearMonth): Flow<MonthlyTotals> =
        transactionDao.observeTotals(month.startEpochDay(), month.endEpochDay())
            .map { MonthlyTotals(month, it.income, it.expense) }

    override fun observeExpenseByCategory(month: YearMonth): Flow<List<CategorySpending>> =
        combine(
            transactionDao.observeExpenseByCategory(month.startEpochDay(), month.endEpochDay()),
            categoryDao.observeAll(),
        ) { rows, categories ->
            val categoryById = categories.associateBy { it.id }
            rows.mapNotNull { row ->
                categoryById[row.categoryId]?.let { CategorySpending(it.toDomain(), row.total) }
            }.sortedByDescending { it.total }
        }

    override fun observeMonthlyTotalsRange(from: YearMonth, to: YearMonth): Flow<List<MonthlyTotals>> =
        transactionDao.observeInRange(from.startEpochDay(), to.endEpochDay()).map { transactions ->
            val months = monthsBetween(from, to)
            val grouped = transactions.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) }
            months.map { month ->
                val entries = grouped[month].orEmpty()
                MonthlyTotals(
                    month = month,
                    income = entries.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    expense = entries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                )
            }
        }

    override fun observeCategoryEvolution(
        categoryId: Long,
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<MonthlyTotals>> =
        combine(
            transactionDao.observeInRange(from.toEpochDay(), to.toEpochDay()),
            categoryDao.observeAll(),
        ) { transactions, categories ->
            val isIncome = categories.firstOrNull { it.id == categoryId }?.type == CategoryType.INCOME
            val months = monthsBetween(YearMonth.from(from), YearMonth.from(to))
            val grouped = transactions
                .filter { it.categoryId == categoryId }
                .groupBy { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) }
            months.map { month ->
                val total = grouped[month].orEmpty().sumOf { it.amount }
                MonthlyTotals(
                    month = month,
                    income = if (isIncome) total else 0.0,
                    expense = if (isIncome) 0.0 else total,
                )
            }
        }

    override suspend fun getTransaction(id: Long): Transaction? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun upsert(transaction: Transaction): Long =
        transactionDao.upsert(transaction.toEntity())

    override suspend fun delete(transaction: Transaction) =
        transactionDao.delete(transaction.toEntity())

    private fun monthsBetween(from: YearMonth, to: YearMonth): List<YearMonth> {
        val result = mutableListOf<YearMonth>()
        var cursor = from
        while (!cursor.isAfter(to)) {
            result.add(cursor)
            cursor = cursor.plusMonths(1)
        }
        return result
    }
}

private fun YearMonth.startEpochDay(): Long = atDay(1).toEpochDay()

private fun YearMonth.endEpochDay(): Long = atEndOfMonth().toEpochDay()
