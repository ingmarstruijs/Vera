package net.moonbaseone.vera.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.moonbaseone.vera.shared.ui.DesignTokens

private val PrimaryGreen = Color(DesignTokens.PRIMARY_GREEN)
private val PrimaryContainer = Color(DesignTokens.PRIMARY_CONTAINER)
private val ErrorRed = Color(DesignTokens.ERROR_RED)
private val TextDark = Color(DesignTokens.TEXT_DARK)
private val TextSecondary = Color(DesignTokens.TEXT_SECONDARY)
private val SurfaceLight = Color(DesignTokens.SURFACE_LIGHT)
private val OutlineSubtle = Color(DesignTokens.OUTLINE_SUBTLE)

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = TextDark,
    error = ErrorRed,
    surface = SurfaceLight,
    onSurface = TextDark,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSubtle,
    background = SurfaceLight,
    onBackground = TextDark,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9BC4B5),
    onPrimary = Color(0xFF0C2A22),
    primaryContainer = Color(0xFF1E3F36),
    onPrimaryContainer = Color(0xFFD8EDE6),
    error = Color(0xFFF2B8B5),
    surface = Color(0xFF121917),
    onSurface = Color(0xFFE4ECE9),
    onSurfaceVariant = Color(0xFFB7C4BF),
    outline = Color(0xFF6B7A74),
    background = Color(0xFF121917),
    onBackground = Color(0xFFE4ECE9),
)

/** Slightly calmer type scale: clearer hierarchy for clinical reading. */
private val VeraTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
    ),
)

@Composable
fun VeraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = VeraTypography,
        content = content,
    )
}

fun statusColor(fhirStatus: String?): Color =
    Color(DesignTokens.statusColorArgb(fhirStatus))
