package com.pesetas.data.repository

import com.pesetas.data.local.dao.AccountDao
import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.local.dao.RecurringTransactionDao
import com.pesetas.data.local.dao.TransactionDao
import com.pesetas.data.local.entity.TransactionEntity
import com.pesetas.data.mapper.toDomain
import com.pesetas.data.mapper.toEntity
import com.pesetas.domain.model.GeneratedRecurring
import com.pesetas.domain.model.RecurringTransaction
import com.pesetas.domain.model.RecurringTransactionDetails
import com.pesetas.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.pesetas.util.*
import kotlin.math.min

class RecurringTransactionRepositoryImpl(
    private val recurringDao: RecurringTransactionDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
) : RecurringTransactionRepository {

    private val generationLock = Mutex()
    private var generationDone = false

    override fun observeRules(): Flow<List<RecurringTransactionDetails>> =
        combine(
            recurringDao.observeAll(),
            categoryDao.observeAll(),
            accountDao.observeAll(),
        ) { rules, categories, accounts ->
            val categoryById = categories.associateBy { it.id }
            val accountById = accounts.associateBy { it.id }
            rules.map { entity ->
                RecurringTransactionDetails(
                    rule = entity.toDomain(),
                    category = categoryById[entity.categoryId]?.toDomain(),
                    account = accountById[entity.accountId]?.toDomain(),
                )
            }
        }

    override suspend fun getRule(id: Long): RecurringTransaction? =
        recurringDao.getById(id)?.toDomain()

    override suspend fun upsert(rule: RecurringTransaction) {
        recurringDao.upsert(rule.toEntity())
    }

    override suspend fun delete(rule: RecurringTransaction) {
        recurringDao.delete(rule.toEntity())
    }

    override suspend fun generatePendingOnce(): List<GeneratedRecurring> =
        generationLock.withLock {
            if (generationDone) return emptyList()
            generationDone = true
            val today = currentDate()
            val generated = mutableListOf<GeneratedRecurring>()
            recurringDao.getAll().forEach { entity ->
                val rule = entity.toDomain()
                val occurrences = recurringOccurrencesBetween(rule.lastGeneratedDate, today, rule.dayOfMonth)
                if (occurrences.isEmpty()) return@forEach
                val categoryName = categoryDao.getById(rule.categoryId)?.name ?: rule.note
                occurrences.forEach { date ->
                    val transactionId = transactionDao.upsert(
                        TransactionEntity(
                            amount = rule.amount,
                            dateEpochDay = date.toEpochDay(),
                            type = rule.type,
                            categoryId = rule.categoryId,
                            accountId = rule.accountId,
                            transferAccountId = null,
                            note = rule.note,
                        ),
                    )
                    generated.add(
                        GeneratedRecurring(
                            transactionId = transactionId,
                            label = rule.note.ifBlank { categoryName },
                            amount = rule.amount,
                            type = rule.type,
                            date = date,
                        ),
                    )
                }
                recurringDao.updateLastGenerated(rule.id, occurrences.last().toEpochDay())
            }
            generated
        }

    override suspend fun undoGenerated(transactionId: Long) {
        transactionDao.getById(transactionId)?.let { transactionDao.delete(it) }
    }

}

internal fun recurringOccurrencesBetween(
    lastGenerated: LocalDate,
    today: LocalDate,
    dayOfMonth: Int,
): List<LocalDate> {
    val occurrences = mutableListOf<LocalDate>()
    var month = yearMonthOf(lastGenerated)
    val endMonth = yearMonthOf(today)
    while (!month.isAfter(endMonth)) {
        val candidate = month.atDay(min(dayOfMonth, month.lengthOfMonth()))
        if (candidate.isAfter(lastGenerated) && !candidate.isAfter(today)) {
            occurrences.add(candidate)
        }
        month = month.plusMonths(1)
    }
    return occurrences
}
