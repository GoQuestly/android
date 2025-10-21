package com.goquesty.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = Green11,
    secondary = GreenDC,
    tertiary = Green10,
    surface = Black1F1,
    onSurface = White,
    onSurfaceVariant = Gray9C,
    primaryContainer = Black1F2,
    onPrimaryContainer = WhiteF6,
    outline = Gray4B,
    error = RedFC,
    onTertiary = Green11,
    surfaceVariant = Black10,
    secondaryContainer = Black18
)

private val LightColorScheme = lightColorScheme(
    primary = Green11,
    secondary = GreenDC,
    tertiary = GreenCF,
    surface = WhiteF6,
    onSurface = Black11,
    onSurfaceVariant = Gray4B,
    primaryContainer = White,
    onPrimaryContainer = Black10,
    outline = GrayD1,
    error = RedB9,
    onTertiary = Black11,
    surfaceVariant = White,
    secondaryContainer = White
)

@Composable
fun GoquestlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides colorScheme.onSurface,
            content = content
        )
    }
}