package com.nextbrowser.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = NextBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7ECFF),
    onPrimaryContainer = Color(0xFF14275E),
    background = LightBackground,
    surface = LightSurface
)

private val DarkColors = darkColorScheme(
    primary = NextBlueDark,
    primaryContainer = Color(0xFF26345F),
    onPrimaryContainer = Color(0xFFE0E6FF),
    background = DarkBackground,
    surface = DarkSurface
)

@Composable
fun NextBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    val colorScheme = if (accentColor == null) {
        baseScheme
    } else {
        baseScheme.copy(primary = accentColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
