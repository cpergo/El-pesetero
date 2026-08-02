package com.pesetas.ui.currencies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.ui.components.EmptyState
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasDropdownMenu
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasPickerField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.components.AppDialog
import com.pesetas.ui.util.CurrencyCatalog
import com.pesetas.ui.util.formatDate

@Composable
fun CurrenciesScreen(
    onBack: () -> Unit,
    viewModel: CurrenciesViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editingRate by remember { mutableStateOf<ForeignCurrencyItem?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { PesetasTopBar(title = "Divisas", onBack = onBack) },
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
                MainCurrencyPicker(
                    selected = state.mainCurrency,
                    onSelected = viewModel::setMainCurrency,
                )
            }
            if (state.foreignCurrencies.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.CurrencyExchange,
                        title = "Una sola divisa",
                        message = "Cuando alguna cuenta use otra divisa, aquí definirás " +
                            "su tasa de conversión manual.",
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            } else {
                item {
                    Text(
                        text = "Tasas de conversión manuales hacia la divisa principal. " +
                            "Actualízalas cuando quieras.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(state.foreignCurrencies, key = { it.code }) { item ->
                    RateRow(
                        item = item,
                        mainCurrency = state.mainCurrency,
                        onClick = { editingRate = item },
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    editingRate?.let { item ->
        RateDialog(
            item = item,
            mainCurrency = state.mainCurrency,
            onSave = { rate ->
                viewModel.setRate(item.code, rate)
                editingRate = null
            },
            onDismiss = { editingRate = null },
        )
    }
}

@Composable
private fun MainCurrencyPicker(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PesetasPickerField(
            value = "$selected · ${CurrencyCatalog.nameFor(selected)}",
            label = "Divisa principal",
            onClick = { expanded = true },
            trailingIcon = Icons.Filled.ArrowDropDown,
        )
        PesetasDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            CurrencyCatalog.currencies.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$code · $name",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun RateRow(
    item: ForeignCurrencyItem,
    mainCurrency: String,
    onClick: () -> Unit,
) {
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.code} · ${CurrencyCatalog.nameFor(item.code)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when {
                        item.rate == null -> "Sin tasa definida: toca para fijarla"
                        else -> "Actualizada el ${formatDate(item.rate.updatedDate)}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.rate == null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            if (item.rate != null) {
                Text(
                    text = "1 ${item.code} = ${item.rate.rateToMain} $mainCurrency",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RateDialog(
    item: ForeignCurrencyItem,
    mainCurrency: String,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var rateText by remember { mutableStateOf(item.rate?.rateToMain?.toString() ?: "") }
    val parsed = rateText.replace(',', '.').toDoubleOrNull()

    AppDialog(
        title = "Tasa de ${item.code}",
        onDismiss = onDismiss,
        confirmText = "Guardar",
        onConfirm = { parsed?.let(onSave) },
        confirmEnabled = parsed != null && parsed > 0,
    ) {
        PesetasField(
            value = rateText,
            onValueChange = { text ->
                rateText = text.filter { it.isDigit() || it == ',' || it == '.' }
            },
            label = "1 ${item.code} equivale a",
            suffix = mainCurrency,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            helperText = "Tasa manual: se usará para el saldo total combinado hasta que la actualices.",
        )
    }
}
