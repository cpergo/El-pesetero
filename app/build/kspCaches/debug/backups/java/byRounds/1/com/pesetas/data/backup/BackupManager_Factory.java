package com.pesetas.data.backup;

import android.content.Context;
import com.pesetas.data.local.PesetasDatabase;
import com.pesetas.domain.repository.TransactionRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class BackupManager_Factory implements Factory<BackupManager> {
  private final Provider<Context> contextProvider;

  private final Provider<PesetasDatabase> databaseProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public BackupManager_Factory(Provider<Context> contextProvider,
      Provider<PesetasDatabase> databaseProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.databaseProvider = databaseProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public BackupManager get() {
    return newInstance(contextProvider.get(), databaseProvider.get(), transactionRepositoryProvider.get());
  }

  public static BackupManager_Factory create(Provider<Context> contextProvider,
      Provider<PesetasDatabase> databaseProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new BackupManager_Factory(contextProvider, databaseProvider, transactionRepositoryProvider);
  }

  public static BackupManager newInstance(Context context, PesetasDatabase database,
      TransactionRepository transactionRepository) {
    return new BackupManager(context, database, transactionRepository);
  }
}
