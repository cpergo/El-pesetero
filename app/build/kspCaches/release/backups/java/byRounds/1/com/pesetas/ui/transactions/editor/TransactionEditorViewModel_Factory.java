package com.pesetas.ui.transactions.editor;

import androidx.lifecycle.SavedStateHandle;
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
public final class TransactionEditorViewModel_Factory implements Factory<TransactionEditorViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<AccountRepository> accountRepositoryProvider;

  public TransactionEditorViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.accountRepositoryProvider = accountRepositoryProvider;
  }

  @Override
  public TransactionEditorViewModel get() {
    return newInstance(savedStateHandleProvider.get(), transactionRepositoryProvider.get(), categoryRepositoryProvider.get(), accountRepositoryProvider.get());
  }

  public static TransactionEditorViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider) {
    return new TransactionEditorViewModel_Factory(savedStateHandleProvider, transactionRepositoryProvider, categoryRepositoryProvider, accountRepositoryProvider);
  }

  public static TransactionEditorViewModel newInstance(SavedStateHandle savedStateHandle,
      TransactionRepository transactionRepository, CategoryRepository categoryRepository,
      AccountRepository accountRepository) {
    return new TransactionEditorViewModel(savedStateHandle, transactionRepository, categoryRepository, accountRepository);
  }
}
