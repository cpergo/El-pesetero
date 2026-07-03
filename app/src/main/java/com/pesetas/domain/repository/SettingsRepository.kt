package com.pesetas.domain.repository

import com.pesetas.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val theme: Flow<AppTheme>
    val appLockEnabled: Flow<Boolean>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setAppLockEnabled(enabled: Boolean)
}
