package com.pesetas.ui.transactions.editor

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pesetas.platform.resolveIosReceiptPath
import com.pesetas.platform.topIosViewController
import com.pesetas.ui.theme.LocalPesetasColors
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UIImageView
import platform.UIKit.UIModalPresentationPopover
import platform.UIKit.UIViewContentMode
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun ReceiptSection(
    path: String?,
    onImageSelected: (ByteArray) -> Unit,
    onRemove: () -> Unit,
) {
    val picker = remember { IosReceiptPicker(onImageSelected) }
    picker.onSelected = onImageSelected

    if (path == null) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { picker.present(camera = true) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hacer foto")
            }
            OutlinedButton(
                onClick = { picker.present(camera = false) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Elegir imagen")
            }
        }
        return
    }

    val image = remember(path) {
        NSFileManager.defaultManager.contentsAtPath(resolveIosReceiptPath(path))?.let(::UIImage)
    }
    if (image == null) {
        Text(
            text = "No se pudo cargar la imagen del ticket",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
        return
    }

    var showFullImage by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        UIKitView(
            factory = {
                UIImageView().apply {
                    clipsToBounds = true
                    contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clickable { showFullImage = true },
            update = { it.image = image },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showFullImage = true }) { Text("Ver a tamaño completo") }
            TextButton(onClick = onRemove) {
                Text("Quitar", color = LocalPesetasColors.current.expense)
            }
        }
    }

    if (showFullImage) {
        Dialog(
            onDismissRequest = { showFullImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImage = false },
            ) {
                UIKitView(
                    factory = {
                        UIImageView().apply {
                            clipsToBounds = true
                            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                        }
                    },
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    update = { it.image = image },
                )
                IconButton(
                    onClick = { showFullImage = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosReceiptPicker(
    var onSelected: (ByteArray) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    private var controller: UIImagePickerController? = null

    fun present(camera: Boolean) {
        val presenter = topIosViewController() ?: return
        val source = if (camera && UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
            )
        ) {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        } else {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        }
        val picker = UIImagePickerController().apply {
            sourceType = source
            delegate = this@IosReceiptPicker
        }
        if (source == UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary) {
            picker.modalPresentationStyle = UIModalPresentationPopover
            picker.popoverPresentationController?.sourceView = presenter.view
            picker.popoverPresentationController?.sourceRect = presenter.view.bounds
        }
        controller = picker
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val bytes = image?.let { UIImageJPEGRepresentation(it, 1.0)?.toByteArray() }
        picker.dismissViewControllerAnimated(true, completion = null)
        controller = null
        if (bytes != null) onSelected(bytes)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        controller = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    if (length == 0uL) return ByteArray(0)
    return ByteArray(length.toInt()).also { result ->
        result.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}
