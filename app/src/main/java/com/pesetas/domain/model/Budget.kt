package com.pesetas.domain.model

data class Budget(
    val categoryId: Long,
    val monthlyLimit: Double,
)

data class BudgetStatus(
    val category: Category,
    val monthlyLimit: Double,
    val spent: Double,
) {
    val ratio: Double get() = if (monthlyLimit > 0) spent / monthlyLimit else 0.0
    val isNearLimit: Boolean get() = ratio >= 0.8 && ratio < 1.0
    val isOverLimit: Boolean get() = ratio >= 1.0
}
