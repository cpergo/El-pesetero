package com.pesetas.di

import android.content.Context
import androidx.room.Room
import com.pesetas.data.local.PesetasDatabase
import com.pesetas.data.local.dao.AccountDao
import com.pesetas.data.local.dao.BudgetDao
import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.local.dao.ExchangeRateDao
import com.pesetas.data.local.dao.SavingsGoalDao
import com.pesetas.data.local.dao.TagDao
import com.pesetas.data.local.dao.RecurringTransactionDao
import com.pesetas.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PesetasDatabase =
        Room.databaseBuilder(context, PesetasDatabase::class.java, PesetasDatabase.NAME)
            .addCallback(PesetasDatabase.seedCallback)
            .addMigrations(PesetasDatabase.MIGRATION_1_2, PesetasDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun provideBudgetDao(database: PesetasDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideRecurringTransactionDao(database: PesetasDatabase): RecurringTransactionDao =
        database.recurringTransactionDao()

    @Provides
    fun provideAccountDao(database: PesetasDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideCategoryDao(database: PesetasDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: PesetasDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideExchangeRateDao(database: PesetasDatabase): ExchangeRateDao = database.exchangeRateDao()

    @Provides
    fun provideSavingsGoalDao(database: PesetasDatabase): SavingsGoalDao = database.savingsGoalDao()

    @Provides
    fun provideTagDao(database: PesetasDatabase): TagDao = database.tagDao()
}
