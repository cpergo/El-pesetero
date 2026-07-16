package com.pesetas.data.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

private const val MaxSidePx = 1080
private const val JpegQuality = 82

@Singleton
class ReceiptImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val receiptsDir: File
        get() = File(context.filesDir, "receipts").apply { mkdirs() }

    private val captureDir: File
        get() = File(context.cacheDir, "capture").apply { mkdirs() }

    fun newCaptureTarget(): Pair<Uri, String> {
        val file = File(captureDir, "capture_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return uri to file.absolutePath
    }

    suspend fun storeCapture(tempPath: String): String? = withContext(Dispatchers.IO) {
        val temp = File(tempPath)
        val stored = runCatching { compressAndSave { temp.inputStream() } }.getOrNull()
        temp.delete()
        stored
    }

    suspend fun storeFromUri(source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            compressAndSave { context.contentResolver.openInputStream(source) }
        }.getOrNull()
    }

    suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        if (path != null) File(path).delete()
    }

    fun discardCapture(tempPath: String) {
        File(tempPath).delete()
    }

    private fun compressAndSave(open: () -> InputStream?): String? {
        val bitmap = decodeScaled(open) ?: return null
        val target = File(receiptsDir, "receipt_${UUID.randomUUID()}.jpg")
        target.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JpegQuality, output)
        }
        return target.absolutePath
    }

    private fun decodeScaled(open: () -> InputStream?): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open()?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MaxSidePx) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = open()?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        val largestSide = max(decoded.width, decoded.height)
        if (largestSide <= MaxSidePx) return decoded
        val scale = MaxSidePx.toFloat() / largestSide
        return Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true,
        )
    }
}
