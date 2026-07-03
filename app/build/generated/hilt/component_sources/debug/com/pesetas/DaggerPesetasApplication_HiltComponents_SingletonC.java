package com.pesetas;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.pesetas.data.backup.BackupManager;
import com.pesetas.data.local.PesetasDatabase;
import com.pesetas.data.local.dao.AccountDao;
import com.pesetas.data.local.dao.CategoryDao;
import com.pesetas.data.local.dao.TransactionDao;
import com.pesetas.data.repository.AccountRepositoryImpl;
import com.pesetas.data.repository.CategoryRepositoryImpl;
import com.pesetas.data.repository.SettingsRepositoryImpl;
import com.pesetas.data.repository.TransactionRepositoryImpl;
import com.pesetas.di.DataStoreModule_ProvideDataStoreFactory;
import com.pesetas.di.DatabaseModule_ProvideAccountDaoFactory;
import com.pesetas.di.DatabaseModule_ProvideCategoryDaoFactory;
import com.pesetas.di.DatabaseModule_ProvideDatabaseFactory;
import com.pesetas.di.DatabaseModule_ProvideTransactionDaoFactory;
import com.pesetas.domain.repository.AccountRepository;
import com.pesetas.domain.repository.CategoryRepository;
import com.pesetas.domain.repository.SettingsRepository;
import com.pesetas.domain.repository.TransactionRepository;
import com.pesetas.ui.AppViewModel;
import com.pesetas.ui.AppViewModel_HiltModules;
import com.pesetas.ui.AppViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.AppViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.accounts.AccountsViewModel;
import com.pesetas.ui.accounts.AccountsViewModel_HiltModules;
import com.pesetas.ui.accounts.AccountsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.accounts.AccountsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.accounts.editor.AccountEditorViewModel;
import com.pesetas.ui.accounts.editor.AccountEditorViewModel_HiltModules;
import com.pesetas.ui.accounts.editor.AccountEditorViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.accounts.editor.AccountEditorViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.categories.CategoriesViewModel;
import com.pesetas.ui.categories.CategoriesViewModel_HiltModules;
import com.pesetas.ui.categories.CategoriesViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.categories.CategoriesViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.categories.editor.CategoryEditorViewModel;
import com.pesetas.ui.categories.editor.CategoryEditorViewModel_HiltModules;
import com.pesetas.ui.categories.editor.CategoryEditorViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.categories.editor.CategoryEditorViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.home.HomeViewModel;
import com.pesetas.ui.home.HomeViewModel_HiltModules;
import com.pesetas.ui.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.settings.SettingsViewModel;
import com.pesetas.ui.settings.SettingsViewModel_HiltModules;
import com.pesetas.ui.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.statistics.StatisticsViewModel;
import com.pesetas.ui.statistics.StatisticsViewModel_HiltModules;
import com.pesetas.ui.statistics.StatisticsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.statistics.StatisticsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.transactions.TransactionsViewModel;
import com.pesetas.ui.transactions.TransactionsViewModel_HiltModules;
import com.pesetas.ui.transactions.TransactionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.transactions.TransactionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.transactions.editor.TransactionEditorViewModel;
import com.pesetas.ui.transactions.editor.TransactionEditorViewModel_HiltModules;
import com.pesetas.ui.transactions.editor.TransactionEditorViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.transactions.editor.TransactionEditorViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.pesetas.ui.transfer.TransferViewModel;
import com.pesetas.ui.transfer.TransferViewModel_HiltModules;
import com.pesetas.ui.transfer.TransferViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.pesetas.ui.transfer.TransferViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerPesetasApplication_HiltComponents_SingletonC {
  private DaggerPesetasApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public PesetasApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements PesetasApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public PesetasApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements PesetasApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public PesetasApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements PesetasApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public PesetasApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements PesetasApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public PesetasApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements PesetasApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public PesetasApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements PesetasApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public PesetasApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements PesetasApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public PesetasApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends PesetasApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends PesetasApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends PesetasApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends PesetasApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(11).put(AccountEditorViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AccountEditorViewModel_HiltModules.KeyModule.provide()).put(AccountsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AccountsViewModel_HiltModules.KeyModule.provide()).put(AppViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AppViewModel_HiltModules.KeyModule.provide()).put(CategoriesViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CategoriesViewModel_HiltModules.KeyModule.provide()).put(CategoryEditorViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CategoryEditorViewModel_HiltModules.KeyModule.provide()).put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(StatisticsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, StatisticsViewModel_HiltModules.KeyModule.provide()).put(TransactionEditorViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionEditorViewModel_HiltModules.KeyModule.provide()).put(TransactionsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionsViewModel_HiltModules.KeyModule.provide()).put(TransferViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransferViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends PesetasApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AccountEditorViewModel> accountEditorViewModelProvider;

    private Provider<AccountsViewModel> accountsViewModelProvider;

    private Provider<AppViewModel> appViewModelProvider;

    private Provider<CategoriesViewModel> categoriesViewModelProvider;

    private Provider<CategoryEditorViewModel> categoryEditorViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<StatisticsViewModel> statisticsViewModelProvider;

    private Provider<TransactionEditorViewModel> transactionEditorViewModelProvider;

    private Provider<TransactionsViewModel> transactionsViewModelProvider;

    private Provider<TransferViewModel> transferViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private BackupManager backupManager() {
      return new BackupManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideDatabaseProvider.get(), singletonCImpl.bindTransactionRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.accountEditorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.accountsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.appViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.categoriesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.categoryEditorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.statisticsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.transactionEditorViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.transactionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.transferViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(11).put(AccountEditorViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) accountEditorViewModelProvider)).put(AccountsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) accountsViewModelProvider)).put(AppViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) appViewModelProvider)).put(CategoriesViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) categoriesViewModelProvider)).put(CategoryEditorViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) categoryEditorViewModelProvider)).put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) homeViewModelProvider)).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) settingsViewModelProvider)).put(StatisticsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) statisticsViewModelProvider)).put(TransactionEditorViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) transactionEditorViewModelProvider)).put(TransactionsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) transactionsViewModelProvider)).put(TransferViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) transferViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.pesetas.ui.accounts.editor.AccountEditorViewModel 
          return (T) new AccountEditorViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.bindAccountRepositoryProvider.get());

          case 1: // com.pesetas.ui.accounts.AccountsViewModel 
          return (T) new AccountsViewModel(singletonCImpl.bindAccountRepositoryProvider.get());

          case 2: // com.pesetas.ui.AppViewModel 
          return (T) new AppViewModel(singletonCImpl.bindSettingsRepositoryProvider.get());

          case 3: // com.pesetas.ui.categories.CategoriesViewModel 
          return (T) new CategoriesViewModel(singletonCImpl.bindCategoryRepositoryProvider.get());

          case 4: // com.pesetas.ui.categories.editor.CategoryEditorViewModel 
          return (T) new CategoryEditorViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.bindCategoryRepositoryProvider.get());

          case 5: // com.pesetas.ui.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.bindTransactionRepositoryProvider.get(), singletonCImpl.bindAccountRepositoryProvider.get());

          case 6: // com.pesetas.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.bindSettingsRepositoryProvider.get(), viewModelCImpl.backupManager());

          case 7: // com.pesetas.ui.statistics.StatisticsViewModel 
          return (T) new StatisticsViewModel(singletonCImpl.bindTransactionRepositoryProvider.get(), singletonCImpl.bindCategoryRepositoryProvider.get());

          case 8: // com.pesetas.ui.transactions.editor.TransactionEditorViewModel 
          return (T) new TransactionEditorViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.bindTransactionRepositoryProvider.get(), singletonCImpl.bindCategoryRepositoryProvider.get(), singletonCImpl.bindAccountRepositoryProvider.get());

          case 9: // com.pesetas.ui.transactions.TransactionsViewModel 
          return (T) new TransactionsViewModel(singletonCImpl.bindTransactionRepositoryProvider.get(), singletonCImpl.bindAccountRepositoryProvider.get(), singletonCImpl.bindCategoryRepositoryProvider.get());

          case 10: // com.pesetas.ui.transfer.TransferViewModel 
          return (T) new TransferViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.bindTransactionRepositoryProvider.get(), singletonCImpl.bindAccountRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends PesetasApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends PesetasApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends PesetasApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<PesetasDatabase> provideDatabaseProvider;

    private Provider<AccountRepositoryImpl> accountRepositoryImplProvider;

    private Provider<AccountRepository> bindAccountRepositoryProvider;

    private Provider<DataStore<Preferences>> provideDataStoreProvider;

    private Provider<SettingsRepositoryImpl> settingsRepositoryImplProvider;

    private Provider<SettingsRepository> bindSettingsRepositoryProvider;

    private Provider<CategoryRepositoryImpl> categoryRepositoryImplProvider;

    private Provider<CategoryRepository> bindCategoryRepositoryProvider;

    private Provider<TransactionRepositoryImpl> transactionRepositoryImplProvider;

    private Provider<TransactionRepository> bindTransactionRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private AccountDao accountDao() {
      return DatabaseModule_ProvideAccountDaoFactory.provideAccountDao(provideDatabaseProvider.get());
    }

    private CategoryDao categoryDao() {
      return DatabaseModule_ProvideCategoryDaoFactory.provideCategoryDao(provideDatabaseProvider.get());
    }

    private TransactionDao transactionDao() {
      return DatabaseModule_ProvideTransactionDaoFactory.provideTransactionDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<PesetasDatabase>(singletonCImpl, 1));
      this.accountRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 0);
      this.bindAccountRepositoryProvider = DoubleCheck.provider((Provider) accountRepositoryImplProvider);
      this.provideDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<DataStore<Preferences>>(singletonCImpl, 3));
      this.settingsRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 2);
      this.bindSettingsRepositoryProvider = DoubleCheck.provider((Provider) settingsRepositoryImplProvider);
      this.categoryRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 4);
      this.bindCategoryRepositoryProvider = DoubleCheck.provider((Provider) categoryRepositoryImplProvider);
      this.transactionRepositoryImplProvider = new SwitchingProvider<>(singletonCImpl, 5);
      this.bindTransactionRepositoryProvider = DoubleCheck.provider((Provider) transactionRepositoryImplProvider);
    }

    @Override
    public void injectPesetasApplication(PesetasApplication pesetasApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.pesetas.data.repository.AccountRepositoryImpl 
          return (T) new AccountRepositoryImpl(singletonCImpl.accountDao());

          case 1: // com.pesetas.data.local.PesetasDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.pesetas.data.repository.SettingsRepositoryImpl 
          return (T) new SettingsRepositoryImpl(singletonCImpl.provideDataStoreProvider.get());

          case 3: // androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> 
          return (T) DataStoreModule_ProvideDataStoreFactory.provideDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.pesetas.data.repository.CategoryRepositoryImpl 
          return (T) new CategoryRepositoryImpl(singletonCImpl.categoryDao());

          case 5: // com.pesetas.data.repository.TransactionRepositoryImpl 
          return (T) new TransactionRepositoryImpl(singletonCImpl.transactionDao(), singletonCImpl.categoryDao(), singletonCImpl.accountDao());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
