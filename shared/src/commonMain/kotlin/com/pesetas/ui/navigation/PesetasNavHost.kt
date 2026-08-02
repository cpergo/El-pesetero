package com.pesetas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pesetas.AppContainer
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.model.TransactionType
import com.pesetas.ui.rememberAccountEditorViewModel
import com.pesetas.ui.rememberAccountsViewModel
import com.pesetas.ui.rememberBudgetsViewModel
import com.pesetas.ui.rememberCategoriesViewModel
import com.pesetas.ui.rememberCategoryEditorViewModel
import com.pesetas.ui.rememberCurrenciesViewModel
import com.pesetas.ui.rememberGoalEditorViewModel
import com.pesetas.ui.rememberHomeViewModel
import com.pesetas.ui.rememberRecurringEditorViewModel
import com.pesetas.ui.rememberRecurringViewModel
import com.pesetas.ui.rememberSavingsGoalsViewModel
import com.pesetas.ui.rememberSettingsViewModel
import com.pesetas.ui.rememberStatisticsViewModel
import com.pesetas.ui.rememberTransactionEditorViewModel
import com.pesetas.ui.rememberTransactionsViewModel
import com.pesetas.ui.rememberTransferViewModel
import com.pesetas.ui.about.AboutScreen
import com.pesetas.ui.currencies.CurrenciesScreen
import com.pesetas.ui.goals.SavingsGoalsScreen
import com.pesetas.ui.goals.editor.SavingsGoalEditorScreen
import com.pesetas.ui.budgets.BudgetsScreen
import com.pesetas.ui.accounts.AccountsScreen
import com.pesetas.ui.accounts.editor.AccountEditorScreen
import com.pesetas.ui.categories.CategoriesScreen
import com.pesetas.ui.categories.editor.CategoryEditorScreen
import com.pesetas.ui.home.HomeScreen
import com.pesetas.ui.recurring.RecurringScreen
import com.pesetas.ui.recurring.editor.RecurringEditorScreen
import com.pesetas.ui.settings.SettingsScreen
import com.pesetas.ui.statistics.StatisticsScreen
import com.pesetas.ui.transactions.TransactionsScreen
import com.pesetas.ui.transactions.editor.TransactionEditorScreen
import com.pesetas.ui.transfer.TransferScreen

