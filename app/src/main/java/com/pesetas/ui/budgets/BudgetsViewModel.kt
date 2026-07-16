package com.pesetas.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.repository.BudgetRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class BudgetItem(
    val category: Category,
    val monthlyLimit: Double?,
    val spent: Double,
) {
    val ratio: Double
        get() = if (monthlyLimit != null && monthlyLimit > 0) spent / monthlyLimit else 0.0
}

data class BudgetsUiState(
    val isLoading: Boolean = true,
    val items: List<BudgetItem> = emptyList(),
    val month: YearMonth = YearMonth.now(),
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
    transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {

    private val month = YearMonth.now()

    val uiState = combine(
        categoryRepository.observeCategories(CategoryType.EXPENSE),
        budgetRepository.observeBudgets(),
        transactionRepository.observeExpenseByCategory(month),
    ) { categories, budgets, spending ->
        val limitByCategory = budgets.associate { it.categoryId to it.monthlyLimit }
        val spentByCategory = spending.associate { it.category.id to it.total }
        BudgetsUiState(
            isLoading = false,
            items = categories.map { category ->
                BudgetItem(
                    category = category,
                    monthlyLimit = limitByCategory[category.id],
                    spent = spentByCategory[category.id] ?: 0.0,
                )
            },
            month = month,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetsUiState(),
    )

    fun setLimit(categoryId: Long, limit: Double) {
        if (limit <= 0) return
        viewModelScope.launch { budgetRepository.setBudget(categoryId, limit) }
    }

    fun clearLimit(categoryId: Long) {
        viewModelScope.launch { budgetRepository.clearBudget(categoryId) }
    }
}
