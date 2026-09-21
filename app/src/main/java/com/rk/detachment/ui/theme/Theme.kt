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

// Material Design 3 Dark Color Scheme mapped to Liquid Frosted Glass Palette with Purple Accents
private val FrostedGlassColorScheme = darkColorScheme(
    primary = PurplePrimary,                  // Vibrant purple (#7C5CF0) - primary action buttons & active toggles
    onPrimary = Color.White,                 // Pure white - text/icons on primary buttons
    primaryContainer = PurpleDark,           // Deep slate-purple (#4C338A) - active container & card tinting
    onPrimaryContainer = PurpleSoft,         // Soft lilac (#D6C8FB) - text/labels on primary containers
    secondary = PurpleLight,                 // Light lavender (#9E86E8) - secondary icons, badges & highlights
    onSecondary = Color.White,               // Pure white - text/icons on secondary buttons
    secondaryContainer = Color(0xFF38296B),  // Midnight purple (#38296B) - secondary card & row backgrounds
    onSecondaryContainer = Color(0xFFEDE9FE),// Pale lilac (#EDE9FE) - text on secondary containers
    tertiary = RoseAccent,                   // Vivid rose red (#F43F5E) - block overlays & danger/alert actions
    onTertiary = Color.White,                // Pure white - text/icons on tertiary action buttons
    tertiaryContainer = Color(0xFF4C0519),   // Deep wine red (#4C0519) - alert containers & emergency cards
    onTertiaryContainer = Color(0xFFFCE7F3), // Soft pink (#FCE7F3) - text/icons on alert containers
    background = FrostedBackground,          // Deep obsidian navy (#0F172A) - main screen canvas background
    onBackground = TextPrimary,              // Off-white (#F8FAFC) - primary readable text across screens
    surface = FrostedBackground,             // Deep obsidian navy (#0F172A) - dialogs, bottom sheets & base surfaces
    onSurface = TextPrimary,                 // Off-white (#F8FAFC) - primary surface text & headers
    surfaceVariant = GlassSurfaceMedium,     // Translucent glass white (~3%) - list item cards & row surfaces
    onSurfaceVariant = TextSecondary,        // Muted slate (#94A3B8) - subtitles, descriptions & helper text
    outline = GlassBorderMedium,             // Translucent glass rim (~13.5% white) - card borders & dividers
    error = RoseAccent,                      // Vivid rose red (#F43F5E) - error messages & input validation alerts
    onError = Color.White                    // Pure white - text/icons on error surfaces
)

@Composable
fun DetachmentTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = FrostedBackground.toArgb()
            @Suppress("DEPRECATION")
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

