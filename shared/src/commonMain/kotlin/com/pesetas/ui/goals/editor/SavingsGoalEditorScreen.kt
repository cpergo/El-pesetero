package com.pesetas.ui.goals.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.ui.components.ColorPickerGrid
import com.pesetas.ui.components.FieldLabel
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.IconPickerDialog
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasDatePickerDialog
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasPickerField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppButton
import com.pesetas.ui.components.AppOutlinedButton
import com.pesetas.ui.util.formatDate
import kotlinx.coroutines.flow.collectLatest
import com.pesetas.util.*

@Composable
fun SavingsGoalEditorScreen(
    onBack: () -> Unit,
    viewModel: SavingsGoalEditorViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showIconPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.finished.collectLatest { onBack() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PesetasTopBar(
                title = if (state.isEditing) "Editar objetivo" else "Nuevo objetivo",
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
                placeholder = "Viaje, fondo de emergencia…",
            )

            PesetasField(
                value = state.targetAmountText,
                onValueChange = viewModel::setTargetAmount,
                label = "Meta",
                suffix = "€",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                PesetasPickerField(
                    value = state.deadline?.let { formatDate(it) } ?: "Sin fecha límite",
                    label = "Fecha límite (opcional)",
                    onClick = { showDatePicker = true },
                    trailingIcon = Icons.Filled.CalendarMonth,
                )
                if (state.deadline != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "QUITAR FECHA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.setDeadline(null) }
                                .padding(4.dp),
                        )
                    }
                }
            }

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

    if (showDatePicker) {
        PesetasDatePickerDialog(
            initialDate = state.deadline ?: currentDate().plusMonths(6),
            onConfirm = viewModel::setDeadline,
            onDismiss = { showDatePicker = false },
        )
    }
}
