package com.pesetas.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.BudgetStatus
import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.GeneratedRecurring
import com.pesetas.domain.model.TransactionType
import com.pesetas.ui.components.DonutChart
import com.pesetas.ui.components.DonutSlice
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.MonthNavigator
import com.pesetas.ui.components.AppButton
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.components.budgetSemaphoreColor
import com.pesetas.ui.theme.LocalPesetasColors
import com.pesetas.ui.util.formatDayHeader
import com.pesetas.ui.util.formatMoney
import com.pesetas.ui.util.formatSignedMoney

@Composable
fun HomeScreen(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState(modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        MonthNavigator(
            month = state.month,
            onPrevious = viewModel::showPreviousMonth,
            onNext = viewModel::showNextMonth,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (state.generatedRecurring.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            GeneratedRecurringNotice(
                items = state.generatedRecurring,
                onUndo = viewModel::undoGenerated,
                onDismiss = viewModel::dismissGenerated,
            )
        }
        if (state.budgetAlerts.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            BudgetAlertsBanner(alerts = state.budgetAlerts)
        }
        Spacer(Modifier.height(12.dp))
        BalanceHeader(
            balance = state.monthBalance,
            income = state.income,
            expense = state.expense,
        )
        Spacer(Modifier.weight(1f))
        ExpenseDonut(
            expense = state.expense,
            spending = state.expenseByCategory,
            hasExpenses = state.hasExpenses,
        )
        Spacer(Modifier.weight(1f))
        QuickActions(
            onAddIncome = onAddIncome,
            onAddExpense = onAddExpense,
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun BudgetAlertsBanner(alerts: List<BudgetStatus>) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            alerts.forEach { status ->
                val color = budgetSemaphoreColor(status.ratio)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (status.isOverLimit) {
                            "${status.category.name}: presupuesto superado"
                        } else {
                            "${status.category.name}: ${(status.ratio * 100).toInt()} % del presupuesto"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${formatMoney(status.spent)} / ${formatMoney(status.monthlyLimit)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneratedRecurringNotice(
    items: List<GeneratedRecurring>,
    onUndo: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalPesetasColors.current
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Añadido automáticamente",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar aviso",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            items.forEach { generated ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${generated.label} · " +
                            formatSignedMoney(
                                generated.amount,
                                positive = generated.type == TransactionType.INCOME,
                            ) +
                            " (${formatDayHeader(generated.date)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                    )
                    TextButton(onClick = { onUndo(generated.transactionId) }) {
                        Text(
                            text = "Deshacer",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.expense,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader(balance: Double, income: Double, expense: Double) {
    val colors = LocalPesetasColors.current
    val balanceColor = when {
        balance < 0 -> colors.expense
        balance > 0 -> colors.income
        else -> MaterialTheme.colorScheme.onBackground
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Balance",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatMoney(balance),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = balanceColor,
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.5f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryColumn(
                label = "Ingresos",
                amount = income,
                tint = colors.income,
                icon = Icons.AutoMirrored.Filled.TrendingUp,
            )
            VerticalDivider(
                modifier = Modifier.height(36.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            SummaryColumn(
                label = "Gastos",
                amount = expense,
                tint = colors.expense,
                icon = Icons.AutoMirrored.Filled.TrendingDown,
            )
        }
    }
}

@Composable
private fun RowScope.SummaryColumn(
    label: String,
    amount: Double,
    tint: Color,
    icon: ImageVector,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = formatMoney(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = tint,
        )
    }
}

@Composable
private fun ExpenseDonut(
    expense: Double,
    spending: List<CategorySpending>,
    hasExpenses: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DonutChart(
            slices = spending.map {
                DonutSlice(
                    value = it.total,
                    color = Color(it.category.colorArgb),
                    iconKey = it.category.iconKey,
                )
            },
            diameter = 300.dp,
            thickness = 52.dp,
            center = { selectedIndex ->
                val selected = selectedIndex?.let { spending.getOrNull(it) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    if (selected != null) {
                        Text(
                            text = selected.category.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(selected.category.colorArgb),
                            maxLines = 1,
                        )
                        Text(
                            text = formatMoney(selected.total),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        val percent = if (expense > 0) (selected.total / expense * 100).toInt() else 0
                        Text(
                            text = "$percent %",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "Gastos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatMoney(expense),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            },
        )
        if (!hasExpenses) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Aún no hay gastos este mes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickActions(onAddIncome: () -> Unit, onAddExpense: () -> Unit) {
    val colors = LocalPesetasColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AppButton(
            text = "Ingreso",
            onClick = onAddIncome,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            containerColor = colors.income,
            contentColor = Color.White,
            modifier = Modifier.weight(1f),
        )
        AppButton(
            text = "Gasto",
            onClick = onAddExpense,
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            containerColor = colors.expense,
            contentColor = Color.White,
            modifier = Modifier.weight(1f),
        )
    }
}
