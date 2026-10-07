package com.pesetas.ui.transactions.editor

import androidx.compose.runtime.Composable
import com.pesetas.platform.BinaryContent

@Composable
expect fun ReceiptSection(
    path: String?,
    onImageSelected: (BinaryContent) -> Unit,
    onRemove: () -> Unit,
)
