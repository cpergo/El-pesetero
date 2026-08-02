package com.pesetas.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesetas.domain.model.TransactionDetails
import com.pesetas.domain.model.TransactionType
import com.pesetas.ui.components.IconBadge
import com.pesetas.ui.theme.LocalPesetasColors
import com.pesetas.ui.util.formatSignedMoney

@Composable
fun TransactionRow(
    details: TransactionDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalPesetasColors.current
    val transaction = details.transaction
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val title = when (transaction.type) {
        TransactionType.TRANSFER -> "Transferencia"
        else -> details.category?.name ?: "Sin categoría"
    }
    val subtitle = when (transaction.type) {
        TransactionType.TRANSFER ->
            "${details.account.name} → ${details.transferAccount?.name.orEmpty()}"
        else -> details.account.name
    }
    val amountColor = when {
        isTransfer -> MaterialTheme.colorScheme.onSurfaceVariant
        isIncome -> colors.income
        else -> colors.expense
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isTransfer) {
            IconBadge(iconKey = "currency_exchange", colorArgb = 0xFF8A6D3B.toInt(), size = 42.dp)
        } else {
            IconBadge(
                iconKey = details.category?.iconKey ?: "category",
                colorArgb = details.category?.colorArgb ?: 0xFF8A6D3B.toInt(),
                size = 42.dp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isTransfer) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (transaction.note.isNotBlank()) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = when {
                isTransfer -> formatSignedMoney(transaction.amount, positive = true).removePrefix("+")
                else -> formatSignedMoney(transaction.amount, positive = isIncome)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = amountColor,
        )
    }
}
