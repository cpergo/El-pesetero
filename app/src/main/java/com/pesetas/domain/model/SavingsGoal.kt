package com.pesetas.domain.model

import java.time.LocalDate

data class SavingsGoal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: LocalDate? = null,
    val iconKey: String = "savings",
    val colorArgb: Int,
) {
    val ratio: Double
        get() = if (targetAmount > 0) currentAmount / targetAmount else 0.0

    val isCompleted: Boolean
        get() = targetAmount > 0 && currentAmount >= targetAmount
}
