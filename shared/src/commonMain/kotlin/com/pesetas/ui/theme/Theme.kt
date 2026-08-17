package com.pesetas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pesetas.domain.model.AppTheme

@Immutable
data class PesetasColors(
    val income: Color,
    val expense: Color,
)

val LocalPesetasColors = staticCompositionLocalOf {
    PesetasColors(income = IngresoClaro, expense = GastoClaro)
}

private val LightColors = lightColorScheme(
    primary = OcreDorado,
    onPrimary = VerdeOliva,
    primaryContainer = OcreClaro,
    onPrimaryContainer = VerdeOliva,
    secondary = VerdeOliva,
    onSecondary = BlancoRoto,
    secondaryContainer = MentaClara,
    onSecondaryContainer = VerdeOliva,
    tertiary = MarronBronce,
    onTertiary = BlancoRoto,
    background = BlancoRoto,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro,
    surfaceVariant = VarianteClara,
    onSurfaceVariant = TextoTenue,
    error = TerracotaRojizo,
    onError = BlancoRoto,
    outline = MarronBronce,
    outlineVariant = OcreClaro,
)

private val DarkColors = darkColorScheme(
    primary = OcreBrillante,
    onPrimary = VerdeOlivaMuyOscuro,
    primaryContainer = MarronBronce,
    onPrimaryContainer = BlancoRoto,
    secondary = VerdeMenta,
    onSecondary = VerdeOlivaMuyOscuro,
    secondaryContainer = MentaOscura,
    onSecondaryContainer = TextoClaro,
    tertiary = OcreClaro,
    onTertiary = VerdeOlivaMuyOscuro,
    background = VerdeOlivaMuyOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = VarianteOscura,
    onSurfaceVariant = TextoClaroTenue,
    error = GastoOscuro,
    onError = VerdeOlivaMuyOscuro,
    outline = OcreClaro,
    outlineVariant = VarianteOscura,
)

@Composable
fun PesetasTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val pesetasColors = if (darkTheme) {
        PesetasColors(income = IngresoOscuro, expense = GastoOscuro)
    } else {
        PesetasColors(income = IngresoClaro, expense = GastoClaro)
    }

    CompositionLocalProvider(LocalPesetasColors provides pesetasColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PesetasTypography,
            content = content,
        )
    }
}
