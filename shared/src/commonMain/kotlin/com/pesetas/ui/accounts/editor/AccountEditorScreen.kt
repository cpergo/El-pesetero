package com.pesetas.ui.accounts.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.pesetas.ui.components.ColorPickerGrid
import com.pesetas.ui.components.FieldLabel
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.IconPickerDialog
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppButton
import com.pesetas.ui.components.PesetasDropdownMenu
import com.pesetas.ui.components.PesetasPickerField
import com.pesetas.ui.components.AppOutlinedButton
import com.pesetas.ui.util.CurrencyCatalog
import com.pesetas.ui.util.currencySymbol
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AccountEditorScreen(
    onBack: () -> Unit,
    viewModel: AccountEditorViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showIconPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.finished.collectLatest { onBack() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PesetasTopBar(
                title = if (state.isEditing) "Editar cuenta" else "Nueva cuenta",
                onBack = onBack,
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconBadge(
                iconKey = state.iconKey,
                colorArgb = state.colorArgb,
                size = 72.dp,
                modifier = Modifier.padding(top = 8.dp),
            )

            PesetasField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = "Nombre",
            )

            PesetasField(
                value = state.initialBalanceText,
                onValueChange = viewModel::setInitialBalance,
                label = "Saldo inicial",
                suffix = currencySymbol(state.currency),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            CurrencyDropdown(
                selected = state.currency,
                onSelected = viewModel::setCurrency,
            )

            AppOutlinedButton(
                text = "Elegir icono",
                onClick = { showIconPicker = true },
                icon = Icons.Filled.Edit,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                FieldLabel("Color")
            }
            ColorPickerGrid(
                selectedColor = state.colorArgb,
                onSelect = viewModel::setColor,
            )

            AppButton(
                text = "Guardar",
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showIconPicker) {
        IconPickerDialog(
            selectedKey = state.iconKey,
            onSelect = {
                viewModel.setIcon(it)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false },
        )
    }
}

@Composable
private fun CurrencyDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        PesetasPickerField(
            value = "$selected · ${CurrencyCatalog.nameFor(selected)}",
            label = "Divisa",
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
