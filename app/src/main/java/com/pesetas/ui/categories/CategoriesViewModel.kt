package com.pesetas.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.BudgetStatus
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.repository.BudgetRepository
import com.pesetas.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val type: CategoryType = CategoryType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val budgetStatusByCategoryId: Map<Long, BudgetStatus> = emptyMap(),
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    budgetRepository: BudgetRepository,
) : ViewModel() {

    private val type = MutableStateFlow(CategoryType.EXPENSE)

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    val uiState = combine(
        type,
        categoryRepository.observeCategories(),
        budgetRepository.observeBudgetStatuses(YearMonth.now()),
    ) { currentType, all, budgetStatuses ->
        CategoriesUiState(
            isLoading = false,
            type = currentType,
            categories = all.filter { it.type == currentType },
            budgetStatusByCategoryId = budgetStatuses.associateBy { it.category.id },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState(),
    )

    fun selectType(newType: CategoryType) {
        type.value = newType
    }

    fun moveUp(category: Category) = move(category, -1)

    fun moveDown(category: Category) = move(category, 1)

    private fun move(category: Category, offset: Int) {
        val current = uiState.value.categories
        val index = current.indexOfFirst { it.id == category.id }
        val target = index + offset
        if (index < 0 || target < 0 || target >= current.size) return
        val reordered = current.toMutableList().apply {
            add(target, removeAt(index))
        }
        viewModelScope.launch {
            categoryRepository.reorder(reordered.map { it.id })
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch {
            if (categoryRepository.isInUse(category.id)) {
                _messages.emit("No puedes borrar «${category.name}»: tiene movimientos asociados")
            } else {
                categoryRepository.delete(category)
            }
        }
    }
}
