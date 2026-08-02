package com.pesetas.ui.statistics

import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.MonthlyTotals
import com.pesetas.domain.model.TagSpending
import com.pesetas.util.*

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val fromMonth: YearMonth = currentYearMonth().minusMonths(5),
    val toMonth: YearMonth = currentYearMonth(),
    val monthlyTotals: List<MonthlyTotals> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val categoryEvolution: List<MonthlyTotals> = emptyList(),
    val tagTotals: List<TagSpending> = emptyList(),
    val selectedTagId: Long? = null,
    val tagBreakdown: List<CategorySpending> = emptyList(),
) {
    val totalIncome: Double get() = monthlyTotals.sumOf { it.income }
    val totalExpense: Double get() = monthlyTotals.sumOf { it.expense }
    val selectedCategory: Category?
        get() = categories.firstOrNull { it.id == selectedCategoryId }
}
