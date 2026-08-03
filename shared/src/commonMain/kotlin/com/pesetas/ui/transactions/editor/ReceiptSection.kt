package com.pesetas.ui.transactions.editor

import androidx.compose.runtime.Composable
import com.pesetas.platform.BinaryContent

/** Native camera/gallery integration with a shared visual contract. */
@Composable
expect fun ReceiptSection(
    path: String?,
    onImageSelected: (BinaryContent) -> Unit,
    onRemove: () -> Unit,
)
