package com.pesetas.ui.goals.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.SavingsGoal
import com.pesetas.domain.repository.SavingsGoalRepository
import com.pesetas.ui.components.CategoryColors
import com.pesetas.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.pesetas.util.*

data class SavingsGoalEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val name: String = "",
    val targetAmountText: String = "",
    val deadline: LocalDate? = null,
    val iconKey: String = "savings",
    val colorArgb: Int = CategoryColors[3],
) {
    val targetAmount: Double
        get() = targetAmountText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val canSave: Boolean
        get() = name.isNotBlank() && targetAmount > 0
}

class SavingsGoalEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val savingsGoalRepository: SavingsGoalRepository,
) : ViewModel() {

    private val goalId: Long = savedStateHandle.get<Long>(Routes.ARG_GOAL_ID) ?: -1L
    private var currentAmount = 0.0

    private val _uiState = MutableStateFlow(
        SavingsGoalEditorUiState(
            isLoading = goalId > 0,
            isEditing = goalId > 0,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    init {
        if (goalId > 0) {
            viewModelScope.launch {
                savingsGoalRepository.getGoal(goalId)?.let { goal ->
                    currentAmount = goal.currentAmount
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = goal.name,
                            targetAmountText = goal.targetAmount.toString(),
                            deadline = goal.deadline,
                            iconKey = goal.iconKey,
                            colorArgb = goal.colorArgb,
                        )
                    }
                } ?: _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name) }

    fun setTargetAmount(text: String) = _uiState.update {
        it.copy(targetAmountText = text.filter { char -> char.isDigit() || char == ',' || char == '.' })
    }

    fun setDeadline(date: LocalDate?) = _uiState.update { it.copy(deadline = date) }

    fun setIcon(iconKey: String) = _uiState.update { it.copy(iconKey = iconKey) }

    fun setColor(colorArgb: Int) = _uiState.update { it.copy(colorArgb = colorArgb) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            savingsGoalRepository.upsert(
                SavingsGoal(
                    id = if (goalId > 0) goalId else 0,
                    name = state.name.trim(),
                    targetAmount = state.targetAmount,
                    currentAmount = currentAmount,
                    deadline = state.deadline,
                    iconKey = state.iconKey,
                    colorArgb = state.colorArgb,
                ),
            )
            _finished.emit(Unit)
        }
    }
}
