package com.pesetas.ui.transactions.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.model.Transaction
import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.TransactionRepository
import com.pesetas.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class EditorForm(
    val isLoading: Boolean,
    val isEditing: Boolean,
    val type: TransactionType,
    val amountText: String,
    val dateEpochDay: Long,
    val categoryId: Long?,
    val accountId: Long?,
    val note: String,
)

@HiltViewModel
class TransactionEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>(Routes.ARG_TRANSACTION_ID) ?: -1L
    private val initialType: TransactionType = runCatching {
        TransactionType.valueOf(savedStateHandle.get<String>(Routes.ARG_TYPE).orEmpty())
    }.getOrDefault(TransactionType.EXPENSE)

    private val form = MutableStateFlow(
        EditorForm(
            isLoading = true,
            isEditing = transactionId > 0,
            type = initialType,
            amountText = "",
            dateEpochDay = java.time.LocalDate.now().toEpochDay(),
            categoryId = null,
            accountId = null,
            note = "",
        ),
    )

    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    val uiState = combine(
        form,
        categoryRepository.observeCategories(),
        accountRepository.observeAccounts(),
    ) { current, categories, accounts ->
        val wantedType = if (current.type == TransactionType.INCOME) {
            CategoryType.INCOME
        } else {
            CategoryType.EXPENSE
        }
        TransactionEditorUiState(
            isLoading = current.isLoading,
            isEditing = current.isEditing,
            type = current.type,
            amountText = current.amountText,
            date = java.time.LocalDate.ofEpochDay(current.dateEpochDay),
            categoryId = current.categoryId,
            accountId = current.accountId ?: accounts.firstOrNull()?.id,
            note = current.note,
            categories = categories.filter { it.type == wantedType },
            accounts = accounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionEditorUiState(),
    )

    init {
        viewModelScope.launch {
            if (transactionId > 0) {
                transactionRepository.getTransaction(transactionId)?.let { existing ->
                    form.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            type = existing.type,
                            amountText = existing.amount.toString(),
                            dateEpochDay = existing.date.toEpochDay(),
                            categoryId = existing.categoryId,
                            accountId = existing.accountId,
                            note = existing.note,
                        )
                    }
                } ?: form.update { it.copy(isLoading = false) }
            } else {
                val defaultAccount = accountRepository.observeAccounts().first().firstOrNull()
                form.update { it.copy(isLoading = false, accountId = defaultAccount?.id) }
            }
        }
    }

    fun setType(type: TransactionType) {
        form.update { it.copy(type = type, categoryId = null) }
    }

    fun setAmount(text: String) {
        form.update { it.copy(amountText = text.filter { char -> char.isDigit() || char == ',' || char == '.' }) }
    }

    fun setDateEpochDay(epochDay: Long) {
        form.update { it.copy(dateEpochDay = epochDay) }
    }

    fun selectCategory(categoryId: Long) {
        form.update { it.copy(categoryId = categoryId) }
    }

    fun selectAccount(accountId: Long) {
        form.update { it.copy(accountId = accountId) }
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
                    type = state.type,
                    categoryId = state.categoryId,
                    accountId = state.accountId!!,
                    transferAccountId = null,
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
