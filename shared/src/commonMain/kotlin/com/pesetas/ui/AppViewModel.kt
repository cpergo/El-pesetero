package com.pesetas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.AppTheme
import com.pesetas.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AppUiState(
    val isLoading: Boolean = true,
    val theme: AppTheme = AppTheme.SYSTEM,
    val appLockEnabled: Boolean = false,
)

class AppViewModel(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState = combine(
        settingsRepository.theme,
        settingsRepository.appLockEnabled,
    ) { theme, lock ->
        AppUiState(isLoading = false, theme = theme, appLockEnabled = lock)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppUiState(),
    )
}
