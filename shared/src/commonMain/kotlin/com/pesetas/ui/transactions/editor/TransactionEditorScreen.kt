package com.pesetas.ui.transactions.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.TransactionType
import com.pesetas.ui.components.FieldLabel
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasDatePickerDialog
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasPickerField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppButton
import com.pesetas.ui.components.AppChip
import com.pesetas.ui.components.AppDialog
import com.pesetas.ui.components.AppSegmented
import com.pesetas.ui.util.currencySymbol
import com.pesetas.ui.util.formatDate
import com.pesetas.util.toEpochDay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionEditorScreen(
    onBack: () -> Unit,
    viewModel: TransactionEditorViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.finished.collectLatest { onBack() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PesetasTopBar(
                title = if (state.isEditing) "Editar movimiento" else "Nuevo movimiento",
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
                suffix = currencySymbol(
                    state.accounts.firstOrNull { it.id == state.accountId }?.currency ?: "EUR",
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            PesetasPickerField(
                value = formatDate(state.date),
                label = "Fecha",
                onClick = { showDatePicker = true },
                trailingIcon = Icons.Filled.CalendarMonth,
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
            )

            FieldLabel("Etiquetas")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.tags.forEach { tag ->
                    AppChip(
                        selected = tag.id in state.selectedTagIds,
                        onClick = { viewModel.toggleTag(tag.id) },
                        label = tag.name,
                        leading = { TagDot(colorArgb = tag.colorArgb) },
                    )
                }
                AppChip(
                    selected = false,
                    onClick = { showNewTagDialog = true },
                    label = "+ Nueva etiqueta",
                )
            }

            FieldLabel("Ticket")
            ReceiptSection(
                path = state.receiptImagePath,
                onImageSelected = viewModel::attachReceipt,
                onRemove = viewModel::removeReceipt,
            )

            AppButton(
                text = "Guardar",
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showDatePicker) {
        PesetasDatePickerDialog(
            initialDate = state.date,
            onConfirm = { date -> viewModel.setDateEpochDay(date.toEpochDay()) },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showNewTagDialog) {
        NewTagDialog(
            onCreate = { name ->
                viewModel.createTag(name)
                showNewTagDialog = false
            },
            onDismiss = { showNewTagDialog = false },
        )
    }
}

@Composable
private fun TagDot(colorArgb: Int) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(Color(colorArgb))
            .border(1.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape),
    )
}

@Composable
private fun NewTagDialog(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AppDialog(
        title = "Nueva etiqueta",
        onDismiss = onDismiss,
        confirmText = "Crear",
        onConfirm = { onCreate(name) },
        confirmEnabled = name.isNotBlank(),
    ) {
        PesetasField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre",
            placeholder = "Viaje a Madrid, regalos…",
            helperText = "Las etiquetas cruzan categorías: sirven para agrupar " +
                "gastos de un mismo plan o proyecto.",
        )
    }
}
