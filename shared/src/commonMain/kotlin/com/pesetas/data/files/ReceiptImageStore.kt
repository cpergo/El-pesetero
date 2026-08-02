package com.pesetas.data.files

/**
 * Platform storage for receipt images. Implementations compress images to JPEG (quality 82,
 * maximum side 1080 px) and keep them in private application storage.
 */
interface ReceiptStorage {
    suspend fun store(encodedImage: ByteArray): String?
    suspend fun read(path: String): ByteArray?
    suspend fun delete(path: String?)
}

class ReceiptImageStore(
    private val storage: ReceiptStorage,
) {
    suspend fun store(encodedImage: ByteArray): String? = storage.store(encodedImage)

    suspend fun read(path: String): ByteArray? = storage.read(path)

    suspend fun delete(path: String?) = storage.delete(path)
}
