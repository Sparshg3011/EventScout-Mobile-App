package com.example.eventfinder.ui.theme

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

// Light palette tuned to the provided UI (soft blue headers, neutral backgrounds)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFCED8F2),        // toolbar / tab background
    onPrimary = Color(0xFF1F2A44),      // text on primary
    primaryContainer = Color(0xFFDDE5F7),
    onPrimaryContainer = Color(0xFF1F2A44),
    secondary = Color(0xFF6C788F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2F4F8),
    onSecondaryContainer = Color(0xFF1F2A44),
    background = Color(0xFFF9F9FB),
    onBackground = Color(0xFF1F2A44),
    surface = Color(0xFFF9F9FB),
    onSurface = Color(0xFF1F2A44),
    surfaceVariant = Color(0xFFE6E9F0),
    onSurfaceVariant = Color(0xFF4A5568),
    outline = Color(0xFFCBD5E1),
    tertiary = Color(0xFF5AC6C2),       // accent (logo tint)
    onTertiary = Color.White,
    error = Color(0xFFB3261E)
)

private val DarkColorScheme = darkColorScheme() // fallback to Material defaults in dark mode

@Composable
fun EventFinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
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
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
