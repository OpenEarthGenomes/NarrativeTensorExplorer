package com.meaning.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00BCD4),      // Cián
    secondary = Color(0xFF03DAC6),    // Teal
    tertiary = Color(0xFFBB86FC),     // Lila
    background = Color(0xFF121212),   // Sötét háttér
    surface = Color(0xFF1E1E1E),      // Felület
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF005662),
    secondaryContainer = Color(0xFF004D40)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0066CC),      // Kék
    secondary = Color(0xFF66BB6A),    // Zöld
    tertiary = Color(0xFF8E24AA),     // Lila
    background = Color(0xFFF5F5F5),   // Világos háttér
    surface = Color.White,            // Fehér felület
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    primaryContainer = Color(0xFFD0E4FF),
    secondaryContainer = Color(0xFFC8E6C9)
)

@Composable
fun MeaningAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            displayLarge = androidx.compose.material3.MaterialTheme.typography.displayLarge,
            displayMedium = androidx.compose.material3.MaterialTheme.typography.displayMedium,
            displaySmall = androidx.compose.material3.MaterialTheme.typography.displaySmall,
            headlineLarge = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            headlineMedium = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            headlineSmall = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            titleLarge = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            titleMedium = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            titleSmall = androidx.compose.material3.MaterialTheme.typography.titleSmall,
            bodyLarge = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            bodyMedium = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            bodySmall = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            labelLarge = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            labelMedium = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            labelSmall = androidx.compose.material3.MaterialTheme.typography.labelSmall
        ),
        content = content
    )
}
