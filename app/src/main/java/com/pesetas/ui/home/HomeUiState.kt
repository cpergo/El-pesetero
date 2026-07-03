package com.pesetas.ui.home

import com.pesetas.domain.model.CategorySpending
import java.time.YearMonth

data class HomeUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val totalBalance: Double = 0.0,
    val expenseByCategory: List<CategorySpending> = emptyList(),
) {
    val monthBalance: Double get() = income - expense
    val hasExpenses: Boolean get() = expenseByCategory.isNotEmpty()
}
