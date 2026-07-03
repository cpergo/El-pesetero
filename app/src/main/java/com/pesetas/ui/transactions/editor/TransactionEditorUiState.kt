package com.pesetas.ui.transactions.editor

import com.pesetas.domain.model.Account
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.TransactionType
import java.time.LocalDate

data class TransactionEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val date: LocalDate = LocalDate.now(),
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
