package com.pesetas.ui.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
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
import com.pesetas.domain.model.Account
import com.pesetas.util.toEpochDay
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasDatePickerDialog
import com.pesetas.ui.components.PesetasDropdownMenu
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasPickerField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppButton
import com.pesetas.ui.theme.LocalPesetasColors
import com.pesetas.ui.util.formatDate
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TransferScreen(
    onBack: () -> Unit,
    viewModel: TransferViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.finished.collectLatest { onBack() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PesetasTopBar(
                title = if (state.isEditing) "Editar transferencia" else "Nueva transferencia",
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
            PesetasField(
                value = state.amountText,
                onValueChange = viewModel::setAmount,
                label = "Importe",
                suffix = "€",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            AccountDropdown(
                label = "Desde",
                selectedId = state.fromAccountId,
                accounts = state.accounts,
                onSelected = viewModel::selectFrom,
            )
            Icon(
                imageVector = Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp),
            )
            AccountDropdown(
                label = "Hacia",
                selectedId = state.toAccountId,
                accounts = state.accounts,
                onSelected = viewModel::selectTo,
            )

            if (state.fromAccountId == state.toAccountId && state.accounts.size > 1) {
                Text(
                    text = "Elige dos cuentas distintas",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalPesetasColors.current.expense,
                )
            }

            PesetasPickerField(
                value = formatDate(state.date),
                label = "Fecha",
                onClick = { showDatePicker = true },
                trailingIcon = Icons.Filled.CalendarMonth,
            )

            PesetasField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = "Nota",
                placeholder = "Opcional",
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
}

@Composable
private fun AccountDropdown(
    label: String,
    selectedId: Long?,
    accounts: List<Account>,
    onSelected: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = accounts.firstOrNull { it.id == selectedId }?.name.orEmpty()
    Box {
        PesetasPickerField(
            value = selectedName,
            label = label,
            onClick = { expanded = true },
            trailingIcon = Icons.Filled.ArrowDropDown,
        )
        PesetasDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        onSelected(account.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
