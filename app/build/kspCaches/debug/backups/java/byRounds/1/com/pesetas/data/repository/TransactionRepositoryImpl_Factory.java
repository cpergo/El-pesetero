package com.pesetas.data.repository;

import com.pesetas.data.local.dao.AccountDao;
import com.pesetas.data.local.dao.CategoryDao;
import com.pesetas.data.local.dao.TransactionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class TransactionRepositoryImpl_Factory implements Factory<TransactionRepositoryImpl> {
  private final Provider<TransactionDao> transactionDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<AccountDao> accountDaoProvider;

  public TransactionRepositoryImpl_Factory(Provider<TransactionDao> transactionDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<AccountDao> accountDaoProvider) {
    this.transactionDaoProvider = transactionDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.accountDaoProvider = accountDaoProvider;
  }

  @Override
  public TransactionRepositoryImpl get() {
    return newInstance(transactionDaoProvider.get(), categoryDaoProvider.get(), accountDaoProvider.get());
  }

  public static TransactionRepositoryImpl_Factory create(
      Provider<TransactionDao> transactionDaoProvider, Provider<CategoryDao> categoryDaoProvider,
      Provider<AccountDao> accountDaoProvider) {
    return new TransactionRepositoryImpl_Factory(transactionDaoProvider, categoryDaoProvider, accountDaoProvider);
  }

  public static TransactionRepositoryImpl newInstance(TransactionDao transactionDao,
      CategoryDao categoryDao, AccountDao accountDao) {
    return new TransactionRepositoryImpl(transactionDao, categoryDao, accountDao);
  }
}
