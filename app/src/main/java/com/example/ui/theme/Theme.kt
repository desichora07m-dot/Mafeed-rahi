package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003A09),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFFF57F17),
    onSecondaryContainer = Color(0xFFFFECB3),
    tertiary = Color(0xFFFF8A65),
    background = AppBackgroundDark,
    surface = AppSurfaceDark,
    surfaceVariant = AppSurfaceVariantDark,
    onBackground = AppTextPrimaryDark,
    onSurface = AppTextPrimaryDark,
    outline = AppOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = TractorGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = TractorGreenContainer,
    onPrimaryContainer = OnTractorGreenContainer,
    secondary = TractorGold,
    onSecondary = Color.White,
    secondaryContainer = TractorGoldContainer,
    onSecondaryContainer = OnTractorGoldContainer,
    tertiary = TractorClay,
    background = AppBackgroundLight,
    surface = AppSurfaceLight,
    surfaceVariant = AppSurfaceVariantLight,
    onBackground = AppTextPrimary,
    onSurface = AppTextPrimary,
    outline = AppOutline
)

@Composable
fun TractorHisabTheme(
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
