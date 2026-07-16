package com.pesetas.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pesetas.ui.theme.LocalPesetasColors

@Composable
fun budgetSemaphoreColor(ratio: Double): Color {
    val colors = LocalPesetasColors.current
    return when {
        ratio >= 1.0 -> colors.expense
        ratio >= 0.8 -> MaterialTheme.colorScheme.primary
        else -> colors.income
    }
}

@Composable
fun BudgetProgressBar(
    spent: Double,
    limit: Double,
    modifier: Modifier = Modifier,
) {
    val ratio = if (limit > 0) spent / limit else 0.0
    LinearProgressIndicator(
        progress = { ratio.coerceIn(0.0, 1.0).toFloat() },
        color = budgetSemaphoreColor(ratio),
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
    )
}
