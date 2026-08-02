package com.pesetas.domain.model

import com.pesetas.util.*

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val date: LocalDate,
    val type: TransactionType,
    val categoryId: Long?,
    val accountId: Long,
    val transferAccountId: Long?,
    val note: String,
    val receiptImagePath: String? = null,
)
