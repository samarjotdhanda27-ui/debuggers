package com.debuggers.reloop.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandForest,
    onPrimary = Color.White,
    primaryContainer = BrandMintBg,
    onPrimaryContainer = BrandDarkGreen,
    secondary = BrandEmerald,
    onSecondary = Color.White,
    tertiary = BrandLime,
    onTertiary = BrandDarkGreen,
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

private val DarkColorScheme = darkColorScheme(
    primary = BrandEmerald,
    onPrimary = Color.White,
    primaryContainer = BrandDarkGreen,
    onPrimaryContainer = Color.White,
    secondary = BrandLime,
    onSecondary = BrandDarkGreen,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
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
