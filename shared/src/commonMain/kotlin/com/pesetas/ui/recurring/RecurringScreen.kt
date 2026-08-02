package com.pesetas.ui.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.RecurringTransactionDetails
import com.pesetas.domain.model.TransactionType
import com.pesetas.ui.components.AppFab
import com.pesetas.ui.components.EmptyState
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.theme.LocalPesetasColors
import com.pesetas.ui.util.formatSignedMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    onBack: () -> Unit,
    onAddRule: () -> Unit,
    onEditRule: (Long) -> Unit,
    viewModel: RecurringViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { PesetasTopBar(title = "Recurrentes", onBack = onBack) },
        floatingActionButton = {
            AppFab(
                onClick = onAddRule,
                icon = Icons.Filled.Add,
                contentDescription = "Añadir regla",
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.rules.isEmpty() -> EmptyState(
                icon = Icons.Filled.EventRepeat,
                title = "Sin movimientos recurrentes",
                message = "Crea reglas para el alquiler, suscripciones o tu nómina y se " +
                    "apuntarán solas cada mes.",
                modifier = Modifier.padding(padding).padding(top = 48.dp),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.rules, key = { it.rule.id }) { details ->
                    RecurringRow(
                        details = details,
                        onClick = { onEditRule(details.rule.id) },
                        onDelete = { viewModel.delete(details) },
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun RecurringRow(
    details: RecurringTransactionDetails,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalPesetasColors.current
    val rule = details.rule
    val isIncome = rule.type == TransactionType.INCOME
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconBadge(
                iconKey = details.category?.iconKey ?: "currency_exchange",
                colorArgb = details.category?.colorArgb ?: 0xFF8A6D3B.toInt(),
                size = 40.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.note.ifBlank { details.category?.name ?: "Movimiento" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Día ${rule.dayOfMonth} de cada mes · ${details.account?.name.orEmpty()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatSignedMoney(rule.amount, positive = isIncome),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isIncome) colors.income else colors.expense,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar regla",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
