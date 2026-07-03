package com.pesetas.data.repository;

import com.pesetas.data.local.dao.AccountDao;
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
public final class AccountRepositoryImpl_Factory implements Factory<AccountRepositoryImpl> {
  private final Provider<AccountDao> accountDaoProvider;

  public AccountRepositoryImpl_Factory(Provider<AccountDao> accountDaoProvider) {
    this.accountDaoProvider = accountDaoProvider;
  }

  @Override
  public AccountRepositoryImpl get() {
    return newInstance(accountDaoProvider.get());
  }

  public static AccountRepositoryImpl_Factory create(Provider<AccountDao> accountDaoProvider) {
    return new AccountRepositoryImpl_Factory(accountDaoProvider);
  }

  public static AccountRepositoryImpl newInstance(AccountDao accountDao) {
    return new AccountRepositoryImpl(accountDao);
  }
}
