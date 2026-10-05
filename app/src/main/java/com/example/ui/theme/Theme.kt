package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MintSecondary,
    onPrimary = Color(0xFF003827),
    primaryContainer = NavyLight,
    onPrimaryContainer = MintLight,
    secondary = MintSecondary,
    onSecondary = Color(0xFF003827),
    secondaryContainer = Color(0xFF004F38),
    onSecondaryContainer = Color(0xFF75F8C6),
    tertiary = GoldAccent,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceDarkCard,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = StatusError
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = Color.White,
    secondary = MintSecondary,
    onSecondary = Color.White,
    secondaryContainer = MintLight,
    onSecondaryContainer = MintDark,
    tertiary = GoldAccent,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = StatusError
)

@Composable
fun AKBankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
