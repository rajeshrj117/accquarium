package com.premium.aquarium.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AquaColorScheme = darkColorScheme(
    primary = Color(0xFF00B4D8),
    secondary = Color(0xFF0077B6),
    tertiary = Color(0xFF48CAE4),
    background = Color(0xFF001428),
    surface = Color(0xFF001E3C)
)

@Composable
fun AquariumTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AquaColorScheme, content = content)
}
