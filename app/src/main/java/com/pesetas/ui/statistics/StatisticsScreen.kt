package com.pesetas.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.ui.components.BarGroup
import com.pesetas.ui.components.GroupedBarChart
import com.pesetas.ui.components.SingleBarChart
import com.pesetas.ui.theme.LocalPesetasColors
import com.pesetas.ui.util.formatMoney
import com.pesetas.ui.util.formatMonth
import com.pesetas.ui.util.formatMonthShort
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalPesetasColors.current
    var showRangePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RangeSelector(
            fromMonth = state.fromMonth,
            toMonth = state.toMonth,
            onPreset = viewModel::setPresetMonths,
            onCustom = { showRangePicker = true },
        )

        SummaryRow(
            income = state.totalIncome,
            expense = state.totalExpense,
            incomeColor = colors.income,
            expenseColor = colors.expense,
        )

        SectionCard(title = "Ingresos y gastos por mes") {
            if (state.monthlyTotals.isEmpty()) {
                EmptyChartText()
            } else {
                GroupedBarChart(
                    groups = state.monthlyTotals.map { totals ->
                        BarGroup(
                            label = formatMonthShort(totals.month),
                            values = listOf(
                                colors.income to totals.income,
                                colors.expense to totals.expense,
                            ),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
                LegendRow(incomeColor = colors.income, expenseColor = colors.expense)
            }
        }

        SectionCard(title = "Evolución por categoría") {
            CategorySelector(
                categories = state.categories.map { it.id to it.name },
                selectedName = state.selectedCategory?.name,
                onSelected = viewModel::selectCategory,
            )
            if (state.selectedCategoryId == null) {
                EmptyChartText("Elige una categoría para ver su evolución")
            } else if (state.categoryEvolution.all { it.income == 0.0 && it.expense == 0.0 }) {
                EmptyChartText("Sin datos para esta categoría en el rango")
            } else {
                val categoryColor = state.selectedCategory?.let { Color(it.colorArgb) }
                    ?: MaterialTheme.colorScheme.primary
                SingleBarChart(
                    bars = state.categoryEvolution.map { totals ->
                        formatMonthShort(totals.month) to (totals.income + totals.expense)
                    },
                    barColor = categoryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 12.dp),
                )
            }
        }

        Box(Modifier.height(16.dp))
    }

    if (showRangePicker) {
        val rangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = rangeState.selectedStartDateMillis
                    val end = rangeState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.setCustomRange(start.toYearMonth(), end.toYearMonth())
                    }
                    showRangePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) { Text("Cancelar") }
            },
        ) {
            DateRangePicker(state = rangeState, modifier = Modifier.height(460.dp))
        }
    }
}

private fun Long.toYearMonth(): YearMonth =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().let { YearMonth.from(it) }

@Composable
private fun RangeSelector(
    fromMonth: YearMonth,
    toMonth: YearMonth,
    onPreset: (Long) -> Unit,
    onCustom: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${formatMonth(fromMonth)}  –  ${formatMonth(toMonth)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = false, onClick = { onPreset(3) }, label = { Text("3 meses") })
            FilterChip(selected = false, onClick = { onPreset(6) }, label = { Text("6 meses") })
            FilterChip(selected = false, onClick = { onPreset(12) }, label = { Text("12 meses") })
        }
        OutlinedButton(onClick = onCustom) { Text("Rango personalizado") }
    }
}

@Composable
private fun SummaryRow(
    income: Double,
    expense: Double,
    incomeColor: Color,
    expenseColor: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryCard("Ingresos", income, incomeColor, Modifier.weight(1f))
        SummaryCard("Gastos", expense, expenseColor, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatMoney(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(
    categories: List<Pair<Long, String>>,
    selectedName: String?,
    onSelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedName ?: "Selecciona categoría")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LegendRow(incomeColor: Color, expenseColor: Color) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot(incomeColor, "Ingresos")
        LegendDot(expenseColor, "Gastos")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(0.dp),
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = color)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyChartText(text: String = "Aún no hay datos") {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 16.dp),
    )
}
