package com.pesetas.ui.statistics

import com.pesetas.domain.model.Category
import com.pesetas.domain.model.MonthlyTotals
import java.time.YearMonth

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val fromMonth: YearMonth = YearMonth.now().minusMonths(5),
    val toMonth: YearMonth = YearMonth.now(),
    val monthlyTotals: List<MonthlyTotals> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val categoryEvolution: List<MonthlyTotals> = emptyList(),
) {
    val totalIncome: Double get() = monthlyTotals.sumOf { it.income }
    val totalExpense: Double get() = monthlyTotals.sumOf { it.expense }
    val selectedCategory: Category?
        get() = categories.firstOrNull { it.id == selectedCategoryId }
}
