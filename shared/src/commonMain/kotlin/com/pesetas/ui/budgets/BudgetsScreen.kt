package com.pesetas.ui.budgets

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.ui.components.BudgetProgressBar
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.AppDialog
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.components.budgetSemaphoreColor
import com.pesetas.ui.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onBack: () -> Unit,
    viewModel: BudgetsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<BudgetItem?>(null) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { PesetasTopBar(title = "Presupuestos", onBack = onBack) },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Toca una categoría para fijar su límite mensual de gasto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            items(state.items, key = { it.category.id }) { item ->
                BudgetRow(item = item, onClick = { editing = item })
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    editing?.let { item ->
        BudgetDialog(
            item = item,
            onSave = { limit ->
                viewModel.setLimit(item.category.id, limit)
                editing = null
            },
            onClear = {
                viewModel.clearLimit(item.category.id)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun BudgetRow(item: BudgetItem, onClick: () -> Unit) {
    AppCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconBadge(
                    iconKey = item.category.iconKey,
                    colorArgb = item.category.colorArgb,
                    size = 38.dp,
                )
                Text(
                    text = item.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (item.monthlyLimit == null) {
                    Text(
                        text = "Sin límite",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = "${formatMoney(item.spent)} / ${formatMoney(item.monthlyLimit)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = budgetSemaphoreColor(item.ratio),
                    )
                }
            }
            if (item.monthlyLimit != null) {
                BudgetProgressBar(spent = item.spent, limit = item.monthlyLimit)
            }
        }
    }
}

@Composable
private fun BudgetDialog(
    item: BudgetItem,
    onSave: (Double) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var limitText by remember {
        mutableStateOf(item.monthlyLimit?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    val parsed = limitText.replace(',', '.').toDoubleOrNull()

    AppDialog(
        title = "Presupuesto de ${item.category.name}",
        onDismiss = onDismiss,
        confirmText = "Guardar",
        onConfirm = { parsed?.let(onSave) },
        confirmEnabled = parsed != null && parsed > 0,
        extraActionText = if (item.monthlyLimit != null) "Quitar límite" else null,
        onExtraAction = if (item.monthlyLimit != null) onClear else null,
    ) {
        PesetasField(
            value = limitText,
            onValueChange = { text ->
                limitText = text.filter { it.isDigit() || it == ',' || it == '.' }
            },
            label = "Límite mensual",
            suffix = "€",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            helperText = "Se aplica a todo el mes en curso, incluido lo ya gastado.",
        )
    }
}
