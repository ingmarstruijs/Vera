package net.moonbaseone.vera.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.theme.lavenderWash
import net.moonbaseone.vera.ui.theme.mintWash

private val CardShape = RoundedCornerShape(DesignTokens.CARD_RADIUS_DP.dp)

/**
 * Single segmented toggle: choose one of two modes (e.g. Eenvoudig / Origineel).
 * One shared outline; the active segment is filled.
 */
@Composable
fun ModeSegmentedToggle(
    leftLabel: String,
    rightLabel: String,
    rightSelected: Boolean,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    val outline = MaterialTheme.colorScheme.secondary
    Surface(
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, outline),
        modifier = modifier,
    ) {
        Row {
            ModeSegment(
                label = leftLabel,
                selected = !rightSelected,
                onClick = onLeft,
            )
            ModeSegment(
                label = rightLabel,
                selected = rightSelected,
                onClick = onRight,
            )
        }
    }
}

@Composable
private fun ModeSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = label
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Soft mint → aqua → lavender atmosphere behind header + timeline.
 * Stronger in the upper band (app header / phase chips), fading to surface
 * so detail cards stay crisp — matches the mock “graceful wash”.
 */
@Composable
fun SoftScreenBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val dark = isSystemInDarkTheme()
    val mint = mintWash()
    val lavender = lavenderWash()
    val surface = MaterialTheme.colorScheme.surface
    val aqua = if (dark) {
        Color(DesignTokens.PRIMARY_CONTAINER_DARK).copy(alpha = 0.55f)
    } else {
        Color(0xFFD7F3F4)
    }
    val topMint = if (dark) mint.copy(alpha = 0.55f) else Color(0xFFE4F7F3)
    val topLav = if (dark) lavender.copy(alpha = 0.5f) else Color(0xFFEDE7F8)
    val blobLav = if (dark) lavender.copy(alpha = 0.4f) else Color(0xFFD9CFF2).copy(alpha = 0.9f)
    val blobMint = if (dark) mint.copy(alpha = 0.35f) else Color(0xFFBFE8E2).copy(alpha = 0.75f)
    val blobAqua = if (dark) aqua else Color(0xFFC8EEF2).copy(alpha = 0.7f)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Base vertical wash — vivid in the header/timeline band, then soft surface.
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to topMint,
                                0.12f to aqua,
                                0.28f to topLav,
                                0.48f to surface,
                                1.0f to surface,
                            ),
                        ),
                    )
                    // Soft organic orbs (lavender left, mint/aqua right) behind header + chips.
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(blobLav, Color.Transparent),
                            center = Offset(w * -0.02f, h * 0.06f),
                            radius = w * 0.72f,
                        ),
                        radius = w * 0.72f,
                        center = Offset(w * -0.02f, h * 0.06f),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(blobAqua, Color.Transparent),
                            center = Offset(w * 0.92f, h * 0.02f),
                            radius = w * 0.58f,
                        ),
                        radius = w * 0.58f,
                        center = Offset(w * 0.92f, h * 0.02f),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(blobMint, Color.Transparent),
                            center = Offset(w * 0.55f, h * 0.14f),
                            radius = w * 0.48f,
                        ),
                        radius = w * 0.48f,
                        center = Offset(w * 0.55f, h * 0.14f),
                    )
                    // Gentle diagonal veil so mint↔lavender blends more gracefully.
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                topLav.copy(alpha = if (dark) 0.18f else 0.28f),
                                Color.Transparent,
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h * 0.36f),
                        ),
                    )
                },
        )
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

@Composable
fun VeraCard(
    modifier: Modifier = Modifier,
    tonal: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = CardShape,
        color = tonal,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

/** Line icon in a soft circular wash — matches mock “app icon style” badges. */
@Composable
fun SoftIconCircle(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    wash: Color = mintWash(),
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(wash),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.48f),
        )
    }
}

@Composable
fun SoftRoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
}

/**
 * Compact appointment reminder for the current care-plan step.
 * One line + link — never a large block that pushes clinical text away.
 */
@Composable
fun CompactAppointmentHint(
    whenLabel: String,
    title: String?,
    onOpenAppointments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = buildString {
        append(UiCopy.APPOINTMENTS_NEXT_LABEL)
        append(": ")
        append(whenLabel)
        if (!title.isNullOrBlank()) {
            append(" · ")
            append(title)
        }
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = mintWash(),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenAppointments)
            .semantics {
                role = Role.Button
                contentDescription = label
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    UiCopy.NAV_APPOINTMENTS,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    UiCopy.APPOINTMENTS_VIEW_ALL,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
            }
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun SectionAccentLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun SoftCalendarBadge(modifier: Modifier = Modifier) {
    SoftIconCircle(
        icon = Icons.Outlined.CalendarMonth,
        modifier = modifier,
        wash = mintWash(),
        tint = MaterialTheme.colorScheme.primary,
    )
}
