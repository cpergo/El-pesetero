package com.pesetas.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pesetas.AppContainer
import com.pesetas.data.backup.DatabaseBackupStorage
import com.pesetas.data.files.ReceiptStorage
import com.pesetas.data.local.PesetasDatabase
import com.pesetas.data.local.PesetasDatabaseConstructor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileManager.Companion.defaultManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSURL.Companion.fileURLWithPath
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.darwin.NSObject
import platform.posix.memcpy
import platform.posix.rename

private const val SettingsFileName = "pesetas_settings.preferences_pb"
private const val MaxReceiptSide = 1080.0
private const val ReceiptJpegQuality = 0.82

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal object IosPlatformSession {
    private val rootPath = applicationSupportPath()
    private val databasePath = "$rootPath/${PesetasDatabase.NAME}"
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
        "$rootPath/$SettingsFileName".toPath()
    }
    private val documentService = IosDocumentService()

    var container: AppContainer by mutableStateOf(createContainer())
        private set

    init {
        excludeFromAutomaticBackup(rootPath)
    }

    fun reloadDatabase() {
        container = createContainer()
    }

    private fun createContainer(): AppContainer {
        val database = Room.databaseBuilder<PesetasDatabase>(
            name = databasePath,
            factory = PesetasDatabaseConstructor::initialize,
        )
            .addCallback(PesetasDatabase.seedCallback)
            .addMigrations(PesetasDatabase.MIGRATION_1_2, PesetasDatabase.MIGRATION_2_3)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
        return AppContainer(
            database = database,
            dataStore = dataStore,
            receiptStorage = IosReceiptStorage(rootPath),
            databaseBackupStorage = IosDatabaseBackupStorage(databasePath, database),
            documentService = documentService,
            authenticator = IosDeviceAuthenticator(),
            externalLinks = IosExternalLinkService(),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun applicationSupportPath(): String {
    val url = defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "No se pudo abrir Application Support" }
}

@OptIn(ExperimentalForeignApi::class)
private fun excludeFromAutomaticBackup(path: String) {
    runCatching {
        fileURLWithPath(path).setResourceValue(
            value = true,
            forKey = NSURLIsExcludedFromBackupKey,
            error = null,
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosReceiptStorage(rootPath: String) : ReceiptStorage {
    private val receiptsPath = "$rootPath/receipts"

    init {
        defaultManager.createDirectoryAtPath(
            path = receiptsPath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        excludeFromAutomaticBackup(receiptsPath)
    }

    override suspend fun store(encodedImage: ByteArray): String? = withContext(Dispatchers.Default) {
        runCatching {
            val image = UIImage(data = encodedImage.toNSData())
            val compressed = resizeIfNeeded(image)
            val data = UIImageJPEGRepresentation(compressed, ReceiptJpegQuality)
                ?: return@runCatching null
            val fileName = "receipt_${NSUUID.UUID().UUIDString}.jpg"
            val target = "$receiptsPath/$fileName"
            check(data.writeToFile(target, atomically = true))
            excludeFromAutomaticBackup(target)
            fileName
        }.getOrNull()
    }

    override suspend fun read(path: String): ByteArray? = withContext(Dispatchers.Default) {
        defaultManager.contentsAtPath(resolveIosReceiptPath(path))?.toByteArray()
    }

    override suspend fun delete(path: String?) = withContext(Dispatchers.Default) {
        path?.let { defaultManager.removeItemAtPath(resolveIosReceiptPath(it), error = null) }
        Unit
    }

    private fun resizeIfNeeded(image: UIImage): UIImage {
        val (width, height) = image.size.useContents { width to height }
        val largest = max(width, height)
        if (largest <= MaxReceiptSide) return image
        val scale = MaxReceiptSide / largest
        val targetWidth = width * scale
        val targetHeight = height * scale
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), false, 1.0)
        image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
        val resized = UIGraphicsGetImageFromCurrentImageContext() ?: image
        UIGraphicsEndImageContext()
        return resized
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosDatabaseBackupStorage(
    private val databasePath: String,
    private val database: PesetasDatabase,
) : DatabaseBackupStorage {
    override suspend fun exportDatabase(): ByteArray = withContext(Dispatchers.Default) {
        checkpoint()
        requireNotNull(defaultManager.contentsAtPath(databasePath)) {
            "No se pudo leer la base de datos"
        }.toByteArray()
    }

    override suspend fun importDatabase(bytes: ByteArray) = withContext(Dispatchers.Default) {
        val temporary = "$databasePath.importing"
        check(bytes.toNSData().writeToFile(temporary, atomically = true)) {
            "No se pudo preparar la copia"
        }
        try {
            validateDatabase(temporary)
            checkpoint()
            check(rename(temporary, databasePath) == 0) { "No se pudo reemplazar la base de datos" }
            database.close()
            deleteSidecars(databasePath)
        } finally {
            defaultManager.removeItemAtPath(temporary, error = null)
            deleteSidecars(temporary)
        }
    }

    private suspend fun checkpoint() {
        database.useWriterConnection { connection ->
            connection.usePrepared("PRAGMA wal_checkpoint(FULL)") { statement -> statement.step() }
        }
    }

    private fun validateDatabase(path: String) {
        val raw = defaultManager.contentsAtPath(path)?.toByteArray()
            ?: error("La copia está vacía")
        require(raw.size >= 16 && raw.decodeToString(0, 16).startsWith("SQLite format 3")) {
            "El archivo no es una base SQLite válida"
        }
        BundledSQLiteDriver().open(path).use { connection ->
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

    private fun deleteSidecars(path: String) {
        defaultManager.removeItemAtPath("$path-wal", error = null)
        defaultManager.removeItemAtPath("$path-shm", error = null)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosDocumentService : DocumentService {
    private var operation: Operation? = null
    private var saveContinuation: CancellableContinuation<Boolean>? = null
    private var openContinuation: CancellableContinuation<ByteArray?>? = null
    private var temporaryExportPath: String? = null
    private var picker: UIDocumentPickerViewController? = null
    private val delegate = IosDocumentPickerDelegate(
        onSelection = ::handleSelection,
        onCancel = ::handleCancellation,
    )

    override suspend fun saveFile(
        suggestedName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Boolean = suspendCancellableCoroutine { continuation ->
        check(operation == null) { "Ya hay un selector de documentos abierto" }
        val path = "${NSTemporaryDirectory()}$suggestedName"
        check(bytes.toNSData().writeToFile(path, atomically = true))
        temporaryExportPath = path
        saveContinuation = continuation
        operation = Operation.EXPORT
        continuation.invokeOnCancellation { finish() }
        present(
            UIDocumentPickerViewController(
                forExportingURLs = listOf(fileURLWithPath(path)),
                asCopy = true,
            ),
        )
    }

    override suspend fun openFile(allowedMimeTypes: List<String>): ByteArray? =
        suspendCancellableCoroutine { continuation ->
            check(operation == null) { "Ya hay un selector de documentos abierto" }
            openContinuation = continuation
            operation = Operation.IMPORT
            continuation.invokeOnCancellation { finish() }
            present(
                UIDocumentPickerViewController(
                    documentTypes = listOf("public.data", "public.database"),
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
                ),
            )
        }

    private fun handleCancellation() {
        when (operation) {
            Operation.EXPORT -> saveContinuation?.resume(false)
            Operation.IMPORT -> openContinuation?.resume(null)
            null -> Unit
        }
        finish()
    }

    private fun handleSelection(url: NSURL?) {
        when (operation) {
            Operation.EXPORT -> saveContinuation?.resume(url != null)
            Operation.IMPORT -> {
                if (url == null) {
                    openContinuation?.resume(null)
                } else {
                    runCatching {
                        val scoped = url.startAccessingSecurityScopedResource()
                        try {
                            NSData.dataWithContentsOfURL(url)?.toByteArray()
                                ?: error("No se pudo leer el documento")
                        } finally {
                            if (scoped) url.stopAccessingSecurityScopedResource()
                        }
                    }.onSuccess { openContinuation?.resume(it) }
                        .onFailure { openContinuation?.resumeWithException(it) }
                }
            }
            null -> Unit
        }
        finish()
    }

    private fun present(controller: UIDocumentPickerViewController) {
        val presenter = topIosViewController()
        if (presenter == null) {
            val error = IllegalStateException("No hay una pantalla iOS activa")
            when (operation) {
                Operation.EXPORT -> saveContinuation?.resumeWithException(error)
                Operation.IMPORT -> openContinuation?.resumeWithException(error)
                null -> Unit
            }
            finish()
            return
        }
        picker = controller
        controller.delegate = delegate
        presenter.presentViewController(controller, animated = true, completion = null)
    }

    private fun finish() {
        temporaryExportPath?.let { defaultManager.removeItemAtPath(it, error = null) }
        temporaryExportPath = null
        saveContinuation = null
        openContinuation = null
        operation = null
        picker = null
    }

    private enum class Operation { EXPORT, IMPORT }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosDocumentPickerDelegate(
    private val onSelection: (NSURL?) -> Unit,
    private val onCancel: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        onSelection(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentAtURL: NSURL,
    ) {
        onSelection(didPickDocumentAtURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onCancel()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosDeviceAuthenticator : DeviceAuthenticator {
    override fun isAvailable(): Boolean =
        LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)

    override suspend fun authenticate(): Boolean = suspendCancellableCoroutine { continuation ->
        val context = LAContext()
        continuation.invokeOnCancellation { context.invalidate() }
        context.evaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthentication,
            localizedReason = "Confirma tu identidad para abrir El pesetero",
        ) { success, _ ->
            if (continuation.isActive) continuation.resume(success)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosExternalLinkService : ExternalLinkService {
    override fun open(url: String) {
        val target = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(
            target,
            options = emptyMap<Any?, Any>(),
            completionHandler = null,
        )
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned -> NSData.create(pinned.addressOf(0), size.toULong()) }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    if (length == 0uL) return ByteArray(0)
    return ByteArray(length.toInt()).also { result ->
        result.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun resolveIosReceiptPath(path: String): String {
    val receiptsPath = "${applicationSupportPath()}/receipts"
    if (!path.startsWith('/')) return "$receiptsPath/${path.substringAfterLast('/')}"
    return if (defaultManager.fileExistsAtPath(path)) {
        path
    } else {
        "$receiptsPath/${path.substringAfterLast('/')}"
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun topIosViewController(): UIViewController? {
    var current: UIViewController? = null
    for (scene in UIApplication.sharedApplication.connectedScenes) {
        val windowScene = scene as? UIWindowScene ?: continue
        for (candidate in windowScene.windows) {
            val window = candidate as? UIWindow ?: continue
            if (!window.isKeyWindow()) continue
            current = window.rootViewController
            break
        }
        if (current != null) break
    }
    while (current?.presentedViewController != null) current = current.presentedViewController
    return current
}
