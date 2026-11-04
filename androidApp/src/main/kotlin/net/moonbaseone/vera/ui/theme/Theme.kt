package net.moonbaseone.vera.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Placeholder tokens — full Atkinson / teal design system lands with UI port.
private val VeraGreen = Color(0xFF1B5E20)

private val LightColors = lightColorScheme(
    primary = VeraGreen,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
)

@Composable
fun VeraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
