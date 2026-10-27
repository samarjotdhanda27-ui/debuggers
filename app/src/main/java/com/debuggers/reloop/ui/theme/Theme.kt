package com.debuggers.reloop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReloopColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandMintBg,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandEmerald,
    onSecondary = Color.White,
    tertiary = BrandLime,
    onTertiary = Color.White,
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
    darkTheme: Boolean = false, // Enforces high-contrast, crystal-clear B2B marketplace styling across all devices
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReloopColorScheme,
        typography = Typography,
        content = content
    )
}
