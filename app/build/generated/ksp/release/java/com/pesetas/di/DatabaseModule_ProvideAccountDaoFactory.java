package com.pesetas.di;

import com.pesetas.data.local.PesetasDatabase;
import com.pesetas.data.local.dao.AccountDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideAccountDaoFactory implements Factory<AccountDao> {
  private final Provider<PesetasDatabase> databaseProvider;

  public DatabaseModule_ProvideAccountDaoFactory(Provider<PesetasDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AccountDao get() {
    return provideAccountDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideAccountDaoFactory create(
      Provider<PesetasDatabase> databaseProvider) {
    return new DatabaseModule_ProvideAccountDaoFactory(databaseProvider);
  }

  public static AccountDao provideAccountDao(PesetasDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAccountDao(database));
  }
}
