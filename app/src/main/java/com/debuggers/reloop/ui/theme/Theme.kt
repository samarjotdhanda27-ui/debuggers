package com.debuggers.reloop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandLime,
    onPrimary = BrandLimeDark,
    secondary = BrandMint,
    onSecondary = BrandForest,
    background = Color(0xFF101713),
    onBackground = Color(0xFFF1F5F2),
    surface = Color(0xFF16211B),
    onSurface = Color(0xFFF1F5F2),
    surfaceVariant = Color(0xFF1C2C24),
    onSurfaceVariant = Color(0xFF9EABA2),
    outline = Color(0xFF283D32)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandForest,
    onPrimary = Color.White,
    secondary = BrandLime,
    onSecondary = BrandLimeDark,
    tertiary = BrandMint,
    background = ReLoopPageBg,
    onBackground = TextDark,
    surface = ReLoopCardBg,
    onSurface = TextDark,
    surfaceVariant = ReLoopSurfaceSubtle,
    onSurfaceVariant = TextMuted,
    outline = ReLoopBorder,
    error = AlertRed,
    onError = Color.White
)

@Composable
fun ReLoopTheme(
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
