package com.pesetas.ui.transactions.editor

import androidx.compose.runtime.Composable

/** Native camera/gallery integration with a shared visual contract. */
@Composable
expect fun ReceiptSection(
    path: String?,
    onImageSelected: (ByteArray) -> Unit,
    onRemove: () -> Unit,
)
