package net.moonbaseone.vera.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.moonbaseone.vera.shared.ui.DesignTokens

private val PrimaryGreen = Color(DesignTokens.PRIMARY_GREEN)
private val PrimaryContainer = Color(DesignTokens.PRIMARY_CONTAINER)
private val ErrorRed = Color(DesignTokens.ERROR_RED)
private val TextDark = Color(DesignTokens.TEXT_DARK)
private val SurfaceLight = Color(DesignTokens.SURFACE_LIGHT)

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    error = ErrorRed,
    surface = SurfaceLight,
    onSurface = TextDark,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003311),
    primaryContainer = Color(0xFF1B3A20),
    error = Color(0xFFEF9A9A),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFECECEC),
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

fun statusColor(fhirStatus: String?): Color =
    Color(DesignTokens.statusColorArgb(fhirStatus))
