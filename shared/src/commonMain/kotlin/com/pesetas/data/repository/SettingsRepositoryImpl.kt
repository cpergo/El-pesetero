package com.pesetas.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pesetas.domain.model.AppTheme
import com.pesetas.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val theme: Flow<AppTheme> = dataStore.data.map { preferences ->
        preferences[THEME]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }

    override val appLockEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[APP_LOCK] ?: false
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[THEME] = theme.name }
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[APP_LOCK] = enabled }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val APP_LOCK = booleanPreferencesKey("app_lock_enabled")
    }
}
