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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.ui.components.BarGroup
import androidx.compose.ui.draw.clip
import com.pesetas.ui.components.LegendDot
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.GroupedBarChart
import com.pesetas.ui.components.PesetasDateRangePickerDialog
import com.pesetas.ui.components.PesetasDropdownMenu
import com.pesetas.ui.components.SingleBarChart
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.components.AppChip
import com.pesetas.ui.components.AppOutlinedButton
import com.pesetas.ui.theme.LocalPesetasColors
import com.pesetas.ui.util.formatMoney
import com.pesetas.ui.util.formatMonth
import com.pesetas.ui.util.formatMonthShort
import com.pesetas.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel,
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

        SectionCard(title = "Gasto por etiqueta") {
            if (state.tagTotals.isEmpty()) {
                EmptyChartText("Añade etiquetas a tus movimientos para agrupar gastos entre categorías")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.tagTotals.forEach { spending ->
                        TagTotalRow(
                            spending = spending,
                            selected = state.selectedTagId == spending.tag.id,
                            onClick = { viewModel.selectTag(spending.tag.id) },
                        )
                        if (state.selectedTagId == spending.tag.id) {
                            TagBreakdown(breakdown = state.tagBreakdown)
                        }
                    }
                }
            }
        }

        Box(Modifier.height(16.dp))
    }

    if (showRangePicker) {
        PesetasDateRangePickerDialog(
            onConfirm = { start, end ->
                viewModel.setCustomRange(yearMonthOf(start), yearMonthOf(end))
            },
            onDismiss = { showRangePicker = false },
        )
    }
}

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
            AppChip(selected = false, onClick = { onPreset(3) }, label = "3 meses")
            AppChip(selected = false, onClick = { onPreset(6) }, label = "6 meses")
            AppChip(selected = false, onClick = { onPreset(12) }, label = "12 meses")
        }
        AppOutlinedButton(text = "Rango personalizado", onClick = onCustom)
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
    AppCard(modifier = modifier) {
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
    AppCard(modifier = Modifier.fillMaxWidth()) {
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
        AppChip(
            selected = selectedName != null,
            onClick = { expanded = true },
            label = selectedName ?: "Selecciona categoría",
            leading = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        PesetasDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { (id, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
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
private fun TagTotalRow(
    spending: com.pesetas.domain.model.TagSpending,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .then(
                if (selected) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LegendDot(Color(spending.tag.colorArgb), spending.tag.name)
        Box(Modifier.weight(1f))
        Text(
            text = formatMoney(spending.total),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TagBreakdown(breakdown: List<com.pesetas.domain.model.CategorySpending>) {
    Column(
        modifier = Modifier.padding(start = 26.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        breakdown.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconBadge(
                    iconKey = item.category.iconKey,
                    colorArgb = item.category.colorArgb,
                    size = 22.dp,
                )
                Text(
                    text = item.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatMoney(item.total),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
