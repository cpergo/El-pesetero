package com.pesetas.ui.categories.editor;

import androidx.lifecycle.SavedStateHandle;
import com.pesetas.domain.repository.CategoryRepository;
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
public final class CategoryEditorViewModel_Factory implements Factory<CategoryEditorViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  public CategoryEditorViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<CategoryRepository> categoryRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
  }

  @Override
  public CategoryEditorViewModel get() {
    return newInstance(savedStateHandleProvider.get(), categoryRepositoryProvider.get());
  }

  public static CategoryEditorViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<CategoryRepository> categoryRepositoryProvider) {
    return new CategoryEditorViewModel_Factory(savedStateHandleProvider, categoryRepositoryProvider);
  }

  public static CategoryEditorViewModel newInstance(SavedStateHandle savedStateHandle,
      CategoryRepository categoryRepository) {
    return new CategoryEditorViewModel(savedStateHandle, categoryRepository);
  }
}
