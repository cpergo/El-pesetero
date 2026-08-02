package com.pesetas.data.backup

import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.TransactionRepository
import com.pesetas.platform.DocumentService
import kotlinx.coroutines.flow.first

interface DatabaseBackupStorage {
    /** Returns a consistent SQLite snapshot after checkpointing WAL. */
    suspend fun exportDatabase(): ByteArray

    /** Atomically replaces the app database after validating the supplied SQLite file. */
    suspend fun importDatabase(bytes: ByteArray)
}

class BackupManager(
    private val transactionRepository: TransactionRepository,
    private val databaseStorage: DatabaseBackupStorage,
    private val documentService: DocumentService,
) {
    suspend fun exportCsv(): Boolean {
        val details = transactionRepository.observeTransactions().first()
        val csv = buildString {
            appendLine(
                CsvBuilder.row(
                    listOf("fecha", "tipo", "importe", "categoria", "cuenta", "cuenta_destino", "nota"),
                ),
            )
            details.forEach { item ->
                val type = when (item.transaction.type) {
                    TransactionType.INCOME -> "Ingreso"
                    TransactionType.EXPENSE -> "Gasto"
                    TransactionType.TRANSFER -> "Transferencia"
                }
                appendLine(
                    CsvBuilder.row(
                        listOf(
                            item.transaction.date.toString(),
                            type,
                            item.transaction.amount.toString(),
                            item.category?.name.orEmpty(),
                            item.account.name,
                            item.transferAccount?.name.orEmpty(),
                            item.transaction.note,
                        ),
                    ),
                )
            }
        }
        return documentService.saveFile(
            suggestedName = "el-pesetero-movimientos.csv",
            mimeType = "text/csv",
            bytes = csv.encodeToByteArray(),
        )
    }

    suspend fun exportDatabase(): Boolean = documentService.saveFile(
        suggestedName = "el-pesetero-backup.db",
        mimeType = "application/octet-stream",
        bytes = databaseStorage.exportDatabase(),
    )

    suspend fun importDatabase(): Boolean {
        val bytes = documentService.openFile(listOf("application/octet-stream", "application/x-sqlite3"))
            ?: return false
        databaseStorage.importDatabase(bytes)
        return true
    }
}
