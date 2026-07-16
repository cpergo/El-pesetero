package com.pesetas.ui.home

import com.pesetas.domain.model.BudgetStatus
import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.GeneratedRecurring
import java.time.YearMonth

data class HomeUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val totalBalance: Double = 0.0,
    val expenseByCategory: List<CategorySpending> = emptyList(),
    val budgetAlerts: List<BudgetStatus> = emptyList(),
    val generatedRecurring: List<GeneratedRecurring> = emptyList(),
) {
    val monthBalance: Double get() = income - expense
    val hasExpenses: Boolean get() = expenseByCategory.isNotEmpty()
}
