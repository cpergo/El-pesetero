package com.pesetas.data.files

import com.pesetas.platform.BinaryContent
import com.pesetas.platform.asBinaryContent

/**
 * Platform storage for receipt images. Implementations compress images to JPEG (quality 82,
 * maximum side 1080 px) and keep them in private application storage.
 */
interface ReceiptStorage {
    suspend fun store(content: BinaryContent): String?
    suspend fun read(path: String): ByteArray?
    suspend fun delete(path: String?)
}

class ReceiptImageStore(
    private val storage: ReceiptStorage,
) {
    suspend fun store(content: BinaryContent): String? = storage.store(content)

    suspend fun store(encodedImage: ByteArray): String? = storage.store(encodedImage.asBinaryContent())

    suspend fun read(path: String): ByteArray? = storage.read(path)

    suspend fun delete(path: String?) = storage.delete(path)
}
