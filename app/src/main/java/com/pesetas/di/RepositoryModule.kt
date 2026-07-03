package com.pesetas.di

import com.pesetas.data.repository.AccountRepositoryImpl
import com.pesetas.data.repository.CategoryRepositoryImpl
import com.pesetas.data.repository.SettingsRepositoryImpl
import com.pesetas.data.repository.TransactionRepositoryImpl
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.SettingsRepository
import com.pesetas.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
