package com.pesetas.ui.settings;

import com.pesetas.data.backup.BackupManager;
import com.pesetas.domain.repository.SettingsRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<BackupManager> backupManagerProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<BackupManager> backupManagerProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.backupManagerProvider = backupManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), backupManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<BackupManager> backupManagerProvider) {
    return new SettingsViewModel_Factory(settingsRepositoryProvider, backupManagerProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepository,
      BackupManager backupManager) {
    return new SettingsViewModel(settingsRepository, backupManager);
  }
}
