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
import androidx.compose.material3.SnackbarHostState
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
import com.pesetas.domain.model.BudgetStatus
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import com.pesetas.ui.components.AppFab
import com.pesetas.ui.components.BudgetProgressBar
import com.pesetas.ui.components.EmptyState
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.PesetasSnackbarHost
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppTabs
import com.pesetas.ui.components.budgetSemaphoreColor
import com.pesetas.ui.util.formatMoney
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { PesetasTopBar(title = "Categorías", onBack = onBack) },
        floatingActionButton = {
            AppFab(
                onClick = { onAddCategory(state.type) },
                icon = Icons.Filled.Add,
                contentDescription = "Añadir categoría",
            )
        },
        snackbarHost = { PesetasSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            val tabs = listOf(CategoryType.EXPENSE to "Gastos", CategoryType.INCOME to "Ingresos")
            AppTabs(
                tabs = tabs.map { it.second },
                selectedIndex = tabs.indexOfFirst { it.first == state.type },
                onSelect = { index -> viewModel.selectType(tabs[index].first) },
            )
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
                            budgetStatus = state.budgetStatusByCategoryId[category.id],
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
    budgetStatus: BudgetStatus?,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        if (budgetStatus != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 12.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BudgetProgressBar(
                    spent = budgetStatus.spent,
                    limit = budgetStatus.monthlyLimit,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${formatMoney(budgetStatus.spent)} / ${formatMoney(budgetStatus.monthlyLimit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = budgetSemaphoreColor(budgetStatus.ratio),
                )
            }
        }
    }
}
