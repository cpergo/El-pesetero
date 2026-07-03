package com.pesetas.ui.statistics;

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
public final class StatisticsViewModel_Factory implements Factory<StatisticsViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  public StatisticsViewModel_Factory(Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
  }

  @Override
  public StatisticsViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), categoryRepositoryProvider.get());
  }

  public static StatisticsViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider) {
    return new StatisticsViewModel_Factory(transactionRepositoryProvider, categoryRepositoryProvider);
  }

  public static StatisticsViewModel newInstance(TransactionRepository transactionRepository,
      CategoryRepository categoryRepository) {
    return new StatisticsViewModel(transactionRepository, categoryRepository);
  }
}
