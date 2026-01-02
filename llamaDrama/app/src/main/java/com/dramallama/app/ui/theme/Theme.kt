package com.dramallama.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Modern dark color scheme with teal accent
private val LlamaDramaColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = Color.Black,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLight,
    
    secondary = TealLight,
    onSecondary = Color.Black,
    
    background = ObsidianBackground,
    onBackground = ObsidianTextPrimary,
    
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianTextSecondary,
    
    error = StatusCoral,
    errorContainer = CoralContainer,
    onErrorContainer = OnCoralContainer,
    
    outline = ObsidianTextSecondary.copy(alpha = 0.5f)
)

@Composable
fun LlamaDramaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LlamaDramaColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}