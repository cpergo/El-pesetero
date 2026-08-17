package com.pesetas.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.domain.model.SavingsGoal
import com.pesetas.ui.components.BudgetProgressBar
import com.pesetas.ui.components.AppFab
import com.pesetas.ui.components.EmptyState
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.components.LoadingState
import com.pesetas.ui.components.PesetasField
import com.pesetas.ui.components.PesetasTopBar
import com.pesetas.ui.components.AppCard
import com.pesetas.ui.components.AppDialog
import com.pesetas.ui.components.AppOutlinedButton
import com.pesetas.ui.components.budgetSemaphoreColor
import com.pesetas.ui.util.formatDate
import com.pesetas.ui.util.formatMoney

@Composable
fun SavingsGoalsScreen(
    onBack: () -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (Long) -> Unit,
    viewModel: SavingsGoalsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var contributingTo by remember { mutableStateOf<SavingsGoal?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { PesetasTopBar(title = "Objetivos", onBack = onBack) },
        floatingActionButton = {
            AppFab(
                onClick = onAddGoal,
                icon = Icons.Filled.Add,
                contentDescription = "Añadir objetivo",
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.goals.isEmpty() -> EmptyState(
                icon = Icons.Filled.Savings,
                title = "Sin objetivos de ahorro",
                message = "Crea tu primera meta: un viaje, un fondo de emergencia, " +
                    "un capricho… y ve apartando poco a poco.",
                modifier = Modifier.padding(padding).padding(top = 48.dp),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onEditGoal(goal.id) },
                        onContribute = { contributingTo = goal },
                        onDelete = { viewModel.delete(goal) },
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    contributingTo?.let { goal ->
        ContributionDialog(
            goal = goal,
            onSave = { amount ->
                viewModel.contribute(goal.id, amount)
                contributingTo = null
            },
            onDismiss = { contributingTo = null },
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    onClick: () -> Unit,
    onContribute: () -> Unit,
    onDelete: () -> Unit,
) {
    AppCard(
        containerColor = if (goal.isCompleted) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconBadge(iconKey = goal.iconKey, colorArgb = goal.colorArgb, size = 40.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (goal.deadline != null) {
                        Text(
                            text = "Antes del ${formatDate(goal.deadline)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eliminar objetivo",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Text(
                text = "${formatMoney(goal.currentAmount)} / ${formatMoney(goal.targetAmount)}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End,
                color = if (goal.isCompleted) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    budgetSemaphoreColor(goal.ratio)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (goal.isCompleted) {
                CompletedBanner()
            } else {
                BudgetProgressBar(spent = goal.currentAmount, limit = goal.targetAmount)
                AppOutlinedButton(
                    text = "Aportar",
                    onClick = onContribute,
                    icon = Icons.Filled.Add,
                )
            }
        }
    }
}

@Composable
private fun CompletedBanner() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = "¡Conseguido!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = "Objetivo completado",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContributionDialog(
    goal: SavingsGoal,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    val parsed = amountText.replace(',', '.').toDoubleOrNull()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    AppDialog(
        title = "Aportar a ${goal.name}",
        onDismiss = onDismiss,
        confirmText = "Aportar",
        onConfirm = { parsed?.let(onSave) },
        confirmEnabled = parsed != null && parsed > 0,
    ) {
        PesetasField(
            value = amountText,
            onValueChange = { text ->
                amountText = text.filter { it.isDigit() || it == ',' || it == '.' }
            },
            label = "Cantidad",
            suffix = "€",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            helperText = "Te faltan ${formatMoney(remaining)} para la meta.",
        )
    }
}
