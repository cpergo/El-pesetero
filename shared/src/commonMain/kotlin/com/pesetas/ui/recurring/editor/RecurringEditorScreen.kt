package com.pesetas.ui.recurring.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.TransactionType
import com.pesetas.ui.components.FieldLabel
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasDropdownMenu
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasPickerField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppButton
import com.pesetas.ui.components.AppChip
import com.pesetas.ui.components.AppSegmented
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurringEditorScreen(
    onBack: () -> Unit,
    viewModel: RecurringEditorViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.finished.collectLatest { onBack() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PesetasTopBar(
                title = if (state.isEditing) "Editar recurrente" else "Nueva recurrente",
                onBack = onBack,
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppSegmented(
                options = listOf(
                    TransactionType.EXPENSE to "Gasto",
                    TransactionType.INCOME to "Ingreso",
                ),
                selected = state.type,
                onSelect = viewModel::setType,
                modifier = Modifier.fillMaxWidth(),
            )

            PesetasField(
                value = state.amountText,
                onValueChange = viewModel::setAmount,
                label = "Importe",
                suffix = "€",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            DayOfMonthDropdown(
                selected = state.dayOfMonth,
                onSelected = viewModel::setDayOfMonth,
            )

            FieldLabel("Categoría")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.categories.forEach { category ->
                    AppChip(
                        selected = state.categoryId == category.id,
                        onClick = { viewModel.selectCategory(category.id) },
                        label = category.name,
                        leading = {
                            IconBadge(
                                iconKey = category.iconKey,
                                colorArgb = category.colorArgb,
                                size = 24.dp,
                            )
                        },
                    )
                }
            }

            FieldLabel("Cuenta")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.accounts.forEach { account ->
                    AppChip(
                        selected = state.accountId == account.id,
                        onClick = { viewModel.selectAccount(account.id) },
                        label = account.name,
                    )
                }
            }

            PesetasField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = "Nota",
                placeholder = "Opcional",
                helperText = "En meses más cortos se apuntará el último día del mes.",
            )

            AppButton(
                text = "Guardar",
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DayOfMonthDropdown(selected: Int, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PesetasPickerField(
            value = "Día $selected de cada mes",
            label = "Se repite",
            onClick = { expanded = true },
            trailingIcon = Icons.Filled.ArrowDropDown,
        )
        PesetasDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            (1..31).forEach { day ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Día $day",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onSelected(day)
                        expanded = false
                    },
                )
            }
        }
    }
}
