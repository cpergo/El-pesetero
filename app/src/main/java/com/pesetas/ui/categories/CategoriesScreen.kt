package com.pesetas.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import com.pesetas.ui.components.EmptyState
import com.pesetas.ui.components.IconBadge
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    onAddCategory: (CategoryType) -> Unit,
    onEditCategory: (Long) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddCategory(state.type) }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir categoría")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            val tabs = listOf(CategoryType.EXPENSE to "Gastos", CategoryType.INCOME to "Ingresos")
            TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == state.type }) {
                tabs.forEach { (type, label) ->
                    Tab(
                        selected = state.type == type,
                        onClick = { viewModel.selectType(type) },
                        text = { Text(label) },
                    )
                }
            }
            if (state.categories.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Category,
                    title = "Sin categorías",
                    message = "Crea tu primera categoría con el botón +",
                    modifier = Modifier.padding(top = 48.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        CategoryManageRow(
                            category = category,
                            onClick = { onEditCategory(category.id) },
                            onMoveUp = { viewModel.moveUp(category) },
                            onMoveDown = { viewModel.moveDown(category) },
                            onDelete = { viewModel.delete(category) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryManageRow(
    category: Category,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconBadge(iconKey = category.iconKey, colorArgb = category.colorArgb, size = 40.dp)
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        IconButton(onClick = onMoveUp) {
            Icon(Icons.Filled.ArrowUpward, contentDescription = "Subir")
        }
        IconButton(onClick = onMoveDown) {
            Icon(Icons.Filled.ArrowDownward, contentDescription = "Bajar")
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
