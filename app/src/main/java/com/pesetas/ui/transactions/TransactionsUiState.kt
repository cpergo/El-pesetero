package com.pesetas.ui.transactions

import com.pesetas.domain.model.Account
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.Tag
import com.pesetas.domain.model.TransactionDetails
import java.time.YearMonth

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val items: List<TransactionDetails> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val accountFilter: Long? = null,
    val categoryFilter: Long? = null,
    val tagFilter: Long? = null,
) {
    val isEmpty: Boolean get() = items.isEmpty()
}
