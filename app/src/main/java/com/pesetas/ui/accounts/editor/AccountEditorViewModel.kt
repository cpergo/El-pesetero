package com.pesetas.ui.accounts.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.Account
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.ui.components.CategoryColors
import com.pesetas.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val name: String = "",
    val iconKey: String = "account_balance_wallet",
    val colorArgb: Int = CategoryColors.first(),
    val initialBalanceText: String = "0",
) {
    val initialBalance: Double
        get() = initialBalanceText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val canSave: Boolean get() = name.isNotBlank()
}

@HiltViewModel
class AccountEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val accountId: Long = savedStateHandle.get<Long>(Routes.ARG_ACCOUNT_ID) ?: -1L

    private val _uiState = MutableStateFlow(
        AccountEditorUiState(
            isLoading = accountId > 0,
            isEditing = accountId > 0,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    init {
        if (accountId > 0) {
            viewModelScope.launch {
                accountRepository.getAccount(accountId)?.let { account ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            name = account.name,
                            iconKey = account.iconKey,
                            colorArgb = account.colorArgb,
                            initialBalanceText = account.initialBalance.toString(),
                        )
                    }
                } ?: _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name) }

    fun setIcon(iconKey: String) = _uiState.update { it.copy(iconKey = iconKey) }

    fun setColor(colorArgb: Int) = _uiState.update { it.copy(colorArgb = colorArgb) }

    fun setInitialBalance(text: String) = _uiState.update {
        it.copy(initialBalanceText = text.filter { char -> char.isDigit() || char == ',' || char == '.' || char == '-' })
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            accountRepository.upsert(
                Account(
                    id = if (accountId > 0) accountId else 0,
                    name = state.name.trim(),
                    iconKey = state.iconKey,
                    colorArgb = state.colorArgb,
                    initialBalance = state.initialBalance,
                ),
            )
            _finished.emit(Unit)
        }
    }
}
