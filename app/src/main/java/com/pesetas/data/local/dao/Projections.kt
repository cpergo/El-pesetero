package com.pesetas.data.local.dao

import androidx.room.Embedded
import com.pesetas.data.local.entity.AccountEntity

data class AccountWithBalance(
    @Embedded val account: AccountEntity,
    val balance: Double,
)

data class CategorySpendingRow(
    val categoryId: Long,
    val total: Double,
)

data class TotalsRow(
    val income: Double,
    val expense: Double,
)
