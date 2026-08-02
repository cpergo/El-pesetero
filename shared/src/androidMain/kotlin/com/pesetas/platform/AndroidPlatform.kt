package com.pesetas.platform

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pesetas.AppContainer
import com.pesetas.data.backup.DatabaseBackupStorage
import com.pesetas.data.files.ReceiptStorage
import com.pesetas.data.local.PesetasDatabase
import com.pesetas.data.local.PesetasDatabaseConstructor
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.ref.WeakReference
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

private const val PreferencesName = "pesetas_settings"
private const val MaxReceiptSide = 1080
private const val ReceiptJpegQuality = 82
private const val Authenticators =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

class AndroidPlatformSession(context: Context) {
    private val applicationContext = context.applicationContext
    private val documents = AndroidDocumentService()
    private var activityReference = WeakReference<FragmentActivity>(null)

    val container: AppContainer = createContainer()

    fun attach(activity: FragmentActivity) {
        activityReference = WeakReference(activity)
        documents.attach(activity)
    }

    private fun requireActivity(): FragmentActivity =
        activityReference.get() ?: error("No hay una pantalla Android activa")

    private fun createContainer(): AppContainer {
        val context = applicationContext
        val databaseFile = context.getDatabasePath(PesetasDatabase.NAME)
        val database = Room.databaseBuilder<PesetasDatabase>(
            context = context,
            name = databaseFile.absolutePath,
            factory = PesetasDatabaseConstructor::initialize,
        )
            .addCallback(PesetasDatabase.seedCallback)
            .addMigrations(PesetasDatabase.MIGRATION_1_2, PesetasDatabase.MIGRATION_2_3)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        val dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(PreferencesName)
        }
        return AppContainer(
            database = database,
            dataStore = dataStore,
            receiptStorage = AndroidReceiptStorage(context),
            databaseBackupStorage = AndroidDatabaseBackupStorage(context, database),
            documentService = documents,
            authenticator = AndroidDeviceAuthenticator(::requireActivity),
            externalLinks = AndroidExternalLinkService(context),
        )
    }
}

private class AndroidReceiptStorage(context: Context) : ReceiptStorage {
    private val filesDir = context.filesDir
    private val receiptsDir: File
        get() = File(filesDir, "receipts").apply { mkdirs() }

    override suspend fun store(encodedImage: ByteArray): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeScaled(encodedImage) ?: return@runCatching null
            val target = File(receiptsDir, "receipt_${UUID.randomUUID()}.jpg")
            target.outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, ReceiptJpegQuality, output))
            }
            target.absolutePath
        }.getOrNull()
    }

    override suspend fun read(path: String): ByteArray? = withContext(Dispatchers.IO) {
        resolve(path).takeIf { it.isFile }?.readBytes()
    }

    override suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        path?.let(::resolve)?.delete()
        Unit
    }

    private fun resolve(path: String): File =
        File(path).takeIf { it.isAbsolute } ?: File(receiptsDir, path)

    private fun decodeScaled(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ByteArrayInputStream(bytes).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MaxReceiptSide) {
            sampleSize *= 2
        }
        val decoded = ByteArrayInputStream(bytes).use {
            BitmapFactory.decodeStream(
                it,
                null,
                BitmapFactory.Options().apply { inSampleSize = sampleSize },
            )
        } ?: return null
        val largestSide = max(decoded.width, decoded.height)
        if (largestSide <= MaxReceiptSide) return decoded
        val scale = MaxReceiptSide.toFloat() / largestSide
        return Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true,
        )
    }
}

private class AndroidDatabaseBackupStorage(
    context: Context,
    private val database: PesetasDatabase,
) : DatabaseBackupStorage {
    private val databaseFile = context.getDatabasePath(PesetasDatabase.NAME)

    override suspend fun exportDatabase(): ByteArray = withContext(Dispatchers.IO) {
        checkpoint()
        databaseFile.readBytes()
    }

    override suspend fun importDatabase(bytes: ByteArray) = withContext(Dispatchers.IO) {
        databaseFile.parentFile?.mkdirs()
        val temporary = File(databaseFile.parentFile, "${PesetasDatabase.NAME}.importing")
        temporary.writeBytes(bytes)
        try {
            validateDatabase(temporary)
            checkpoint()
            replaceDatabase(temporary, databaseFile)
            database.close()
            deleteSidecars(databaseFile)
        } finally {
            temporary.delete()
            deleteSidecars(temporary)
        }
    }

    private suspend fun checkpoint() {
        database.useWriterConnection { connection ->
            connection.usePrepared("PRAGMA wal_checkpoint(FULL)") { statement -> statement.step() }
        }
    }

    private fun validateDatabase(file: File) {
        val header = ByteArray(16)
        val count = file.inputStream().use { it.read(header) }
        require(count == header.size && header.decodeToString().startsWith("SQLite format 3")) {
            "El archivo no es una base SQLite válida"
        }
        BundledSQLiteDriver().open(file.absolutePath).use { connection ->
            val quickCheck = connection.prepare("PRAGMA quick_check").use { statement ->
                if (statement.step()) statement.getText(0) else ""
            }
            require(quickCheck.equals("ok", ignoreCase = true)) { "La copia SQLite está dañada" }
            val version = connection.prepare("PRAGMA user_version").use { statement ->
                if (statement.step()) statement.getLong(0).toInt() else 0
            }
            require(version in 1..3) { "Versión de copia no compatible: $version" }
            val tables = buildSet {
                connection.prepare("SELECT name FROM sqlite_master WHERE type = 'table'").use { statement ->
                    while (statement.step()) add(statement.getText(0))
                }
            }
            require(tables.containsAll(setOf("accounts", "categories", "transactions"))) {
                "La copia no pertenece a El pesetero"
            }
        }
    }

    private fun deleteSidecars(file: File) {
        File(file.path + "-wal").delete()
        File(file.path + "-shm").delete()
    }

    private fun replaceDatabase(source: File, target: File) {
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }
}

