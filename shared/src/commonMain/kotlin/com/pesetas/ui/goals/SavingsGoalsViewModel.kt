package com.pesetas.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.SavingsGoal
import com.pesetas.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavingsGoalsUiState(
    val isLoading: Boolean = true,
    val goals: List<SavingsGoal> = emptyList(),
)

class SavingsGoalsViewModel(
    private val savingsGoalRepository: SavingsGoalRepository,
) : ViewModel() {

    val uiState = savingsGoalRepository.observeGoals()
        .map { SavingsGoalsUiState(isLoading = false, goals = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SavingsGoalsUiState(),
        )

    fun contribute(goalId: Long, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch { savingsGoalRepository.contribute(goalId, amount) }
    }

    fun delete(goal: SavingsGoal) {
        viewModelScope.launch { savingsGoalRepository.delete(goal) }
    }
}
