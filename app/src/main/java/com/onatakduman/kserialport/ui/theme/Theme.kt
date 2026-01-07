package com.onatakduman.kserialport.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TerminalColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = DarkBackground,
    primaryContainer = AccentGreenDark,
    onPrimaryContainer = TextPrimary,
    secondary = AccentBlue,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentCyan,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = DarkBackground,
    outline = TextMuted,
    outlineVariant = DarkSurfaceVariant
)

@Composable
fun AndroidkserialportTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TerminalColorScheme,
        typography = Typography,
        content = content
    )
}