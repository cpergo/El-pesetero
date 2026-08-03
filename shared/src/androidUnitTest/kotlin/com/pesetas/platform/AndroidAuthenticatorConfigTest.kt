package com.pesetas.platform

import androidx.biometric.BiometricManager
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AndroidAuthenticatorConfigTest {
    @Test
    fun acceptsTheSameBiometricClassesAsTheExistingAndroidRelease() {
        assertEquals(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            AndroidAuthenticators,
        )
    }

    @Test
    fun pendingRestoreReplacesTheDatabaseAndRemovesOldSQLiteSidecars() {
        val directory = Files.createTempDirectory("pesetas-restore-test").toFile()
        try {
            val database = File(directory, "pesetas.db").apply { writeBytes("old".encodeToByteArray()) }
            File(database.path + ".pending-import").writeBytes("restored".encodeToByteArray())
            File(database.path + "-wal").writeBytes("old wal".encodeToByteArray())
            File(database.path + "-shm").writeBytes("old shm".encodeToByteArray())
            File(database.path + "-journal").writeBytes("old journal".encodeToByteArray())

            applyPendingDatabaseImport(database)

            assertContentEquals("restored".encodeToByteArray(), database.readBytes())
            assertFalse(File(database.path + ".pending-import").exists())
            assertFalse(File(database.path + ".restore-pending").exists())
            assertFalse(File(database.path + "-wal").exists())
            assertFalse(File(database.path + "-shm").exists())
            assertFalse(File(database.path + "-journal").exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun restoreMarkerFinishesSidecarCleanupAfterAnInterruptedSwap() {
        val directory = Files.createTempDirectory("pesetas-restore-recovery-test").toFile()
        try {
            val database = File(directory, "pesetas.db").apply { writeBytes("restored".encodeToByteArray()) }
            File(database.path + ".restore-pending").writeBytes(byteArrayOf(1))
            File(database.path + "-wal").writeBytes("old wal".encodeToByteArray())
            File(database.path + "-journal").writeBytes("old journal".encodeToByteArray())

            applyPendingDatabaseImport(database)

            assertContentEquals("restored".encodeToByteArray(), database.readBytes())
            assertFalse(File(database.path + ".restore-pending").exists())
            assertFalse(File(database.path + "-wal").exists())
            assertFalse(File(database.path + "-journal").exists())
        } finally {
            directory.deleteRecursively()
        }
    }
}
