package com.pesetas.domain.model

import com.pesetas.util.*

data class AccountBalance(
    val account: Account,
    val balance: Double,
)

data class CategorySpending(
    val category: Category,
    val total: Double,
)

data class MonthlyTotals(
    val month: YearMonth,
    val income: Double,
    val expense: Double,
) {
    val balance: Double get() = income - expense
}
