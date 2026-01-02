package com.example.llamadrama.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Obsidian-inspired dark color scheme
private val ObsidianColorScheme = darkColorScheme(
    primary = ObsidianPurple,
    onPrimary = Color.White,
    primaryContainer = ObsidianPurpleDark,
    onPrimaryContainer = ObsidianPurpleLight,
    
    secondary = ObsidianPurpleLight,
    onSecondary = Color.Black,
    
    background = ObsidianBackground,
    onBackground = ObsidianTextPrimary,
    
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = ObsidianTextSecondary,
    
    error = ObsidianRed,
    errorContainer = ObsidianErrorContainer,
    onErrorContainer = ObsidianOnErrorContainer,
    
    outline = ObsidianTextSecondary.copy(alpha = 0.5f)
)

@Composable
fun LlamaDramaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ObsidianColorScheme
    
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