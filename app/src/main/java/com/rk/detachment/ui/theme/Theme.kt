package com.rk.detachment.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FrostedGlassColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E1B4B),
    onPrimaryContainer = IndigoSoft,
    secondary = IndigoLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = RoseAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C0519),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = FrostedBackground,
    onBackground = TextPrimary,
    surface = FrostedBackground,
    onSurface = TextPrimary,
    surfaceVariant = Color(0x14FFFFFF),
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderMedium,
    error = RoseAccent,
    onError = Color.White
)

@Composable
fun DetachmentTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FrostedBackground.toArgb()
            window.navigationBarColor = FrostedBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = FrostedGlassColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = DetachmentTheme(darkTheme, content)
