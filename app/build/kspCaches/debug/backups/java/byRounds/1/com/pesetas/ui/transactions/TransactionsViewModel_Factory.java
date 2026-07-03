package com.pesetas.ui.transactions;

import com.pesetas.domain.repository.AccountRepository;
import com.pesetas.domain.repository.CategoryRepository;
import com.pesetas.domain.repository.TransactionRepository;
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
public final class TransactionsViewModel_Factory implements Factory<TransactionsViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<AccountRepository> accountRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  public TransactionsViewModel_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.accountRepositoryProvider = accountRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
  }

  @Override
  public TransactionsViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), accountRepositoryProvider.get(), categoryRepositoryProvider.get());
  }

  public static TransactionsViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider) {
    return new TransactionsViewModel_Factory(transactionRepositoryProvider, accountRepositoryProvider, categoryRepositoryProvider);
  }

  public static TransactionsViewModel newInstance(TransactionRepository transactionRepository,
      AccountRepository accountRepository, CategoryRepository categoryRepository) {
    return new TransactionsViewModel(transactionRepository, accountRepository, categoryRepository);
  }
}
