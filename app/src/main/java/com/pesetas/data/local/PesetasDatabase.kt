package com.pesetas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pesetas.data.local.dao.AccountDao
import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.local.dao.TransactionDao
import com.pesetas.data.local.entity.AccountEntity
import com.pesetas.data.local.entity.CategoryEntity
import com.pesetas.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class PesetasDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "pesetas.db"

        val seedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DefaultData.accounts.forEach { account ->
                    db.execSQL(
                        "INSERT INTO accounts (name, iconKey, colorArgb, initialBalance, position) " +
                            "VALUES (?, ?, ?, ?, ?)",
                        arrayOf(
                            account.name,
                            account.iconKey,
                            account.colorArgb,
                            account.initialBalance,
                            account.position,
                        ),
                    )
                }
                DefaultData.categories.forEach { category ->
                    db.execSQL(
                        "INSERT INTO categories (name, iconKey, colorArgb, type, position) " +
                            "VALUES (?, ?, ?, ?, ?)",
                        arrayOf(
                            category.name,
                            category.iconKey,
                            category.colorArgb,
                            category.type.name,
                            category.position,
                        ),
                    )
                }
            }
        }
    }
}
