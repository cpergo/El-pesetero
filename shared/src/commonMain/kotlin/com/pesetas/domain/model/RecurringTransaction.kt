package com.pesetas.domain.model

import com.pesetas.util.*

data class RecurringTransaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    val dayOfMonth: Int,
    val lastGeneratedDate: LocalDate,
)

data class RecurringTransactionDetails(
    val rule: RecurringTransaction,
    val category: Category?,
    val account: Account?,
)

data class GeneratedRecurring(
    val transactionId: Long,
    val label: String,
    val amount: Double,
    val type: TransactionType,
    val date: LocalDate,
)
