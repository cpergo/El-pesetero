package com.pesetas.di

import com.pesetas.data.repository.AccountRepositoryImpl
import com.pesetas.data.repository.BudgetRepositoryImpl
import com.pesetas.data.repository.CurrencyRepositoryImpl
import com.pesetas.data.repository.CategoryRepositoryImpl
import com.pesetas.data.repository.RecurringTransactionRepositoryImpl
import com.pesetas.data.repository.SavingsGoalRepositoryImpl
import com.pesetas.data.repository.TagRepositoryImpl
import com.pesetas.data.repository.SettingsRepositoryImpl
import com.pesetas.data.repository.TransactionRepositoryImpl
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.BudgetRepository
import com.pesetas.domain.repository.CurrencyRepository
import com.pesetas.domain.repository.CategoryRepository
import com.pesetas.domain.repository.RecurringTransactionRepository
import com.pesetas.domain.repository.SavingsGoalRepository
import com.pesetas.domain.repository.TagRepository
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

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(impl: CurrencyRepositoryImpl): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindSavingsGoalRepository(impl: SavingsGoalRepositoryImpl): SavingsGoalRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(
        impl: RecurringTransactionRepositoryImpl,
    ): RecurringTransactionRepository
}
