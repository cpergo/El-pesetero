package com.pesetas.ui.recurring.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.Account
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.model.RecurringTransaction
import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.RecurringTransactionRepository
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
import java.time.LocalDate
import javax.inject.Inject

data class RecurringEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val dayOfMonth: Int = 1,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
) {
    val amount: Double
        get() = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val canSave: Boolean
        get() = amount > 0.0 && categoryId != null && accountId != null
}

private data class RecurringForm(
    val isLoading: Boolean,
    val isEditing: Boolean,
    val type: TransactionType,
    val amountText: String,
    val dayOfMonth: Int,
    val categoryId: Long?,
    val accountId: Long?,
    val note: String,
    val lastGeneratedEpochDay: Long?,
)

@HiltViewModel
class RecurringEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recurringRepository: RecurringTransactionRepository,
    categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val ruleId: Long = savedStateHandle.get<Long>(Routes.ARG_RULE_ID) ?: -1L

    private val form = MutableStateFlow(
        RecurringForm(
            isLoading = true,
            isEditing = ruleId > 0,
            type = TransactionType.EXPENSE,
            amountText = "",
            dayOfMonth = 1,
            categoryId = null,
            accountId = null,
            note = "",
            lastGeneratedEpochDay = null,
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
        RecurringEditorUiState(
            isLoading = current.isLoading,
            isEditing = current.isEditing,
            type = current.type,
            amountText = current.amountText,
            dayOfMonth = current.dayOfMonth,
            categoryId = current.categoryId,
            accountId = current.accountId ?: accounts.firstOrNull()?.id,
            note = current.note,
            categories = categories.filter { it.type == wantedType },
            accounts = accounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecurringEditorUiState(),
    )

    init {
        viewModelScope.launch {
            if (ruleId > 0) {
                recurringRepository.getRule(ruleId)?.let { existing ->
                    form.update {
                        it.copy(
                            isLoading = false,
                            type = existing.type,
                            amountText = existing.amount.toString(),
                            dayOfMonth = existing.dayOfMonth,
                            categoryId = existing.categoryId,
                            accountId = existing.accountId,
                            note = existing.note,
                            lastGeneratedEpochDay = existing.lastGeneratedDate.toEpochDay(),
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

    fun setDayOfMonth(day: Int) {
        form.update { it.copy(dayOfMonth = day.coerceIn(1, 31)) }
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
        val lastGenerated = form.value.lastGeneratedEpochDay
            ?.let { LocalDate.ofEpochDay(it) }
            ?: LocalDate.now()
        viewModelScope.launch {
            recurringRepository.upsert(
                RecurringTransaction(
                    id = if (ruleId > 0) ruleId else 0,
                    amount = state.amount,
                    type = state.type,
                    categoryId = state.categoryId!!,
                    accountId = state.accountId!!,
                    note = state.note.trim(),
                    dayOfMonth = state.dayOfMonth,
                    lastGeneratedDate = lastGenerated,
                ),
            )
            _finished.emit(Unit)
        }
    }

    fun delete() {
        if (ruleId <= 0) return
        viewModelScope.launch {
            recurringRepository.getRule(ruleId)?.let { recurringRepository.delete(it) }
            _finished.emit(Unit)
        }
    }
}
