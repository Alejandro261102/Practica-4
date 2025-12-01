package com.example.gestorarchivosipn.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- COLORES ---

// Tema Guinda (IPN)
val GuindaIPN = Color(0xFF6C1D45)
val GuindaDark = Color(0xFF4A122E)
val GuindaLight = Color(0xFF9E486A)

// Tema Azul (ESCOM)
val AzulESCOM = Color(0xFF0067C6)
val AzulDark = Color(0xFF004585)
val AzulLight = Color(0xFF5CA3F4)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF121212)

// Definición de esquemas
private val DarkGuindaScheme = darkColorScheme(
    primary = GuindaLight,
    secondary = GuindaIPN,
    tertiary = Color(0xFFEFB8C8),
    background = Black,
    surface = Color(0xFF1E1E1E)
)

private val LightGuindaScheme = lightColorScheme(
    primary = GuindaIPN,
    secondary = GuindaDark,
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    surface = White
)

private val DarkAzulScheme = darkColorScheme(
    primary = AzulLight,
    secondary = AzulESCOM,
    tertiary = Color(0xFFB8D6EF),
    background = Black,
    surface = Color(0xFF1E1E1E)
)

private val LightAzulScheme = lightColorScheme(
    primary = AzulESCOM,
    secondary = AzulDark,
    tertiary = Color(0xFF52687D),
    background = Color(0xFFFFFBFE),
    surface = White
)

enum class AppThemeType {
    GUINDA_IPN,
    AZUL_ESCOM
}

@Composable
fun GestorArchivosIPNTheme(
    themeType: AppThemeType = AppThemeType.GUINDA_IPN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Selección de esquema según tema y modo oscuro
    val colorScheme = when (themeType) {
        AppThemeType.GUINDA_IPN -> if (darkTheme) DarkGuindaScheme else LightGuindaScheme
        AppThemeType.AZUL_ESCOM -> if (darkTheme) DarkAzulScheme else LightAzulScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de que exista Typography o usa el default
        content = content
    )
}