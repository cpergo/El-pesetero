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
import java.io.File
import java.io.InputStream
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.Source
import okio.buffer
import okio.sink
import okio.source

private const val PreferencesName = "pesetas_settings"
private const val MaxReceiptSide = 1080
private const val ReceiptJpegQuality = 82
private const val PendingImportSuffix = ".pending-import"
private const val RestoreMarkerSuffix = ".restore-pending"
internal const val AndroidAuthenticators =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

class AndroidPlatformSession(context: Context) {
    private val applicationContext = context.applicationContext
    private val documents = AndroidDocumentService()
    private var activityReference = WeakReference<FragmentActivity>(null)

    init {
        applyPendingDatabaseImport(applicationContext)
    }

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
    private val cacheDir = context.cacheDir
    private val receiptsDir: File
        get() = File(filesDir, "receipts").apply { mkdirs() }

    override suspend fun store(content: BinaryContent): String? = withContext(Dispatchers.IO) {
        runCatching {
            val staged = File.createTempFile("receipt_", ".source", cacheDir)
            try {
                staged.sink().buffer().use { sink -> content.writeTo(sink) }
                val bitmap = decodeScaled { staged.inputStream() } ?: return@runCatching null
                val target = File(receiptsDir, "receipt_${UUID.randomUUID()}.jpg")
                try {
                    target.outputStream().use { output ->
                        check(bitmap.compress(Bitmap.CompressFormat.JPEG, ReceiptJpegQuality, output))
                    }
                    target.absolutePath
                } catch (error: Throwable) {
                    target.delete()
                    throw error
                } finally {
                    bitmap.recycle()
                }
            } finally {
                staged.delete()
            }
        }.getOrNull()
    }

    override suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        path?.let(::resolve)?.delete()
        Unit
    }

    override suspend fun read(path: String): ByteArray? = withContext(Dispatchers.IO) {
        resolve(path).takeIf { it.isFile }?.readBytes()
    }

    private fun resolve(path: String): File =
        File(path).takeIf { it.isAbsolute } ?: File(receiptsDir, path)

    private fun decodeScaled(open: () -> InputStream?): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open()?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MaxReceiptSide) {
            sampleSize *= 2
        }
        val decoded = open()?.use {
            BitmapFactory.decodeStream(
                it,
                null,
                BitmapFactory.Options().apply { inSampleSize = sampleSize },
            )
        } ?: return null
        val largestSide = max(decoded.width, decoded.height)
        if (largestSide <= MaxReceiptSide) return decoded
        val scale = MaxReceiptSide.toFloat() / largestSide
        val scaled = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }
}

