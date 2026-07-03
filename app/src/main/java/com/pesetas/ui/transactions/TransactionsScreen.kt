package com.pesetas.ui.transactions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.TransactionDetails
import com.pesetas.domain.model.TransactionType
import com.pesetas.ui.components.EmptyState
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.MonthNavigator
import com.pesetas.ui.util.formatDayHeader
import com.pesetas.ui.util.formatMoney
import java.time.LocalDate

@Composable
fun TransactionsScreen(
    onTransactionClick: (TransactionDetails) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        MonthNavigator(
            month = state.month,
            onPrevious = viewModel::showPreviousMonth,
            onNext = viewModel::showNextMonth,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        FilterRow(
            accountName = state.accounts.firstOrNull { it.id == state.accountFilter }?.name,
            categoryName = state.categories.firstOrNull { it.id == state.categoryFilter }?.name,
            accounts = state.accounts.map { it.id to it.name },
            categories = state.categories.map { it.id to it.name },
            onAccountSelected = viewModel::setAccountFilter,
            onCategorySelected = viewModel::setCategoryFilter,
            onClear = viewModel::clearFilters,
        )

        when {
            state.isLoading -> LoadingState()
            state.isEmpty -> EmptyState(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "Sin movimientos",
                message = "No hay movimientos en este mes. Pulsa + para añadir el primero.",
                modifier = Modifier.padding(top = 48.dp),
            )
            else -> TransactionList(
                items = state.items,
                onTransactionClick = onTransactionClick,
            )
        }
    }
}

@Composable
private fun FilterRow(
    accountName: String?,
    categoryName: String?,
    accounts: List<Pair<Long, String>>,
    categories: List<Pair<Long, String>>,
    onAccountSelected: (Long?) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DropdownFilterChip(
            label = accountName ?: "Cuenta",
            selected = accountName != null,
            options = accounts,
            onSelected = onAccountSelected,
        )
        DropdownFilterChip(
            label = categoryName ?: "Categoría",
            selected = categoryName != null,
            options = categories,
            onSelected = onCategorySelected,
        )
        if (accountName != null || categoryName != null) {
            AssistChip(
                onClick = onClear,
                label = { Text("Limpiar") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.padding(0.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun DropdownFilterChip(
    label: String,
    selected: Boolean,
    options: List<Pair<Long, String>>,
    onSelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(label) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TransactionList(
    items: List<TransactionDetails>,
    onTransactionClick: (TransactionDetails) -> Unit,
) {
    val grouped = items.groupBy { it.transaction.date }.toSortedMap(compareByDescending { it })
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        grouped.forEach { (date, dayItems) ->
            item(key = "header_$date") {
                DayHeader(date = date, items = dayItems)
            }
            items(dayItems, key = { it.transaction.id }) { details ->
                TransactionRow(
                    details = details,
                    onClick = { onTransactionClick(details) },
                )
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, items: List<TransactionDetails>) {
    val dayNet = items.sumOf { details ->
        when (details.transaction.type) {
            TransactionType.INCOME -> details.transaction.amount
            TransactionType.EXPENSE -> -details.transaction.amount
            TransactionType.TRANSFER -> 0.0
        }
    }
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDayHeader(date),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatMoney(dayNet),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}
