package com.pesetas.ui.transactions.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.platform.BinaryContent
import com.pesetas.platform.asBinaryContent
import com.pesetas.data.files.ReceiptImageStore
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.model.Transaction
import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.TagRepository
import com.pesetas.domain.repository.TransactionRepository
import com.pesetas.ui.components.CategoryColors
import com.pesetas.ui.navigation.Routes
import com.pesetas.util.localDateFromEpochDay
import com.pesetas.util.toEpochDay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class EditorForm(
    val isLoading: Boolean,
    val isEditing: Boolean,
    val type: TransactionType,
    val amountText: String,
    val dateEpochDay: Long,
    val categoryId: Long?,
    val accountId: Long?,
    val note: String,
    val receiptImagePath: String?,
    val selectedTagIds: Set<Long>,
)

class TransactionEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val receiptImageStore: ReceiptImageStore,
    private val tagRepository: TagRepository,
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
            dateEpochDay = com.pesetas.util.currentDate().toEpochDay(),
            categoryId = null,
            accountId = null,
            note = "",
            receiptImagePath = null,
            selectedTagIds = emptySet(),
        ),
    )

    private var originalReceiptPath: String? = null

    private val _finished = MutableSharedFlow<Unit>()
    val finished = _finished.asSharedFlow()

    val uiState = combine(
        form,
        categoryRepository.observeCategories(),
        accountRepository.observeAccounts(),
        tagRepository.observeTags(),
    ) { current, categories, accounts, tags ->
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
            date = localDateFromEpochDay(current.dateEpochDay),
            categoryId = current.categoryId,
            accountId = current.accountId ?: accounts.firstOrNull()?.id,
            note = current.note,
            receiptImagePath = current.receiptImagePath,
            categories = categories.filter { it.type == wantedType },
            accounts = accounts,
            tags = tags,
            selectedTagIds = current.selectedTagIds,
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
                    originalReceiptPath = existing.receiptImagePath
                    val existingTagIds = tagRepository.getTagIdsForTransaction(transactionId).toSet()
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
                            receiptImagePath = existing.receiptImagePath,
                            selectedTagIds = existingTagIds,
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

    fun toggleTag(tagId: Long) {
        form.update {
            val current = it.selectedTagIds
            it.copy(
                selectedTagIds = if (tagId in current) current - tagId else current + tagId,
            )
        }
    }

    fun createTag(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val existingCount = uiState.value.tags.size
            val color = CategoryColors[existingCount % CategoryColors.size]
            val newId = tagRepository.createTag(name, color)
            form.update { it.copy(selectedTagIds = it.selectedTagIds + newId) }
        }
    }

    fun attachReceipt(content: BinaryContent) {
        viewModelScope.launch {
            receiptImageStore.store(content)?.let { attachStoredReceipt(it) }
        }
    }

    fun attachReceipt(encodedImage: ByteArray) {
        attachReceipt(encodedImage.asBinaryContent())
    }

    fun removeReceipt() {
        val current = form.value.receiptImagePath ?: return
        viewModelScope.launch {
            if (current != originalReceiptPath) {
                receiptImageStore.delete(current)
            }
            form.update { it.copy(receiptImagePath = null) }
        }
    }

    private suspend fun attachStoredReceipt(path: String) {
        val previous = form.value.receiptImagePath
        if (previous != null && previous != originalReceiptPath) {
            receiptImageStore.delete(previous)
        }
        form.update { it.copy(receiptImagePath = path) }
    }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            if (originalReceiptPath != null && originalReceiptPath != state.receiptImagePath) {
                receiptImageStore.delete(originalReceiptPath)
            }
            val insertedId = transactionRepository.upsert(
                Transaction(
                    id = if (transactionId > 0) transactionId else 0,
                    amount = state.amount,
                    date = state.date,
                    type = state.type,
                    categoryId = state.categoryId,
                    accountId = state.accountId!!,
                    transferAccountId = null,
                    note = state.note.trim(),
                    receiptImagePath = state.receiptImagePath,
                ),
            )
            val resolvedId = if (transactionId > 0) transactionId else insertedId
            tagRepository.setTagsForTransaction(resolvedId, state.selectedTagIds.toList())
            _finished.emit(Unit)
        }
    }

    fun delete() {
        if (transactionId <= 0) return
        viewModelScope.launch {
            transactionRepository.getTransaction(transactionId)?.let { existing ->
                receiptImageStore.delete(existing.receiptImagePath)
                transactionRepository.delete(existing)
            }
            _finished.emit(Unit)
        }
    }
}
