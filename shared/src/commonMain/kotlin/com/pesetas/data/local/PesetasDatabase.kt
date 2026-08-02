package com.pesetas.data.local

import androidx.room.Database
import androidx.room.ConstructedBy
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import com.pesetas.data.local.dao.AccountDao
import com.pesetas.data.local.dao.BudgetDao
import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.local.dao.ExchangeRateDao
import com.pesetas.data.local.dao.RecurringTransactionDao
import com.pesetas.data.local.dao.SavingsGoalDao
import com.pesetas.data.local.dao.TagDao
import com.pesetas.data.local.dao.TransactionDao
import com.pesetas.data.local.entity.AccountEntity
import com.pesetas.data.local.entity.BudgetEntity
import com.pesetas.data.local.entity.CategoryEntity
import com.pesetas.data.local.entity.ExchangeRateEntity
import com.pesetas.data.local.entity.RecurringTransactionEntity
import com.pesetas.data.local.entity.SavingsGoalEntity
import com.pesetas.data.local.entity.TagEntity
import com.pesetas.data.local.entity.TransactionTagCrossRef
import com.pesetas.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        ExchangeRateEntity::class,
        SavingsGoalEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
@ConstructedBy(PesetasDatabaseConstructor::class)
abstract class PesetasDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun tagDao(): TagDao

    companion object {
        const val NAME = "pesetas.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execute("ALTER TABLE transactions ADD COLUMN receiptImagePath TEXT")
                connection.execute(
                    "CREATE TABLE IF NOT EXISTS `budgets` (" +
                        "`categoryId` INTEGER NOT NULL, " +
                        "`monthlyLimit` REAL NOT NULL, " +
                        "PRIMARY KEY(`categoryId`), " +
                        "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                connection.execute(
                    "CREATE TABLE IF NOT EXISTS `recurring_transactions` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`categoryId` INTEGER NOT NULL, " +
                        "`accountId` INTEGER NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "`dayOfMonth` INTEGER NOT NULL, " +
                        "`lastGeneratedEpochDay` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                connection.execute(
                    "CREATE INDEX IF NOT EXISTS `index_recurring_transactions_categoryId` " +
                        "ON `recurring_transactions` (`categoryId`)",
                )
                connection.execute(
                    "CREATE INDEX IF NOT EXISTS `index_recurring_transactions_accountId` " +
                        "ON `recurring_transactions` (`accountId`)",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execute("ALTER TABLE accounts ADD COLUMN currency TEXT NOT NULL DEFAULT 'EUR'")
                connection.execute(
                    "CREATE TABLE IF NOT EXISTS `exchange_rates` (" +
                        "`currencyCode` TEXT NOT NULL, " +
                        "`rateToMain` REAL NOT NULL, " +
                        "`updatedEpochDay` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`currencyCode`))",
                )
                connection.execute(
                    "CREATE TABLE IF NOT EXISTS `savings_goals` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`targetAmount` REAL NOT NULL, " +
                        "`currentAmount` REAL NOT NULL, " +
                        "`deadlineEpochDay` INTEGER, " +
                        "`iconKey` TEXT NOT NULL, " +
                        "`colorArgb` INTEGER NOT NULL)",
                )
                connection.execute(
                    "CREATE TABLE IF NOT EXISTS `tags` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`colorArgb` INTEGER NOT NULL)",
                )
                connection.execute(
                    "CREATE TABLE IF NOT EXISTS `transaction_tags` (" +
                        "`transactionId` INTEGER NOT NULL, " +
                        "`tagId` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`transactionId`, `tagId`), " +
                        "FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                        "FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                connection.execute(
                    "CREATE INDEX IF NOT EXISTS `index_transaction_tags_tagId` " +
                        "ON `transaction_tags` (`tagId`)",
                )
            }
        }

        val seedCallback = object : Callback() {
            override fun onCreate(connection: SQLiteConnection) {
                super.onCreate(connection)
                DefaultData.accounts.forEach { account ->
                    connection.prepare(
                        "INSERT INTO accounts " +
                            "(name, iconKey, colorArgb, initialBalance, position, currency) " +
                            "VALUES (?, ?, ?, ?, ?, 'EUR')",
                    ).use { statement ->
                        statement.bindText(1, account.name)
                        statement.bindText(2, account.iconKey)
                        statement.bindLong(3, account.colorArgb.toLong())
                        statement.bindDouble(4, account.initialBalance)
                        statement.bindLong(5, account.position.toLong())
                        statement.step()
                    }
                }
                DefaultData.categories.forEach { category ->
                    connection.prepare(
                        "INSERT INTO categories (name, iconKey, colorArgb, type, position) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    ).use { statement ->
                        statement.bindText(1, category.name)
                        statement.bindText(2, category.iconKey)
                        statement.bindLong(3, category.colorArgb.toLong())
                        statement.bindText(4, category.type.name)
                        statement.bindLong(5, category.position.toLong())
                        statement.step()
                    }
                }
            }
        }
    }
}

@Suppress("KotlinNoActualForExpect")
expect object PesetasDatabaseConstructor : RoomDatabaseConstructor<PesetasDatabase> {
    override fun initialize(): PesetasDatabase
}

private fun SQLiteConnection.execute(sql: String) {
    prepare(sql).use { statement -> statement.step() }
}
