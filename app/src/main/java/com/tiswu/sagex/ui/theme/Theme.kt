package com.tiswu.sagex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// WeChat palette
private val WeChatGreen = Color(0xFF07C160)
private val AppBackground = Color(0xFFEDEDED)
private val CellSurface = Color(0xFFFFFFFF)
private val UnreadRed = Color(0xFFFA5151)
private val TextPrimary = Color(0xFF191919)
private val TextSecondary = Color(0xFF888888)

private val LightColors = lightColorScheme(
    primary = WeChatGreen,
    onPrimary = Color.White,
    secondary = WeChatGreen,
    onSecondary = Color.White,
    tertiary = WeChatGreen,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CellSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppBackground,
    onSurfaceVariant = TextSecondary,
    error = UnreadRed,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = WeChatGreen,
    onPrimary = Color.Black,
    secondary = WeChatGreen,
    background = Color(0xFF111111),
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFF9A9A9A),
    error = UnreadRed,
)

@Composable
fun SageXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
