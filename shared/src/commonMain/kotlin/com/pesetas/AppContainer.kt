package com.pesetas

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pesetas.data.backup.BackupManager
import com.pesetas.data.backup.DatabaseBackupStorage
import com.pesetas.data.files.ReceiptImageStore
import com.pesetas.data.files.ReceiptStorage
import com.pesetas.data.local.PesetasDatabase
import com.pesetas.data.repository.AccountRepositoryImpl
import com.pesetas.data.repository.BudgetRepositoryImpl
import com.pesetas.data.repository.CategoryRepositoryImpl
import com.pesetas.data.repository.CurrencyRepositoryImpl
import com.pesetas.data.repository.RecurringTransactionRepositoryImpl
import com.pesetas.data.repository.SavingsGoalRepositoryImpl
import com.pesetas.data.repository.SettingsRepositoryImpl
import com.pesetas.data.repository.TagRepositoryImpl
import com.pesetas.data.repository.TransactionRepositoryImpl
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.BudgetRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.CurrencyRepository
import com.pesetas.domain.repository.RecurringTransactionRepository
import com.pesetas.domain.repository.SavingsGoalRepository
import com.pesetas.domain.repository.SettingsRepository
import com.pesetas.domain.repository.TagRepository
import com.pesetas.domain.repository.TransactionRepository
import com.pesetas.platform.DeviceAuthenticator
import com.pesetas.platform.DocumentService
import com.pesetas.platform.ExternalLinkService

/**
 * Small multiplatform dependency container. Keeping construction explicit avoids tying the
 * shared code to an Android-only DI runtime and makes every platform use the same repositories.
 */
class AppContainer(
    val database: PesetasDatabase,
    dataStore: DataStore<Preferences>,
    receiptStorage: ReceiptStorage,
    databaseBackupStorage: DatabaseBackupStorage,
    documentService: DocumentService,
    val authenticator: DeviceAuthenticator,
    val externalLinks: ExternalLinkService,
) {
    val accountRepository: AccountRepository = AccountRepositoryImpl(database.accountDao())
    val categoryRepository: CategoryRepository = CategoryRepositoryImpl(database.categoryDao())
    val transactionRepository: TransactionRepository = TransactionRepositoryImpl(
        database.transactionDao(),
        database.categoryDao(),
        database.accountDao(),
    )
    val budgetRepository: BudgetRepository = BudgetRepositoryImpl(
        database.budgetDao(),
        database.categoryDao(),
        database.transactionDao(),
    )
    val recurringRepository: RecurringTransactionRepository = RecurringTransactionRepositoryImpl(
        database.recurringTransactionDao(),
        database.transactionDao(),
        database.categoryDao(),
        database.accountDao(),
    )
    val savingsGoalRepository: SavingsGoalRepository =
        SavingsGoalRepositoryImpl(database.savingsGoalDao())
    val tagRepository: TagRepository = TagRepositoryImpl(database.tagDao(), database.categoryDao())
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(dataStore)
    val currencyRepository: CurrencyRepository =
        CurrencyRepositoryImpl(database.exchangeRateDao(), dataStore)
    val receiptImageStore = ReceiptImageStore(receiptStorage)
    val backupManager = BackupManager(transactionRepository, databaseBackupStorage, documentService)

    fun close() = database.close()
}
