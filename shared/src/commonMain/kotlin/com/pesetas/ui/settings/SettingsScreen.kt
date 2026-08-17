package com.pesetas.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.AppTheme
import com.pesetas.ui.components.PesetasSnackbarHost
import com.pesetas.ui.components.PesetasSwitch
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.components.AppChip
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onManageCategories: () -> Unit,
    onManageAccounts: () -> Unit,
    onManageBudgets: () -> Unit,
    onManageCurrencies: () -> Unit,
    onManageGoals: () -> Unit,
    onManageRecurring: () -> Unit,
    onAbout: () -> Unit,
    onRestartRequired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.Message -> snackbarHostState.showSnackbar(event.text)
                SettingsEvent.RestartRequired -> onRestartRequired()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { PesetasSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            SettingsSection(title = "Apariencia") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip("Claro", state.theme == AppTheme.LIGHT) { viewModel.setTheme(AppTheme.LIGHT) }
                    ThemeChip("Oscuro", state.theme == AppTheme.DARK) { viewModel.setTheme(AppTheme.DARK) }
                    ThemeChip("Auto", state.theme == AppTheme.SYSTEM) { viewModel.setTheme(AppTheme.SYSTEM) }
                }
            }

            SettingsSection(title = "Seguridad") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloqueo con huella o PIN",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Usa el bloqueo de tu dispositivo al abrir la app",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    PesetasSwitch(
                        checked = state.appLockEnabled,
                        onCheckedChange = viewModel::setAppLock,
                    )
                }
            }

            SettingsSection(title = "Gestión") {
                SettingsItem(icon = Icons.Filled.Category, title = "Categorías", onClick = onManageCategories)
                HorizontalDivider()
                SettingsItem(icon = Icons.Filled.Wallet, title = "Cuentas", onClick = onManageAccounts)
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.Savings,
                    title = "Presupuestos",
                    subtitle = "Límite mensual de gasto por categoría",
                    onClick = onManageBudgets,
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.EventRepeat,
                    title = "Movimientos recurrentes",
                    subtitle = "Alquiler, suscripciones, nómina…",
                    onClick = onManageRecurring,
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.Flag,
                    title = "Objetivos de ahorro",
                    subtitle = "Metas con aportaciones manuales",
                    onClick = onManageGoals,
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.CurrencyExchange,
                    title = "Divisas",
                    subtitle = "Divisa principal y tasas de conversión manuales",
                    onClick = onManageCurrencies,
                )
            }

            SettingsSection(title = "Copias de seguridad") {
                SettingsItem(
                    icon = Icons.Filled.TableChart,
                    title = "Exportar a CSV",
                    subtitle = "Guarda tus movimientos en una hoja de cálculo",
                    onClick = viewModel::exportCsv,
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.FileDownload,
                    title = "Exportar copia completa",
                    subtitle = "Guarda toda la base de datos en un archivo",
                    onClick = viewModel::exportDatabase,
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.FileUpload,
                    title = "Importar copia",
                    subtitle = "Restaura una copia guardada anteriormente",
                    onClick = viewModel::importDatabase,
                )
            }

            SettingsSection(title = "Información") {
                SettingsItem(icon = Icons.Filled.Info, title = "Acerca de El pesetero", onClick = onAbout)
            }
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AppChip(selected = selected, onClick = onClick, label = label)
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