private class AndroidDocumentService : DocumentService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var activityReference = WeakReference<FragmentActivity>(null)
    private var pendingSave: PendingSave? = null
    private var pendingOpen: CancellableContinuation<ByteArray?>? = null
    private var csvCreator: androidx.activity.result.ActivityResultLauncher<String>? = null
    private var databaseCreator: androidx.activity.result.ActivityResultLauncher<String>? = null
    private var documentPicker: androidx.activity.result.ActivityResultLauncher<Array<String>>? = null

    fun attach(activity: FragmentActivity) {
        activityReference = WeakReference(activity)
        csvCreator?.unregister()
        databaseCreator?.unregister()
        documentPicker?.unregister()
        csvCreator = activity.activityResultRegistry.register(
            "pesetas-create-csv",
            activity,
            ActivityResultContracts.CreateDocument("text/csv"),
            ::finishSave,
        )
        databaseCreator = activity.activityResultRegistry.register(
            "pesetas-create-database",
            activity,
            ActivityResultContracts.CreateDocument("application/octet-stream"),
            ::finishSave,
        )
        documentPicker = activity.activityResultRegistry.register(
            "pesetas-open-database",
            activity,
            ActivityResultContracts.OpenDocument(),
        ) picker@{ uri ->
            val continuation = pendingOpen ?: return@picker
            pendingOpen = null
            if (uri == null) {
                continuation.resume(null)
            } else {
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            activity.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: error("No se pudo abrir el archivo")
                        }
                    }.onSuccess(continuation::resume)
                        .onFailure(continuation::resumeWithException)
                }
            }
        }
    }

    override suspend fun saveFile(
        suggestedName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Boolean = suspendCancellableCoroutine { continuation ->
        check(pendingSave == null) { "Ya hay una exportación en curso" }
        pendingSave = PendingSave(bytes, continuation)
        continuation.invokeOnCancellation { pendingSave = null }
        val launcher = if (mimeType == "text/csv") csvCreator else databaseCreator
        if (launcher == null) {
            pendingSave = null
            continuation.resumeWithException(IllegalStateException("No hay una pantalla Android activa"))
        } else {
            launcher.launch(suggestedName)
        }
    }

    override suspend fun openFile(allowedMimeTypes: List<String>): ByteArray? =
        suspendCancellableCoroutine { continuation ->
            check(pendingOpen == null) { "Ya hay una importación en curso" }
            pendingOpen = continuation
            continuation.invokeOnCancellation { pendingOpen = null }
            val launcher = documentPicker
            if (launcher == null) {
                pendingOpen = null
                continuation.resumeWithException(IllegalStateException("No hay una pantalla Android activa"))
            } else {
                launcher.launch(allowedMimeTypes.toTypedArray())
            }
        }

    private fun finishSave(uri: Uri?) {
        val pending = pendingSave ?: return
        pendingSave = null
        if (uri == null) {
            pending.continuation.resume(false)
            return
        }
        scope.launch {
            val activity = activityReference.get()
            if (activity == null) {
                pending.continuation.resumeWithException(
                    IllegalStateException("No hay una pantalla Android activa"),
                )
                return@launch
            }
            runCatching {
                withContext(Dispatchers.IO) {
                    activity.contentResolver.openOutputStream(uri)?.use { it.write(pending.bytes) }
                        ?: error("No se pudo abrir el destino")
                }
                true
            }.onSuccess(pending.continuation::resume)
                .onFailure(pending.continuation::resumeWithException)
        }
    }

    private data class PendingSave(
        val bytes: ByteArray,
        val continuation: CancellableContinuation<Boolean>,
    )
}

private class AndroidDeviceAuthenticator(
    private val activityProvider: () -> FragmentActivity,
) : DeviceAuthenticator {
    override fun isAvailable(): Boolean = runCatching {
        BiometricManager.from(activityProvider()).canAuthenticate(Authenticators) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }.getOrDefault(false)

    override suspend fun authenticate(): Boolean = suspendCancellableCoroutine { continuation ->
        val activity = activityProvider()
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (continuation.isActive) continuation.resume(false)
                }
            },
        )
        continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Desbloquear El pesetero")
                .setSubtitle("Confirma tu identidad para continuar")
                .setAllowedAuthenticators(Authenticators)
                .build(),
        )
    }
}

private class AndroidExternalLinkService(
    private val context: Context,
) : ExternalLinkService {
    override fun open(url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
