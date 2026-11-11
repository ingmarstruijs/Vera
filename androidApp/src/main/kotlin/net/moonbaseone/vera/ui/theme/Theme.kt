package net.moonbaseone.vera.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
    errorContainer = Color(DesignTokens.BANNER_ERROR_BG),
    onErrorContainer = ErrorRed,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE4EBE8),
    onSurfaceVariant = TextSecondary,
    outline = OutlineSubtle,
    outlineVariant = OutlineSubtle,
    background = SurfaceLight,
    onBackground = TextDark,
)

private val DarkColors = darkColorScheme(
    primary = Color(DesignTokens.PRIMARY_GREEN_DARK),
    onPrimary = Color(DesignTokens.ON_PRIMARY_DARK),
    primaryContainer = Color(DesignTokens.PRIMARY_CONTAINER_DARK),
    onPrimaryContainer = Color(DesignTokens.ON_PRIMARY_CONTAINER_DARK),
    error = Color(DesignTokens.ERROR_RED_DARK),
    errorContainer = Color(DesignTokens.BANNER_ERROR_BG_DARK),
    onErrorContainer = Color(DesignTokens.ERROR_RED_DARK),
    surface = Color(DesignTokens.SURFACE_DARK),
    onSurface = Color(DesignTokens.TEXT_ON_DARK),
    surfaceVariant = Color(DesignTokens.SURFACE_VARIANT_DARK),
    onSurfaceVariant = Color(DesignTokens.TEXT_SECONDARY_DARK),
    outline = Color(DesignTokens.OUTLINE_DARK),
    outlineVariant = Color(DesignTokens.OUTLINE_VARIANT_DARK),
    background = Color(DesignTokens.SURFACE_DARK),
    onBackground = Color(DesignTokens.TEXT_ON_DARK),
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
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VeraTypography,
        content = content,
    )
}

@Composable
fun statusColor(fhirStatus: String?): Color =
    Color(DesignTokens.statusColorArgb(fhirStatus, dark = isSystemInDarkTheme()))

@Composable
fun warningBannerBackground(): Color =
    Color(
        if (isSystemInDarkTheme()) DesignTokens.BANNER_WARNING_BG_DARK
        else DesignTokens.BANNER_WARNING_BG,
    )

@Composable
fun warningBannerForeground(): Color =
    Color(if (isSystemInDarkTheme()) DesignTokens.AMBER_DARK else DesignTokens.AMBER)

@Composable
fun errorBannerBackground(): Color =
    Color(
        if (isSystemInDarkTheme()) DesignTokens.BANNER_ERROR_BG_DARK
        else DesignTokens.BANNER_ERROR_BG,
    )

@Composable
fun sentinelBackground(): Color =
    Color(
        if (isSystemInDarkTheme()) DesignTokens.SENTINEL_BG_DARK
        else DesignTokens.SENTINEL_BG,
    )

@Composable
fun sentinelAccent(): Color =
    Color(
        if (isSystemInDarkTheme()) DesignTokens.SENTINEL_GREEN_DARK
        else DesignTokens.SENTINEL_GREEN,
    )
