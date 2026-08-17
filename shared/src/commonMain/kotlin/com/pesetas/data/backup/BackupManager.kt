package com.pesetas.data.backup

import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.TransactionRepository
import com.pesetas.platform.BinaryContent
import com.pesetas.platform.DocumentService
import kotlinx.coroutines.flow.first
import okio.BufferedSink

interface DatabaseBackupStorage {
    /** Streams a consistent SQLite snapshot after checkpointing WAL. */
    suspend fun exportDatabase(): BinaryContent

    /** Validates and stages a database that the platform applies during its restart. */
    suspend fun importDatabase(content: BinaryContent)
}

class BackupManager(
    private val transactionRepository: TransactionRepository,
    private val databaseStorage: DatabaseBackupStorage,
    private val documentService: DocumentService,
) {
    suspend fun exportCsv(): Boolean {
        val details = transactionRepository.observeTransactions().first()
        val content = BinaryContent { sink ->
            sink.writeLine(
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
                sink.writeLine(
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
            content = content,
        )
    }

    suspend fun exportDatabase(): Boolean = documentService.saveFile(
        suggestedName = "el-pesetero-backup.db",
        mimeType = "application/octet-stream",
        content = databaseStorage.exportDatabase(),
    )

    suspend fun importDatabase(): Boolean {
        val content = documentService.openFile(
            listOf(
                "application/vnd.sqlite3",
                "application/x-sqlite3",
                "application/octet-stream",
                "*/*",
            ),
        )
            ?: return false
        databaseStorage.importDatabase(content)
        return true
    }
}

private fun BufferedSink.writeLine(value: String) {
    writeUtf8(value)
    writeByte('\n'.code)
}
