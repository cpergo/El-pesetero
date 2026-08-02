package com.pesetas.ui.categories.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.CategoryType
import com.pesetas.ui.components.ColorPickerGrid
import com.pesetas.ui.components.FieldLabel
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.IconPickerDialog
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppButton
import com.pesetas.ui.components.AppOutlinedButton
import com.pesetas.ui.components.AppSegmented
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CategoryEditorScreen(
    onBack: () -> Unit,
    viewModel: CategoryEditorViewModel,
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
                title = if (state.isEditing) "Editar categoría" else "Nueva categoría",
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

            AppSegmented(
                options = listOf(
                    CategoryType.EXPENSE to "Gasto",
                    CategoryType.INCOME to "Ingreso",
                ),
                selected = state.type,
                onSelect = viewModel::setType,
                modifier = Modifier.fillMaxWidth(),
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
