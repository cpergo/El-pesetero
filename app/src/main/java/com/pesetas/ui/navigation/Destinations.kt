package com.pesetas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"

    const val TRANSACTION_EDITOR = "transaction_editor"
    const val TRANSFER = "transfer"
    const val CATEGORIES = "categories"
    const val CATEGORY_EDITOR = "category_editor"
    const val ACCOUNTS = "accounts"
    const val ACCOUNT_EDITOR = "account_editor"
    const val ABOUT = "about"

    const val ARG_TRANSACTION_ID = "transactionId"
    const val ARG_CATEGORY_ID = "categoryId"
    const val ARG_ACCOUNT_ID = "accountId"
    const val ARG_TYPE = "type"

    fun transactionEditor(transactionId: Long? = null, type: String? = null): String {
        val id = transactionId ?: -1L
        val typeArg = type ?: ""
        return "$TRANSACTION_EDITOR?$ARG_TRANSACTION_ID=$id&$ARG_TYPE=$typeArg"
    }

    fun categoryEditor(categoryId: Long? = null, type: String? = null): String {
        val id = categoryId ?: -1L
        val typeArg = type ?: ""
        return "$CATEGORY_EDITOR?$ARG_CATEGORY_ID=$id&$ARG_TYPE=$typeArg"
    }

    fun accountEditor(accountId: Long? = null): String {
        val id = accountId ?: -1L
        return "$ACCOUNT_EDITOR?$ARG_ACCOUNT_ID=$id"
    }

    fun transfer(transactionId: Long? = null): String {
        val id = transactionId ?: -1L
        return "$TRANSFER?$ARG_TRANSACTION_ID=$id"
    }
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Inicio", Icons.Filled.Home),
    TRANSACTIONS(Routes.TRANSACTIONS, "Movimientos", Icons.AutoMirrored.Filled.ListAlt),
    STATISTICS(Routes.STATISTICS, "Estadísticas", Icons.Filled.BarChart),
    SETTINGS(Routes.SETTINGS, "Ajustes", Icons.Filled.Settings),
}
