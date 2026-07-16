package com.pesetas.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.TagRepository
import com.pesetas.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

private data class Filters(
    val month: YearMonth,
    val accountId: Long?,
    val categoryId: Long?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    tagRepository: TagRepository,
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())
    private val accountFilter = MutableStateFlow<Long?>(null)
    private val categoryFilter = MutableStateFlow<Long?>(null)
    private val tagFilter = MutableStateFlow<Long?>(null)

    private val filters = combine(month, accountFilter, categoryFilter, ::Filters)

    val uiState = combine(
        filters.flatMapLatest { transactionRepository.observeTransactions(it.month) },
        accountRepository.observeAccounts(),
        categoryRepository.observeCategories(),
        month,
        combine(
            combine(accountFilter, categoryFilter, tagFilter, ::Triple),
            tagRepository.observeTags(),
            tagRepository.observeTransactionIdsByTag(),
        ) { filters, tags, refs -> Triple(filters, tags, refs) },
    ) { transactions, accounts, categories, currentMonth, extras ->
        val (activeFilters, tags, refs) = extras
        val (account, category, tag) = activeFilters
        val tagTransactionIds = tag?.let { refs[it] ?: emptySet() }
        val filtered = transactions.filter { details ->
            (account == null || details.transaction.accountId == account ||
                details.transaction.transferAccountId == account) &&
                (category == null || details.transaction.categoryId == category) &&
                (tagTransactionIds == null || details.transaction.id in tagTransactionIds)
        }
        TransactionsUiState(
            isLoading = false,
            month = currentMonth,
            items = filtered,
            accounts = accounts,
            categories = categories,
            tags = tags,
            accountFilter = account,
            categoryFilter = category,
            tagFilter = tag,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionsUiState(),
    )

    fun showPreviousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun showNextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun setAccountFilter(accountId: Long?) {
        accountFilter.value = accountId
    }

    fun setCategoryFilter(categoryId: Long?) {
        categoryFilter.value = categoryId
    }

    fun setTagFilter(tagId: Long?) {
        tagFilter.value = tagId
    }

    fun clearFilters() {
        accountFilter.value = null
        categoryFilter.value = null
        tagFilter.value = null
    }
}
