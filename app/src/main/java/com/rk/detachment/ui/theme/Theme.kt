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

// Material Design 3 Dark Color Scheme mapped to Liquid Frosted Glass Palette
private val FrostedGlassColorScheme = darkColorScheme(
    primary = IndigoPrimary,                  // Primary action color (#6366F1)
    onPrimary = Color.White,                 // Text on primary buttons
    primaryContainer = Color(0xFF1E1B4B),    // Deep indigo container tint
    onPrimaryContainer = IndigoSoft,         // Text on primary containers
    secondary = IndigoLight,                 // Supporting interactive element accent (#818CF8)
    onSecondary = Color.White,               // Text on secondary buttons
    secondaryContainer = Color(0xFF312E81),  // Indigo secondary container backdrop
    onSecondaryContainer = Color(0xFFE0E7FF),// Text on secondary containers
    tertiary = RoseAccent,                   // Destructive/alert accent color (#F43F5E)
    onTertiary = Color.White,                // Text on tertiary actions
    tertiaryContainer = Color(0xFF4C0519),   // Deep rose container tint
    onTertiaryContainer = Color(0xFFFCE7F3), // Text on tertiary containers
    background = FrostedBackground,          // Deep obsidian liquid canvas background (#0F172A)
    onBackground = TextPrimary,              // Primary readable text (#F8FAFC)
    surface = FrostedBackground,             // Base canvas surface (#0F172A)
    onSurface = TextPrimary,                 // Primary surface text color (#F8FAFC)
    surfaceVariant = GlassSurfaceMedium,     // Translucent liquid glass container surface (~4% white)
    onSurfaceVariant = TextSecondary,        // Secondary slate body text (#94A3B8)
    outline = GlassBorderMedium,             // Crisp specular glass border outline (~9.5% white)
    error = RoseAccent,                      // Error state indicator
    onError = Color.White                    // Text on error containers
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

