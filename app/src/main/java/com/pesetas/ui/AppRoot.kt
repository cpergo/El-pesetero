package com.pesetas.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pesetas.ui.navigation.PesetasNavHost
import com.pesetas.ui.security.AppLockScreen
import com.pesetas.ui.theme.PesetasTheme

@Composable
fun AppRoot(
    onRestartRequired: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val state by appViewModel.uiState.collectAsStateWithLifecycle()
    var unlocked by rememberSaveable { mutableStateOf(false) }

    PesetasTheme(appTheme = state.theme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (state.appLockEnabled && !unlocked) {
                AppLockScreen(onUnlocked = { unlocked = true })
            } else {
                PesetasNavHost(onRestartRequired = onRestartRequired)
            }
        }
    }
}
