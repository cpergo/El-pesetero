package com.pesetas.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.GeneratedRecurring
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.BudgetRepository
import com.pesetas.domain.repository.RecurringTransactionRepository
import com.pesetas.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringTransactionRepository,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val generatedRecurring = MutableStateFlow<List<GeneratedRecurring>>(emptyList())

    init {
        viewModelScope.launch {
            generatedRecurring.value = recurringRepository.generatePendingOnce()
        }
    }

    private val notices = combine(
        budgetRepository.observeBudgetStatuses(YearMonth.now()),
        generatedRecurring,
    ) { statuses, generated ->
        statuses.filter { it.ratio >= 0.8 } to generated
    }

    val uiState = selectedMonth.flatMapLatest { month ->
        combine(
            transactionRepository.observeMonthlyTotals(month),
            transactionRepository.observeExpenseByCategory(month),
            accountRepository.observeTotalBalance(),
            notices,
        ) { totals, expenseByCategory, totalBalance, (alerts, generated) ->
            HomeUiState(
                isLoading = false,
                month = month,
                income = totals.income,
                expense = totals.expense,
                totalBalance = totalBalance,
                expenseByCategory = expenseByCategory,
                budgetAlerts = alerts,
                generatedRecurring = generated,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun showPreviousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun showNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun undoGenerated(transactionId: Long) {
        viewModelScope.launch {
            recurringRepository.undoGenerated(transactionId)
            generatedRecurring.update { list -> list.filterNot { it.transactionId == transactionId } }
        }
    }

    fun dismissGenerated() {
        generatedRecurring.value = emptyList()
    }
}
