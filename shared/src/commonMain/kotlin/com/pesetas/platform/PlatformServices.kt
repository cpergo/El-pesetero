package com.pesetas.platform

interface DocumentService {
    suspend fun saveFile(
        suggestedName: String,
        mimeType: String,
        content: BinaryContent,
    ): Boolean

    suspend fun openFile(allowedMimeTypes: List<String>): BinaryContent?
}

interface DeviceAuthenticator {
    fun isAvailable(): Boolean
    suspend fun authenticate(): Boolean
}

interface ExternalLinkService {
    fun open(url: String)
}
