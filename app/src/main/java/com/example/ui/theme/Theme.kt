package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Stillframe True-Black AMOLED Theme
val StillframeDarkColorScheme = darkColorScheme(
    primary = AccentAmber,
    onPrimary = Color(0xFF140D02),
    primaryContainer = Color(0xFF2E200C),
    onPrimaryContainer = Color(0xFFFFDFA6),

    secondary = AccentSteelBlue,
    onSecondary = Color(0xFF031633),
    secondaryContainer = Color(0xFF16253D),
    onSecondaryContainer = Color(0xFFCBE0FF),

    tertiary = AccentSage,
    onTertiary = Color(0xFF042111),
    tertiaryContainer = Color(0xFF183321),
    onTertiaryContainer = Color(0xFFBCE3C9),

    background = StillframeBlack,
    onBackground = TextPrimary,

    surface = StillframeSurface0,
    onSurface = TextPrimary,
    surfaceVariant = StillframeSurface1,
    onSurfaceVariant = TextSecondary,

    surfaceContainer = StillframeSurface1,
    surfaceContainerHigh = StillframeSurface2,
    surfaceContainerHighest = StillframeSurface3,

    outline = StillframeBorder,
    outlineVariant = StillframeBorderSubtle,

    error = AccentCrimson,
    onError = Color.White
)

@Composable
fun StillframeTheme(
    accentColor: Color = AccentAmber,
    content: @Composable () -> Unit
) {
    val dynamicScheme = StillframeDarkColorScheme.copy(
        primary = accentColor
    )

    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = StillframeTypography,
        content = content
    )
}
