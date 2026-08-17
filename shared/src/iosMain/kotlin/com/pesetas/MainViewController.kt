package com.pesetas

import androidx.compose.runtime.key
import androidx.compose.ui.window.ComposeUIViewController
import com.pesetas.platform.IosPlatformSession
import com.pesetas.ui.AppRoot

fun MainViewController() = ComposeUIViewController {
    val container = IosPlatformSession.container
    key(container) {
        AppRoot(
            container = container,
            onRestartRequired = IosPlatformSession::reloadDatabase,
        )
    }
}
