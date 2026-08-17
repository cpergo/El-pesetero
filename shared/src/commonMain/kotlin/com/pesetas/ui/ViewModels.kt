package com.pesetas.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pesetas.AppContainer
import com.pesetas.ui.accounts.AccountsViewModel
import com.pesetas.ui.accounts.editor.AccountEditorViewModel
import com.pesetas.ui.budgets.BudgetsViewModel
import com.pesetas.ui.categories.CategoriesViewModel
import com.pesetas.ui.categories.editor.CategoryEditorViewModel
import com.pesetas.ui.currencies.CurrenciesViewModel
import com.pesetas.ui.goals.SavingsGoalsViewModel
import com.pesetas.ui.goals.editor.SavingsGoalEditorViewModel
import com.pesetas.ui.home.HomeViewModel
import com.pesetas.ui.recurring.RecurringViewModel
import com.pesetas.ui.recurring.editor.RecurringEditorViewModel
import com.pesetas.ui.settings.SettingsViewModel
import com.pesetas.ui.statistics.StatisticsViewModel
import com.pesetas.ui.transactions.TransactionsViewModel
import com.pesetas.ui.transactions.editor.TransactionEditorViewModel
import com.pesetas.ui.transfer.TransferViewModel

@Composable
fun rememberAppViewModel(container: AppContainer): AppViewModel =
    rememberViewModel { AppViewModel(container.settingsRepository) }

@Composable
fun rememberHomeViewModel(container: AppContainer): HomeViewModel = rememberViewModel {
    HomeViewModel(
        container.transactionRepository,
        container.accountRepository,
        container.budgetRepository,
        container.recurringRepository,
    )
}

@Composable
fun rememberTransactionsViewModel(container: AppContainer): TransactionsViewModel = rememberViewModel {
    TransactionsViewModel(
        container.transactionRepository,
        container.accountRepository,
        container.categoryRepository,
        container.tagRepository,
    )
}

@Composable
fun rememberStatisticsViewModel(container: AppContainer): StatisticsViewModel = rememberViewModel {
    StatisticsViewModel(
        container.transactionRepository,
        container.categoryRepository,
        container.tagRepository,
    )
}

@Composable
fun rememberSettingsViewModel(container: AppContainer): SettingsViewModel = rememberViewModel {
    SettingsViewModel(container.settingsRepository, container.backupManager, container.authenticator)
}

@Composable
fun rememberBudgetsViewModel(container: AppContainer): BudgetsViewModel = rememberViewModel {
    BudgetsViewModel(
        container.categoryRepository,
        container.transactionRepository,
        container.budgetRepository,
    )
}

@Composable
fun rememberCurrenciesViewModel(container: AppContainer): CurrenciesViewModel = rememberViewModel {
    CurrenciesViewModel(container.accountRepository, container.currencyRepository)
}

@Composable
fun rememberSavingsGoalsViewModel(container: AppContainer): SavingsGoalsViewModel = rememberViewModel {
    SavingsGoalsViewModel(container.savingsGoalRepository)
}

@Composable
fun rememberRecurringViewModel(container: AppContainer): RecurringViewModel = rememberViewModel {
    RecurringViewModel(container.recurringRepository)
}

@Composable
fun rememberCategoriesViewModel(container: AppContainer): CategoriesViewModel = rememberViewModel {
    CategoriesViewModel(container.categoryRepository, container.budgetRepository)
}

@Composable
fun rememberAccountsViewModel(container: AppContainer): AccountsViewModel = rememberViewModel {
    AccountsViewModel(container.accountRepository, container.currencyRepository)
}

@Composable
fun rememberGoalEditorViewModel(container: AppContainer): SavingsGoalEditorViewModel =
    rememberSavedStateViewModel { state ->
        SavingsGoalEditorViewModel(state, container.savingsGoalRepository)
    }

@Composable
fun rememberRecurringEditorViewModel(container: AppContainer): RecurringEditorViewModel =
    rememberSavedStateViewModel { state ->
        RecurringEditorViewModel(
            state,
            container.recurringRepository,
            container.categoryRepository,
            container.accountRepository,
        )
    }

@Composable
fun rememberTransactionEditorViewModel(container: AppContainer): TransactionEditorViewModel =
    rememberSavedStateViewModel { state ->
        TransactionEditorViewModel(
            state,
            container.transactionRepository,
            container.categoryRepository,
            container.accountRepository,
            container.receiptImageStore,
            container.tagRepository,
        )
    }

@Composable
fun rememberTransferViewModel(container: AppContainer): TransferViewModel =
    rememberSavedStateViewModel { state ->
        TransferViewModel(state, container.transactionRepository, container.accountRepository)
    }

@Composable
fun rememberCategoryEditorViewModel(container: AppContainer): CategoryEditorViewModel =
    rememberSavedStateViewModel { state ->
        CategoryEditorViewModel(state, container.categoryRepository)
    }

@Composable
fun rememberAccountEditorViewModel(container: AppContainer): AccountEditorViewModel =
    rememberSavedStateViewModel { state ->
        AccountEditorViewModel(state, container.accountRepository)
    }

@Composable
private inline fun <reified VM : ViewModel> rememberViewModel(
    crossinline create: () -> VM,
): VM = viewModel(
    factory = viewModelFactory {
        initializer { create() }
    },
)

@Composable
private inline fun <reified VM : ViewModel> rememberSavedStateViewModel(
    crossinline create: (SavedStateHandle) -> VM,
): VM = viewModel(
    factory = viewModelFactory {
        initializer { create(createSavedStateHandle()) }
    },
)
