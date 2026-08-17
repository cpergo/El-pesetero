package com.pesetas.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.MonthlyTotals
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.TagRepository
import com.pesetas.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import com.pesetas.util.*

private data class Range(val from: YearMonth, val to: YearMonth)

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
) : ViewModel() {

    private val range = MutableStateFlow(Range(currentYearMonth().minusMonths(5), currentYearMonth()))
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val selectedTagId = MutableStateFlow<Long?>(null)

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

    private val tagTotals = range.flatMapLatest { current ->
        tagRepository.observeTagExpenseTotals(current.from, current.to)
    }

    private val tagBreakdown = combine(range, selectedTagId) { current, tagId ->
        current to tagId
    }.flatMapLatest { (current, tagId) ->
        if (tagId == null) {
            flowOf(emptyList<CategorySpending>())
        } else {
            tagRepository.observeTagCategoryBreakdown(tagId, current.from, current.to)
        }
    }

    val uiState = combine(
        range,
        monthlyTotals,
        categoryRepository.observeCategories(),
        combine(selectedCategoryId, categoryEvolution, ::Pair),
        combine(selectedTagId, tagTotals, tagBreakdown, ::Triple),
    ) { current, totals, categories, categoryState, tagState ->
        val (categoryId, evolution) = categoryState
        val (tagId, totalsByTag, breakdown) = tagState
        StatisticsUiState(
            isLoading = false,
            fromMonth = current.from,
            toMonth = current.to,
            monthlyTotals = totals,
            categories = categories,
            selectedCategoryId = categoryId,
            categoryEvolution = evolution,
            tagTotals = totalsByTag,
            selectedTagId = tagId,
            tagBreakdown = breakdown,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(),
    )

    fun setPresetMonths(months: Long) {
        val now = currentYearMonth()
        range.value = Range(now.minusMonths(months - 1), now)
    }

    fun setCustomRange(from: YearMonth, to: YearMonth) {
        range.value = if (from.isAfter(to)) Range(to, from) else Range(from, to)
    }

    fun selectCategory(categoryId: Long?) {
        selectedCategoryId.value = categoryId
    }

    fun selectTag(tagId: Long?) {
        selectedTagId.value = if (selectedTagId.value == tagId) null else tagId
    }
}
