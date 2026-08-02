package com.pesetas.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.RecurringTransactionDetails
import com.pesetas.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecurringUiState(
    val isLoading: Boolean = true,
    val rules: List<RecurringTransactionDetails> = emptyList(),
)

class RecurringViewModel(
    private val recurringRepository: RecurringTransactionRepository,
) : ViewModel() {

    val uiState = recurringRepository.observeRules()
        .map { RecurringUiState(isLoading = false, rules = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecurringUiState(),
        )

    fun delete(details: RecurringTransactionDetails) {
        viewModelScope.launch { recurringRepository.delete(details.rule) }
    }
}
