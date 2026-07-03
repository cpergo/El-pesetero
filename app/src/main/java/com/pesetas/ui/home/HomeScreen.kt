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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.CategorySpending
import com.pesetas.ui.components.DonutChart
import com.pesetas.ui.components.DonutSlice
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.MonthNavigator
import com.pesetas.ui.theme.LocalPesetasColors
import com.pesetas.ui.util.formatMoney

@Composable
fun HomeScreen(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
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
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onAddIncome,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.income,
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ingreso")
        }
        Button(
            onClick = onAddExpense,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.expense,
                contentColor = Color.White,
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Gasto")
        }
    }
}
