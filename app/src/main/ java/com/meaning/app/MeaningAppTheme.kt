package com.meaning.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBlue = Color(0xFF213A58)
val MidBlue = Color(0xFF0C6478)
val Teal = Color(0xFF15919B)
val LightTeal = Color(0xFF09D1C7)
val BrightGreen = Color(0xFF80EE98)

private val DarkColorScheme = darkColorScheme(
    primary = BrightGreen,
    secondary = LightTeal,
    background = DarkBlue,
    surface = MidBlue,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MeaningAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
