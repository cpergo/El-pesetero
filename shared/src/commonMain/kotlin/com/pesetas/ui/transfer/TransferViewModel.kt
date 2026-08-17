package com.pesetas.ui.transfer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.Transaction
import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.TransactionRepository
import com.pesetas.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.pesetas.util.*

private data class TransferForm(
    val isLoading: Boolean,
    val isEditing: Boolean,
    val amountText: String,
    val dateEpochDay: Long,
    val fromAccountId: Long?,
    val toAccountId: Long?,
    val note: String,
)

class TransferViewModel(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>(Routes.ARG_TRANSACTION_ID) ?: -1L

    private val form = MutableStateFlow(
        TransferForm(
            isLoading = true,
            isEditing = transactionId > 0,
            amountText = "",
            dateEpochDay = currentDate().toEpochDay(),
            fromAccountId = null,
            toAccountId = null,
            note = "",
        ),
    )

    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    val uiState = combine(form, accountRepository.observeAccounts()) { current, accounts ->
        TransferUiState(
            isLoading = current.isLoading,
            isEditing = current.isEditing,
            amountText = current.amountText,
            date = localDateFromEpochDay(current.dateEpochDay),
            fromAccountId = current.fromAccountId ?: accounts.getOrNull(0)?.id,
            toAccountId = current.toAccountId ?: accounts.getOrNull(1)?.id,
            note = current.note,
            accounts = accounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransferUiState(),
    )

    init {
        viewModelScope.launch {
            if (transactionId > 0) {
                transactionRepository.getTransaction(transactionId)?.let { existing ->
                    form.update {
                        it.copy(
                            isLoading = false,
                            amountText = existing.amount.toString(),
                            dateEpochDay = existing.date.toEpochDay(),
                            fromAccountId = existing.accountId,
                            toAccountId = existing.transferAccountId,
                            note = existing.note,
                        )
                    }
                } ?: form.update { it.copy(isLoading = false) }
            } else {
                accountRepository.observeAccounts().first()
                form.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setAmount(text: String) {
        form.update { it.copy(amountText = text.filter { char -> char.isDigit() || char == ',' || char == '.' }) }
    }

    fun setDateEpochDay(epochDay: Long) {
        form.update { it.copy(dateEpochDay = epochDay) }
    }

    fun selectFrom(accountId: Long) {
        form.update { it.copy(fromAccountId = accountId) }
    }

    fun selectTo(accountId: Long) {
        form.update { it.copy(toAccountId = accountId) }
    }

    fun setNote(note: String) {
        form.update { it.copy(note = note) }
    }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            transactionRepository.upsert(
                Transaction(
                    id = if (transactionId > 0) transactionId else 0,
                    amount = state.amount,
                    date = state.date,
                    type = TransactionType.TRANSFER,
                    categoryId = null,
                    accountId = state.fromAccountId!!,
                    transferAccountId = state.toAccountId!!,
                    note = state.note.trim(),
                ),
            )
            _finished.emit(Unit)
        }
    }

    fun delete() {
        if (transactionId <= 0) return
        viewModelScope.launch {
            transactionRepository.getTransaction(transactionId)?.let {
                transactionRepository.delete(it)
            }
            _finished.emit(Unit)
        }
    }
}
