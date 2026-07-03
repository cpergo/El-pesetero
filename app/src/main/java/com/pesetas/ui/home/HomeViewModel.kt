package com.pesetas.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.repository.AccountRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState = selectedMonth.flatMapLatest { month ->
        combine(
            transactionRepository.observeMonthlyTotals(month),
            transactionRepository.observeExpenseByCategory(month),
            accountRepository.observeTotalBalance(),
        ) { totals, expenseByCategory, totalBalance ->
            HomeUiState(
                isLoading = false,
                month = month,
                income = totals.income,
                expense = totals.expense,
                totalBalance = totalBalance,
                expenseByCategory = expenseByCategory,
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
}
