package com.pesetas

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pesetas.data.local.PesetasDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseMigrationTest {
    @Test
    fun migrationsOneToThreeKeepCoreDataAndCreateEveryFeatureTable() {
        BundledSQLiteDriver().open(":memory:").use { connection ->
            connection.exec(
                "CREATE TABLE accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                    "iconKey TEXT NOT NULL, colorArgb INTEGER NOT NULL, initialBalance REAL NOT NULL, " +
                    "position INTEGER NOT NULL)",
            )
            connection.exec(
                "CREATE TABLE categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                    "iconKey TEXT NOT NULL, colorArgb INTEGER NOT NULL, type TEXT NOT NULL, " +
                    "position INTEGER NOT NULL)",
            )
            connection.exec(
                "CREATE TABLE transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, amount REAL NOT NULL, " +
                    "dateEpochDay INTEGER NOT NULL, type TEXT NOT NULL, categoryId INTEGER, " +
                    "accountId INTEGER NOT NULL, transferAccountId INTEGER, note TEXT NOT NULL)",
            )
            connection.exec("INSERT INTO accounts VALUES (1, 'Banco', 'bank', 1, 25.0, 0)")

            PesetasDatabase.MIGRATION_1_2.migrate(connection)
            PesetasDatabase.MIGRATION_2_3.migrate(connection)

            val tables = connection.textColumn(
                "SELECT name FROM sqlite_master WHERE type='table'",
            ).toSet()
            assertTrue(
                tables.containsAll(
                    setOf(
                        "accounts", "categories", "transactions", "budgets",
                        "recurring_transactions", "exchange_rates", "savings_goals",
                        "tags", "transaction_tags",
                    ),
                ),
            )
            assertTrue(connection.textColumn("PRAGMA table_info(accounts)", column = 1).contains("currency"))
            assertTrue(
                connection.textColumn("PRAGMA table_info(transactions)", column = 1)
                    .contains("receiptImagePath"),
            )
            assertEquals("Banco", connection.textColumn("SELECT name FROM accounts").single())
            assertEquals("EUR", connection.textColumn("SELECT currency FROM accounts").single())
        }
    }

    @Test
    fun seedCallbackCreatesTheSameThreeAccountsAndTwelveCategories() {
        BundledSQLiteDriver().open(":memory:").use { connection ->
            connection.exec(
                "CREATE TABLE accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                    "iconKey TEXT NOT NULL, colorArgb INTEGER NOT NULL, initialBalance REAL NOT NULL, " +
                    "position INTEGER NOT NULL, currency TEXT NOT NULL)",
            )
            connection.exec(
                "CREATE TABLE categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                    "iconKey TEXT NOT NULL, colorArgb INTEGER NOT NULL, type TEXT NOT NULL, " +
                    "position INTEGER NOT NULL)",
            )
            PesetasDatabase.seedCallback.onCreate(connection)
            assertEquals(3L, connection.longValue("SELECT COUNT(*) FROM accounts"))
            assertEquals(12L, connection.longValue("SELECT COUNT(*) FROM categories"))
            assertEquals(setOf("EUR"), connection.textColumn("SELECT currency FROM accounts").toSet())
        }
    }
}

private fun SQLiteConnection.exec(sql: String) {
    prepare(sql).use { it.step() }
}

private fun SQLiteConnection.textColumn(sql: String, column: Int = 0): List<String> = buildList {
    prepare(sql).use { statement ->
        while (statement.step()) add(statement.getText(column))
    }
}

private fun SQLiteConnection.longValue(sql: String): Long = prepare(sql).use { statement ->
    check(statement.step())
    statement.getLong(0)
}
