package com.pesetas.ui.accounts

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.AccountBalance
import com.pesetas.ui.components.AppFab
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.PesetasSnackbarHost
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.util.formatDate
import com.pesetas.ui.util.formatMoney
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (Long) -> Unit,
    onNewTransfer: () -> Unit,
    viewModel: AccountsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collectLatest { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            PesetasTopBar(
                title = "Cuentas",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onNewTransfer) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Nueva transferencia")
                    }
                },
            )
        },
        floatingActionButton = {
            AppFab(
                onClick = onAddAccount,
                icon = Icons.Filled.Add,
                contentDescription = "Añadir cuenta",
            )
        },
        snackbarHost = { PesetasSnackbarHost(snackbarHostState) },
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Saldo total",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = buildString {
                                if (state.combinedBalance.isApproximate) append("≈ ")
                                append(
                                    formatMoney(
                                        state.combinedBalance.total,
                                        state.combinedBalance.mainCurrency,
                                    ),
                                )
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        if (state.combinedBalance.isApproximate) {
                            Text(
                                text = when {
                                    state.combinedBalance.hasMissingRates ->
                                        "Aprox. · faltan tasas por definir en Ajustes → Divisas"
                                    state.combinedBalance.oldestRateDate != null ->
                                        "Aprox. con tasas manuales del " +
                                            formatDate(state.combinedBalance.oldestRateDate!!)
                                    else -> "Aprox. con tasas manuales"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
            items(state.accounts, key = { it.account.id }) { item ->
                AccountRow(
                    item = item,
                    onClick = { onEditAccount(item.account.id) },
                    onMoveUp = { viewModel.moveUp(item.account) },
                    onMoveDown = { viewModel.moveDown(item.account) },
                    onDelete = { viewModel.delete(item.account) },
                )
            }
        }
    }
}

@Composable
private fun AccountRow(
    item: AccountBalance,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconBadge(iconKey = item.account.iconKey, colorArgb = item.account.colorArgb, size = 40.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            ) {
                Text(
                    text = item.account.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatMoney(item.balance, item.account.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
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
}