private class AndroidDatabaseBackupStorage(
    context: Context,
    private val database: PesetasDatabase,
) : DatabaseBackupStorage {
    private val databaseFile = context.getDatabasePath(PesetasDatabase.NAME)
    private val operationMutex = Mutex()

    override suspend fun exportDatabase(): BinaryContent = BinaryContent { sink ->
        operationMutex.withLock {
            withContext(Dispatchers.IO) { checkpointAndCopy(sink) }
        }
    }

    override suspend fun importDatabase(content: BinaryContent) {
        operationMutex.withLock {
            withContext(Dispatchers.IO) {
                databaseFile.parentFile?.mkdirs()
                val temporary = File(databaseFile.parentFile, "${PesetasDatabase.NAME}.importing")
                val pending = File(databaseFile.path + PendingImportSuffix)
                try {
                    temporary.sink().buffer().use { sink -> content.writeTo(sink) }
                    validateDatabase(temporary)
                    replaceDatabase(temporary, pending)
                    deleteDatabaseSidecars(pending)
                } finally {
                    temporary.delete()
                    deleteDatabaseSidecars(temporary)
                }
            }
        }
    }

    private suspend fun checkpointAndCopy(sink: BufferedSink) {
        database.useWriterConnection { connection ->
            connection.usePrepared("PRAGMA wal_checkpoint(FULL)") { statement ->
                check(statement.step() && statement.getLong(0) == 0L) {
                    "No se pudo consolidar la base de datos para exportarla"
                }
            }
            databaseFile.source().use { source -> sink.writeAll(source) }
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

}

private fun applyPendingDatabaseImport(context: Context) {
    applyPendingDatabaseImport(context.getDatabasePath(PesetasDatabase.NAME))
}

internal fun applyPendingDatabaseImport(databaseFile: File) {
    val pending = File(databaseFile.path + PendingImportSuffix)
    val marker = File(databaseFile.path + RestoreMarkerSuffix)
    if (!pending.isFile && !marker.isFile) return
    if (pending.isFile) {
        marker.outputStream().use { output ->
            output.write(1)
            output.fd.sync()
        }
        replaceDatabase(pending, databaseFile)
    }
    deleteDatabaseSidecars(databaseFile, requireSuccess = true)
    check(!marker.exists() || marker.delete()) { "No se pudo completar la restauración" }
}

private fun deleteDatabaseSidecars(file: File, requireSuccess: Boolean = false) {
    listOf(
        File(file.path + "-wal"),
        File(file.path + "-shm"),
        File(file.path + "-journal"),
    ).forEach { sidecar ->
        val deleted = !sidecar.exists() || sidecar.delete()
        if (requireSuccess) check(deleted) { "No se pudo limpiar ${sidecar.name}" }
    }
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

private class AndroidDocumentService : DocumentService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var activityReference = WeakReference<FragmentActivity>(null)
    private var pendingSave: PendingSave? = null
    private var pendingOpen: CancellableContinuation<BinaryContent?>? = null
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
                val resolver = activity.applicationContext.contentResolver
                continuation.resume(
                    androidBinaryContent(openSource = {
                        resolver.openInputStream(uri)?.source()
                    }),
                )
            }
        }
    }

    override suspend fun saveFile(
        suggestedName: String,
        mimeType: String,
        content: BinaryContent,
    ): Boolean = suspendCancellableCoroutine { continuation ->
        check(pendingSave == null) { "Ya hay una exportación en curso" }
        val pending = PendingSave(content, continuation)
        pendingSave = pending
        continuation.invokeOnCancellation {
            if (!pending.started) clearPendingSave(pending)
        }
        val launcher = if (mimeType == "text/csv") csvCreator else databaseCreator
        if (launcher == null) {
            pendingSave = null
            continuation.resumeWithException(IllegalStateException("No hay una pantalla Android activa"))
        } else {
            launcher.launch(suggestedName)
        }
    }

    override suspend fun openFile(allowedMimeTypes: List<String>): BinaryContent? =
        suspendCancellableCoroutine { continuation ->
            check(pendingOpen == null) { "Ya hay una importación en curso" }
            pendingOpen = continuation
            continuation.invokeOnCancellation {
                if (pendingOpen === continuation) pendingOpen = null
            }
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
        if (uri == null) {
            clearPendingSave(pending)
            pending.continuation.resume(false)
            return
        }
        pending.started = true
        scope.launch {
            val activity = activityReference.get()
            if (activity == null) {
                pending.continuation.resumeWithException(
                    IllegalStateException("No hay una pantalla Android activa"),
                )
                clearPendingSave(pending)
                return@launch
            }
            try {
                runCatching {
                    withContext(Dispatchers.IO) {
                        activity.contentResolver.openOutputStream(uri)?.sink()?.buffer()?.use { sink ->
                            pending.content.writeTo(sink)
                        } ?: error("No se pudo abrir el destino")
                    }
                    true
                }.onSuccess(pending.continuation::resume)
                    .onFailure(pending.continuation::resumeWithException)
            } finally {
                clearPendingSave(pending)
            }
        }
    }

    private fun clearPendingSave(pending: PendingSave) {
        if (pendingSave === pending) pendingSave = null
    }

    private data class PendingSave(
        val content: BinaryContent,
        val continuation: CancellableContinuation<Boolean>,
        var started: Boolean = false,
    )
}

internal fun androidBinaryContent(
    openSource: () -> Source?,
    onFinished: () -> Unit = {},
): BinaryContent = BinaryContent { sink ->
    withContext(Dispatchers.IO) {
        try {
            val source = openSource() ?: error("No se pudo abrir el archivo")
            source.use { sink.writeAll(it) }
        } finally {
            onFinished()
        }
    }
}

private class AndroidDeviceAuthenticator(
    private val activityProvider: () -> FragmentActivity,
) : DeviceAuthenticator {
    override fun isAvailable(): Boolean = runCatching {
        BiometricManager.from(activityProvider()).canAuthenticate(AndroidAuthenticators) ==
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
                .setAllowedAuthenticators(AndroidAuthenticators)
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
