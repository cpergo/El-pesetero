package com.pesetas.ui.transfer

import com.pesetas.domain.model.Account
import com.pesetas.util.*

data class TransferUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val amountText: String = "",
    val date: LocalDate = currentDate(),
    val fromAccountId: Long? = null,
    val toAccountId: Long? = null,
    val note: String = "",
    val accounts: List<Account> = emptyList(),
) {
    val amount: Double
        get() = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0

    val canSave: Boolean
        get() = amount > 0.0 && fromAccountId != null && toAccountId != null &&
            fromAccountId != toAccountId
}
