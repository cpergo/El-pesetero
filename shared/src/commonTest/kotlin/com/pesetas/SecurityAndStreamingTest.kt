package com.pesetas

import com.pesetas.data.backup.BackupManager
import com.pesetas.data.backup.DatabaseBackupStorage
import com.pesetas.domain.model.Account
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.model.MonthlyTotals
import com.pesetas.domain.model.Transaction
import com.pesetas.domain.model.TransactionDetails
import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.TransactionRepository
import com.pesetas.platform.BinaryContent
import com.pesetas.platform.DeviceAuthenticator
import com.pesetas.platform.DocumentService
import com.pesetas.platform.asBinaryContent
import com.pesetas.platform.readByteArray
import com.pesetas.ui.security.authenticateForUnlock
import com.pesetas.util.LocalDate
import com.pesetas.util.YearMonth
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

class SecurityAndStreamingTest {
    @Test
    fun unavailableAuthenticatorNeverAttemptsAuthenticationOrUnlocks() = runBlocking {
        val authenticator = FakeAuthenticator(available = false, result = true)

        assertFalse(authenticateForUnlock(authenticator))
        assertEquals(0, authenticator.attempts)
    }

    @Test
    fun unlockRequiresACompletedSuccessfulAuthentication() = runBlocking {
        val rejected = FakeAuthenticator(available = true, result = false)
        val accepted = FakeAuthenticator(available = true, result = true)

        assertFalse(authenticateForUnlock(rejected))
        assertTrue(authenticateForUnlock(accepted))
        assertEquals(1, rejected.attempts)
        assertEquals(1, accepted.attempts)
    }

    @Test
    fun databaseBackupUsesBinaryContentAndKeepsTheWildcardImportFallback() = runBlocking {
        val exported = ByteArray(256 * 1024) { index -> (index % 251).toByte() }
        val imported = "SQLite format 3 streamed import".encodeToByteArray()
        val storage = RecordingDatabaseStorage(exported)
        val documents = RecordingDocumentService(imported.asBinaryContent())
        val manager = BackupManager(StubTransactionRepository(), storage, documents)

        assertTrue(manager.exportDatabase())
        assertContentEquals(exported, documents.savedBytes)

        assertTrue(manager.importDatabase())
        assertContentEquals(imported, storage.importedBytes)
        assertTrue("*/*" in documents.openedMimeTypes)
        assertTrue("application/vnd.sqlite3" in documents.openedMimeTypes)
    }

    @Test
    fun csvStreamingPreservesTheExistingFormatAndEscaping() = runBlocking {
        val details = TransactionDetails(
            transaction = Transaction(
                amount = 12.5,
                date = LocalDate(2026, 8, 3),
                type = TransactionType.EXPENSE,
                categoryId = 2,
                accountId = 1,
                transferAccountId = null,
                note = "café, \"especial\"",
            ),
            category = Category(
                id = 2,
                name = "Comida",
                iconKey = "restaurant",
                colorArgb = 0,
                type = CategoryType.EXPENSE,
            ),
            account = Account(
                id = 1,
                name = "Banco",
                iconKey = "bank",
                colorArgb = 0,
                initialBalance = 0.0,
            ),
            transferAccount = null,
        )
        val documents = RecordingDocumentService(ByteArray(0).asBinaryContent())
        val manager = BackupManager(
            StubTransactionRepository(listOf(details)),
            RecordingDatabaseStorage(ByteArray(0)),
            documents,
        )

        assertTrue(manager.exportCsv())
        assertEquals(
            "fecha,tipo,importe,categoria,cuenta,cuenta_destino,nota\n" +
                "2026-08-03,Gasto,12.5,Comida,Banco,,\"café, \"\"especial\"\"\"\n",
            documents.savedBytes?.decodeToString(),
        )
    }
}

private class FakeAuthenticator(
    private val available: Boolean,
    private val result: Boolean,
) : DeviceAuthenticator {
    var attempts: Int = 0
        private set

    override fun isAvailable(): Boolean = available

    override suspend fun authenticate(): Boolean {
        attempts += 1
        return result
    }
}

private class RecordingDatabaseStorage(
    private val exportedBytes: ByteArray,
) : DatabaseBackupStorage {
    var importedBytes: ByteArray? = null
        private set

    override suspend fun exportDatabase(): BinaryContent = BinaryContent { sink ->
        var offset = 0
        while (offset < exportedBytes.size) {
            val count = minOf(4_096, exportedBytes.size - offset)
            sink.write(exportedBytes, offset, count)
            offset += count
        }
    }

    override suspend fun importDatabase(content: BinaryContent) {
        importedBytes = content.readByteArray()
    }
}

private class RecordingDocumentService(
    private val contentToOpen: BinaryContent,
) : DocumentService {
    var savedBytes: ByteArray? = null
        private set
    var openedMimeTypes: List<String> = emptyList()
        private set

    override suspend fun saveFile(
        suggestedName: String,
        mimeType: String,
        content: BinaryContent,
    ): Boolean {
        savedBytes = content.readByteArray()
        return true
    }

    override suspend fun openFile(allowedMimeTypes: List<String>): BinaryContent {
        openedMimeTypes = allowedMimeTypes
        return contentToOpen
    }
}

private class StubTransactionRepository(
    private val transactions: List<TransactionDetails> = emptyList(),
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<TransactionDetails>> = flowOf(transactions)
    override fun observeTransactions(month: YearMonth): Flow<List<TransactionDetails>> = error("Unused")
    override fun observeMonthlyTotals(month: YearMonth): Flow<MonthlyTotals> = error("Unused")
    override fun observeExpenseByCategory(month: YearMonth): Flow<List<CategorySpending>> = error("Unused")
    override fun observeMonthlyTotalsRange(from: YearMonth, to: YearMonth): Flow<List<MonthlyTotals>> = error("Unused")
    override fun observeCategoryEvolution(
        categoryId: Long,
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<MonthlyTotals>> = error("Unused")

    override suspend fun getTransaction(id: Long): Transaction? = error("Unused")
    override suspend fun upsert(transaction: Transaction): Long = error("Unused")
    override suspend fun delete(transaction: Transaction): Unit = error("Unused")
}
