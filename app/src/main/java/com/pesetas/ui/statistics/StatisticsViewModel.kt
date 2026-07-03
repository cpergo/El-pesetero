package com.pesetas.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.MonthlyTotals
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

private data class Range(val from: YearMonth, val to: YearMonth)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val range = MutableStateFlow(Range(YearMonth.now().minusMonths(5), YearMonth.now()))
    private val selectedCategoryId = MutableStateFlow<Long?>(null)

    private val monthlyTotals = range.flatMapLatest { current ->
        transactionRepository.observeMonthlyTotalsRange(current.from, current.to)
    }

    private val categoryEvolution = combine(range, selectedCategoryId) { current, categoryId ->
        current to categoryId
    }.flatMapLatest { (current, categoryId) ->
        if (categoryId == null) {
            flowOf(emptyList<MonthlyTotals>())
        } else {
            transactionRepository.observeCategoryEvolution(
                categoryId = categoryId,
                from = current.from.atDay(1),
                to = current.to.atEndOfMonth(),
            )
        }
    }

    val uiState = combine(
        range,
        monthlyTotals,
        categoryRepository.observeCategories(),
        selectedCategoryId,
        categoryEvolution,
    ) { current, totals, categories, categoryId, evolution ->
        StatisticsUiState(
            isLoading = false,
            fromMonth = current.from,
            toMonth = current.to,
            monthlyTotals = totals,
            categories = categories,
            selectedCategoryId = categoryId,
            categoryEvolution = evolution,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(),
    )

    fun setPresetMonths(months: Long) {
        val now = YearMonth.now()
        range.value = Range(now.minusMonths(months - 1), now)
    }

    fun setCustomRange(from: YearMonth, to: YearMonth) {
        range.value = if (from.isAfter(to)) Range(to, from) else Range(from, to)
    }

    fun selectCategory(categoryId: Long?) {
        selectedCategoryId.value = categoryId
    }
}
