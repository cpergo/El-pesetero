package com.pesetas.platform

interface DocumentService {
    suspend fun saveFile(suggestedName: String, mimeType: String, bytes: ByteArray): Boolean
    suspend fun openFile(allowedMimeTypes: List<String>): ByteArray?
}

interface DeviceAuthenticator {
    fun isAvailable(): Boolean
    suspend fun authenticate(): Boolean
}

interface ExternalLinkService {
    fun open(url: String)
}
