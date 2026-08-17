package com.pesetas.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.pesetas.ui.components.AppButton
import com.pesetas.platform.DeviceAuthenticator
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    authenticator: DeviceAuthenticator,
    onUnlocked: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var authenticationUnavailable by remember { mutableStateOf(false) }

    fun requestUnlock() {
        scope.launch {
            if (authenticateForUnlock(authenticator) { authenticationUnavailable = !it }) onUnlocked()
        }
    }

    LaunchedEffect(Unit) {
        if (authenticateForUnlock(authenticator) { authenticationUnavailable = !it }) onUnlocked()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
            )
        }
        Text(
            text = "El pesetero está bloqueado",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        if (authenticationUnavailable) {
            Text(
                text = "Configura un código, patrón o biometría en los ajustes del dispositivo y vuelve a intentarlo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        AppButton(text = "Desbloquear", onClick = { requestUnlock() })
    }
}

internal suspend fun authenticateForUnlock(
    authenticator: DeviceAuthenticator,
    onAvailabilityChecked: (Boolean) -> Unit = {},
): Boolean {
    val available = authenticator.isAvailable()
    onAvailabilityChecked(available)
    return available && authenticator.authenticate()
}
