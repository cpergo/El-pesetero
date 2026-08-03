package com.pesetas

import com.pesetas.platform.applyPendingDatabaseImport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import platform.Foundation.NSFileManager.Companion.defaultManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

@OptIn(ExperimentalForeignApi::class)
class IosRestoreTest {
    @Test
    fun pendingRestoreReplacesTheDatabaseAndCleansOldSQLiteSidecars() {
        withTemporaryDirectory { directory ->
            val database = "$directory/pesetas.db"
            writeText(database, "old")
            writeText("$database.pending-import", "restored")
            writeText("$database-wal", "old wal")
            writeText("$database-shm", "old shm")
            writeText("$database-journal", "old journal")

            applyPendingDatabaseImport(database)

            assertEquals("restored", readText(database))
            assertFalse(defaultManager.fileExistsAtPath("$database.pending-import"))
            assertFalse(defaultManager.fileExistsAtPath("$database.restore-pending"))
            assertFalse(defaultManager.fileExistsAtPath("$database-wal"))
            assertFalse(defaultManager.fileExistsAtPath("$database-shm"))
            assertFalse(defaultManager.fileExistsAtPath("$database-journal"))
        }
    }

    @Test
    fun restoreMarkerCompletesCleanupAfterAnInterruptedSwap() {
        withTemporaryDirectory { directory ->
            val database = "$directory/pesetas.db"
            writeText(database, "restored")
            writeText("$database.restore-pending", "1")
            writeText("$database-wal", "old wal")
            writeText("$database-journal", "old journal")

            applyPendingDatabaseImport(database)

            assertEquals("restored", readText(database))
            assertFalse(defaultManager.fileExistsAtPath("$database.restore-pending"))
            assertFalse(defaultManager.fileExistsAtPath("$database-wal"))
            assertFalse(defaultManager.fileExistsAtPath("$database-journal"))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun withTemporaryDirectory(block: (String) -> Unit) {
    val directory = "${NSTemporaryDirectory()}pesetas-restore-${NSUUID.UUID().UUIDString}"
    check(
        defaultManager.createDirectoryAtPath(
            path = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        ),
    )
    try {
        block(directory)
    } finally {
        defaultManager.removeItemAtPath(directory, error = null)
    }
}

private fun writeText(path: String, value: String) {
    val sink = FileSystem.SYSTEM.sink(path.toPath()).buffer()
    try {
        sink.writeUtf8(value)
    } finally {
        sink.close()
    }
}

private fun readText(path: String): String {
    val source = FileSystem.SYSTEM.source(path.toPath()).buffer()
    return try {
        source.readUtf8()
    } finally {
        source.close()
    }
}
