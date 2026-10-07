package com.pesetas.platform

import okio.Buffer
import okio.BufferedSink

fun interface BinaryContent {
    suspend fun writeTo(sink: BufferedSink)
}

fun ByteArray.asBinaryContent(): BinaryContent = ByteArrayBinaryContent(this)

suspend fun BinaryContent.readByteArray(): ByteArray {
    if (this is ByteArrayBinaryContent) return bytes
    val buffer = Buffer()
    writeTo(buffer)
    return buffer.readByteArray()
}

private class ByteArrayBinaryContent(
    val bytes: ByteArray,
) : BinaryContent {
    override suspend fun writeTo(sink: BufferedSink) {
        sink.write(bytes)
    }
}
