package com.moneysnap.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryPink = Color(0xFFD64D64)
val SurfaceGrey = Color(0xFFF5F5F5)
val TextDark = Color(0xFF1A1A1A)
val TextLight = Color(0xFF808080)
val BackgroundWhite = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    background = BackgroundWhite,
    surface = SurfaceGrey,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun MoneySnapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
