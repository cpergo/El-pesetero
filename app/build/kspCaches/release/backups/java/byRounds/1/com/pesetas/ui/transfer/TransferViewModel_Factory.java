package com.pesetas.ui.transfer;

import androidx.lifecycle.SavedStateHandle;
import com.pesetas.domain.repository.AccountRepository;
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
public final class TransferViewModel_Factory implements Factory<TransferViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<AccountRepository> accountRepositoryProvider;

  public TransferViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.accountRepositoryProvider = accountRepositoryProvider;
  }

  @Override
  public TransferViewModel get() {
    return newInstance(savedStateHandleProvider.get(), transactionRepositoryProvider.get(), accountRepositoryProvider.get());
  }

  public static TransferViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider) {
    return new TransferViewModel_Factory(savedStateHandleProvider, transactionRepositoryProvider, accountRepositoryProvider);
  }

  public static TransferViewModel newInstance(SavedStateHandle savedStateHandle,
      TransactionRepository transactionRepository, AccountRepository accountRepository) {
    return new TransferViewModel(savedStateHandle, transactionRepository, accountRepository);
  }
}
