package com.pesetas.ui.accounts.editor;

import androidx.lifecycle.SavedStateHandle;
import com.pesetas.domain.repository.AccountRepository;
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
public final class AccountEditorViewModel_Factory implements Factory<AccountEditorViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<AccountRepository> accountRepositoryProvider;

  public AccountEditorViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AccountRepository> accountRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.accountRepositoryProvider = accountRepositoryProvider;
  }

  @Override
  public AccountEditorViewModel get() {
    return newInstance(savedStateHandleProvider.get(), accountRepositoryProvider.get());
  }

  public static AccountEditorViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<AccountRepository> accountRepositoryProvider) {
    return new AccountEditorViewModel_Factory(savedStateHandleProvider, accountRepositoryProvider);
  }

  public static AccountEditorViewModel newInstance(SavedStateHandle savedStateHandle,
      AccountRepository accountRepository) {
    return new AccountEditorViewModel(savedStateHandle, accountRepository);
  }
}
