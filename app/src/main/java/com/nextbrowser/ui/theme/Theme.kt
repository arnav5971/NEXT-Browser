package com.nextbrowser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = NextBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE7ECFF),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF14275E),
    background = LightBackground,
    surface = LightSurface
)

private val DarkColors = darkColorScheme(
    primary = NextBlueDark,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF26345F),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFE0E6FF),
    background = DarkBackground,
    surface = DarkSurface
)

@Composable
fun NextBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
