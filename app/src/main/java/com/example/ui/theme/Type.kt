package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Editorial Serif for cinematic titles and headings
val EditorialSerif = FontFamily.Serif
val ScreenplayMono = FontFamily.Monospace
val InterfaceSans = FontFamily.SansSerif

val StillframeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.25.sp,
        color = TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = EditorialSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        color = TextSecondary
    ),
    bodyLarge = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = TextTertiary
    ),
    labelLarge = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.8.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = InterfaceSans,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = TextTertiary
    )
)
