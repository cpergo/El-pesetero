package com.pesetas.di

import android.content.Context
import androidx.room.Room
import com.pesetas.data.local.PesetasDatabase
import com.pesetas.data.local.dao.AccountDao
import com.pesetas.data.local.dao.CategoryDao
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
            .build()

    @Provides
    fun provideAccountDao(database: PesetasDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideCategoryDao(database: PesetasDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: PesetasDatabase): TransactionDao = database.transactionDao()
}
