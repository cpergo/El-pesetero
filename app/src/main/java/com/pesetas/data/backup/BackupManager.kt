package com.pesetas.data.backup

import android.content.Context
import android.net.Uri
import com.pesetas.data.local.PesetasDatabase
import com.pesetas.domain.model.TransactionType
import com.pesetas.domain.repository.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PesetasDatabase,
    private val transactionRepository: TransactionRepository,
) {

    suspend fun exportCsv(target: Uri) = withContext(Dispatchers.IO) {
        val details = transactionRepository.observeTransactions().first()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        context.contentResolver.openOutputStream(target)?.bufferedWriter()?.use { writer ->
            writer.appendLine(
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
                writer.appendLine(
                    CsvBuilder.row(
                        listOf(
                            item.transaction.date.format(formatter),
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
        } ?: error("No se pudo abrir el destino")
    }

    suspend fun exportDatabase(target: Uri) = withContext(Dispatchers.IO) {
        checkpoint()
        val dbFile = context.getDatabasePath(PesetasDatabase.NAME)
        context.contentResolver.openOutputStream(target)?.use { output ->
            dbFile.inputStream().use { input -> input.copyTo(output) }
        } ?: error("No se pudo abrir el destino")
    }

    suspend fun importDatabase(source: Uri) = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(PesetasDatabase.NAME)
        val temp = File(context.cacheDir, "import_temp.db")
        context.contentResolver.openInputStream(source)?.use { input ->
            temp.outputStream().use { output -> input.copyTo(output) }
        } ?: error("No se pudo abrir el archivo")
        if (!isSqliteDatabase(temp)) {
            temp.delete()
            error("El archivo no es una copia válida de Peseta")
        }
        checkpoint()
        database.close()
        deleteSidecar(dbFile)
        temp.copyTo(dbFile, overwrite = true)
        temp.delete()
    }

    private fun checkpoint() {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
    }

    private fun deleteSidecar(dbFile: File) {
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
    }

    private fun isSqliteDatabase(file: File): Boolean {
        val header = ByteArray(16)
        val read = file.inputStream().use { it.read(header) }
        return read == 16 && String(header, Charsets.US_ASCII).startsWith("SQLite format 3")
    }
}
