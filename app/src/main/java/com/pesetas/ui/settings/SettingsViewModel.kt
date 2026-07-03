package com.pesetas.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.data.backup.BackupManager
import com.pesetas.domain.model.AppTheme
import com.pesetas.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val appLockEnabled: Boolean = false,
)

sealed interface SettingsEvent {
    data class Message(val text: String) : SettingsEvent
    data object RestartRequired : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager,
) : ViewModel() {

    val uiState = combine(
        settingsRepository.theme,
        settingsRepository.appLockEnabled,
    ) { theme, lock ->
        SettingsUiState(theme = theme, appLockEnabled = lock)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAppLockEnabled(enabled) }
    }

    fun exportCsv(target: Uri) {
        viewModelScope.launch {
            runCatching { backupManager.exportCsv(target) }
                .onSuccess { emit(SettingsEvent.Message("Movimientos exportados a CSV")) }
                .onFailure { emit(SettingsEvent.Message("No se pudo exportar: ${it.message}")) }
        }
    }

    fun exportDatabase(target: Uri) {
        viewModelScope.launch {
            runCatching { backupManager.exportDatabase(target) }
                .onSuccess { emit(SettingsEvent.Message("Copia de seguridad guardada")) }
                .onFailure { emit(SettingsEvent.Message("No se pudo guardar la copia: ${it.message}")) }
        }
    }

    fun importDatabase(source: Uri) {
        viewModelScope.launch {
            runCatching { backupManager.importDatabase(source) }
                .onSuccess { emit(SettingsEvent.RestartRequired) }
                .onFailure { emit(SettingsEvent.Message("No se pudo importar: ${it.message}")) }
        }
    }

    private suspend fun emit(event: SettingsEvent) = _events.emit(event)
}