@Composable
fun PesetasNavHost(
    container: AppContainer,
    onRestartRequired: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevel = TopLevelDestination.entries
    val isTopLevel = topLevel.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    topLevel.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigateTopLevel(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Routes.TRANSACTIONS) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(
                            Routes.transactionEditor(type = TransactionType.EXPENSE.name),
                        )
                    },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir movimiento")
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier,
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAddIncome = {
                        navController.navigate(Routes.transactionEditor(type = TransactionType.INCOME.name))
                    },
                    onAddExpense = {
                        navController.navigate(Routes.transactionEditor(type = TransactionType.EXPENSE.name))
                    },
                    modifier = Modifier.padding(padding),
                    viewModel = rememberHomeViewModel(container),
                )
            }

            composable(Routes.TRANSACTIONS) {
                TransactionsScreen(
                    onTransactionClick = { details ->
                        if (details.transaction.type == TransactionType.TRANSFER) {
                            navController.navigate(Routes.transfer(details.transaction.id))
                        } else {
                            navController.navigate(
                                Routes.transactionEditor(transactionId = details.transaction.id),
                            )
                        }
                    },
                    modifier = Modifier.padding(padding),
                    viewModel = rememberTransactionsViewModel(container),
                )
            }

            composable(Routes.STATISTICS) {
                StatisticsScreen(
                    modifier = Modifier.padding(padding),
                    viewModel = rememberStatisticsViewModel(container),
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                    onManageAccounts = { navController.navigate(Routes.ACCOUNTS) },
                    onManageBudgets = { navController.navigate(Routes.BUDGETS) },
                    onManageCurrencies = { navController.navigate(Routes.CURRENCIES) },
                    onManageGoals = { navController.navigate(Routes.GOALS) },
                    onManageRecurring = { navController.navigate(Routes.RECURRING) },
                    onAbout = { navController.navigate(Routes.ABOUT) },
                    onRestartRequired = onRestartRequired,
                    modifier = Modifier.padding(padding),
                    viewModel = rememberSettingsViewModel(container),
                )
            }

            composable(Routes.BUDGETS) {
                BudgetsScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberBudgetsViewModel(container),
                )
            }

            composable(Routes.CURRENCIES) {
                CurrenciesScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberCurrenciesViewModel(container),
                )
            }

            composable(Routes.GOALS) {
                SavingsGoalsScreen(
                    onBack = { navController.popBackStack() },
                    onAddGoal = { navController.navigate(Routes.goalEditor()) },
                    onEditGoal = { id -> navController.navigate(Routes.goalEditor(goalId = id)) },
                    viewModel = rememberSavingsGoalsViewModel(container),
                )
            }

            composable(
                route = "${Routes.GOAL_EDITOR}?${Routes.ARG_GOAL_ID}={${Routes.ARG_GOAL_ID}}",
                arguments = listOf(
                    navArgument(Routes.ARG_GOAL_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                SavingsGoalEditorScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberGoalEditorViewModel(container),
                )
            }

            composable(Routes.RECURRING) {
                RecurringScreen(
                    onBack = { navController.popBackStack() },
                    onAddRule = { navController.navigate(Routes.recurringEditor()) },
                    onEditRule = { id -> navController.navigate(Routes.recurringEditor(ruleId = id)) },
                    viewModel = rememberRecurringViewModel(container),
                )
            }

            composable(
                route = "${Routes.RECURRING_EDITOR}?${Routes.ARG_RULE_ID}={${Routes.ARG_RULE_ID}}",
                arguments = listOf(
                    navArgument(Routes.ARG_RULE_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                RecurringEditorScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberRecurringEditorViewModel(container),
                )
            }

            composable(
                route = "${Routes.TRANSACTION_EDITOR}?${Routes.ARG_TRANSACTION_ID}={${Routes.ARG_TRANSACTION_ID}}" +
                    "&${Routes.ARG_TYPE}={${Routes.ARG_TYPE}}",
                arguments = listOf(
                    navArgument(Routes.ARG_TRANSACTION_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(Routes.ARG_TYPE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                TransactionEditorScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberTransactionEditorViewModel(container),
                )
            }

            composable(
                route = "${Routes.TRANSFER}?${Routes.ARG_TRANSACTION_ID}={${Routes.ARG_TRANSACTION_ID}}",
                arguments = listOf(
                    navArgument(Routes.ARG_TRANSACTION_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                TransferScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberTransferViewModel(container),
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    onBack = { navController.popBackStack() },
                    onAddCategory = { type ->
                        navController.navigate(Routes.categoryEditor(type = type.name))
                    },
                    onEditCategory = { id -> navController.navigate(Routes.categoryEditor(categoryId = id)) },
                    viewModel = rememberCategoriesViewModel(container),
                )
            }

            composable(
                route = "${Routes.CATEGORY_EDITOR}?${Routes.ARG_CATEGORY_ID}={${Routes.ARG_CATEGORY_ID}}" +
                    "&${Routes.ARG_TYPE}={${Routes.ARG_TYPE}}",
                arguments = listOf(
                    navArgument(Routes.ARG_CATEGORY_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(Routes.ARG_TYPE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                CategoryEditorScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberCategoryEditorViewModel(container),
                )
            }

            composable(Routes.ACCOUNTS) {
                AccountsScreen(
                    onBack = { navController.popBackStack() },
                    onAddAccount = { navController.navigate(Routes.accountEditor()) },
                    onEditAccount = { id -> navController.navigate(Routes.accountEditor(accountId = id)) },
                    onNewTransfer = { navController.navigate(Routes.transfer()) },
                    viewModel = rememberAccountsViewModel(container),
                )
            }

            composable(
                route = "${Routes.ACCOUNT_EDITOR}?${Routes.ARG_ACCOUNT_ID}={${Routes.ARG_ACCOUNT_ID}}",
                arguments = listOf(
                    navArgument(Routes.ARG_ACCOUNT_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                AccountEditorScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = rememberAccountEditorViewModel(container),
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen(
                    onBack = { navController.popBackStack() },
                    externalLinks = container.externalLinks,
                )
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
