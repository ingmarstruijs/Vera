package net.moonbaseone.vera.ui.blueprint

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.blueprint.BlueprintUiState
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.domain.TimelineEvent
import net.moonbaseone.vera.shared.domain.TimelinePhase
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay
import net.moonbaseone.vera.ui.theme.statusColor

private val TimelineNodeWidth = 140.dp
private val TimelineConnectorWidth = 24.dp

@Composable
fun BlueprintScreen(viewModel: BlueprintViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        LegendRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

        when (val ui = state) {
            BlueprintUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is BlueprintUiState.Error -> {
                Text(
                    ui.message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            is BlueprintUiState.Ready -> {
                if (ui.events.isEmpty()) {
                    Text(
                        UiCopy.BLUEPRINT_NO_DATA,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    TimelineStrip(
                        events = ui.events,
                        expandedId = ui.expandedId,
                        onToggle = viewModel::toggleExpanded,
                    )
                    val expanded = ui.events.firstOrNull { it.id == ui.expandedId }
                    if (expanded != null) {
                        EventDetailPanel(
                            event = expanded,
                            jargon = viewModel.jargonMatches(expanded),
                            expandedJargonTerm = ui.expandedJargonTerm,
                            showClinicalText = ui.showClinicalText,
                            onToggleJargon = viewModel::toggleJargon,
                            onShowClinicalText = viewModel::setShowClinicalText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics {
            contentDescription =
                "${UiCopy.BLUEPRINT_PHASE_COMPLETED}, ${UiCopy.BLUEPRINT_PHASE_CURRENT}, ${UiCopy.BLUEPRINT_PHASE_FUTURE}"
        },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LegendItem(Color(DesignTokens.STATUS_COMPLETED), UiCopy.BLUEPRINT_PHASE_COMPLETED)
        LegendItem(Color(DesignTokens.STATUS_IN_PROGRESS), UiCopy.BLUEPRINT_PHASE_CURRENT)
        LegendItem(Color(DesignTokens.STATUS_SCHEDULED), UiCopy.BLUEPRINT_PHASE_FUTURE)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TimelineStrip(
    events: List<TimelineEvent>,
    expandedId: String?,
    onToggle: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    // Prefer the expanded (active) item; fall back to first Current-phase node.
    val focusId = expandedId
        ?: events.firstOrNull { it.phase == TimelinePhase.Current }?.id

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val viewportPx = with(density) { maxWidth.toPx() }
        val nodePx = with(density) { TimelineNodeWidth.toPx() }
        val stridePx = with(density) { (TimelineNodeWidth + TimelineConnectorWidth).toPx() }

        LaunchedEffect(focusId, events, viewportPx) {
            val index = events.indexOfFirst { it.id == focusId }
            if (index < 0) return@LaunchedEffect
            val centered = (index * stridePx - (viewportPx - nodePx) / 2f)
                .toInt()
                .coerceAtLeast(0)
            scrollState.animateScrollTo(centered)
        }

        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            events.forEachIndexed { index, event ->
                TimelineNode(
                    event = event,
                    expanded = expandedId == event.id,
                    onClick = { onToggle(event.id) },
                )
                if (index < events.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(TimelineConnectorWidth)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineNode(
    event: TimelineEvent,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val phaseColor = statusColor(event.fhirStatus ?: phaseStatus(event.phase))
    val phaseLabel = when (event.phase) {
        TimelinePhase.Completed -> UiCopy.BLUEPRINT_PHASE_COMPLETED
        TimelinePhase.Current -> UiCopy.BLUEPRINT_PHASE_CURRENT
        TimelinePhase.Future -> UiCopy.BLUEPRINT_PHASE_FUTURE
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (expanded) phaseColor else phaseColor.copy(alpha = 0.12f),
        border = BorderStroke(if (expanded) 2.dp else 1.5.dp, phaseColor),
        modifier = Modifier
            .width(TimelineNodeWidth)
            .height(92.dp)
            .semantics {
                role = Role.Button
                contentDescription = "${event.title}. Status: $phaseLabel."
            },
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(phaseColor, CircleShape),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    phaseLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (expanded) Color.White else phaseColor,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                event.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (expanded) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EventDetailPanel(
    event: TimelineEvent,
    jargon: List<net.moonbaseone.vera.shared.domain.JargonEntry>,
    expandedJargonTerm: String?,
    showClinicalText: Boolean,
    onToggleJargon: (String) -> Unit,
    onShowClinicalText: (Boolean) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                event.fhirStatus?.let { StatusBadge(it) }
            }
            if (event.dateIso.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${UiCopy.BLUEPRINT_SCHEDULED_PREFIX}${DateDisplay.formatIso(event.dateIso)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilterChip(
                    selected = !showClinicalText,
                    onClick = { onShowClinicalText(false) },
                    label = { Text(UiCopy.BLUEPRINT_MODE_PLAIN) },
                )
                FilterChip(
                    selected = showClinicalText,
                    onClick = { onShowClinicalText(true) },
                    label = { Text(UiCopy.BLUEPRINT_MODE_CLINICAL) },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (showClinicalText) {
                Text(
                    UiCopy.BLUEPRINT_CLINICAL_LABEL,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(event.description, style = MaterialTheme.typography.bodyMedium)
                event.iknlGuidelineRef?.let { url ->
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { uriHandler.openUri(url) },
                        modifier = Modifier.padding(0.dp),
                    ) {
                        Text(UiCopy.BLUEPRINT_IKNL_LINK)
                    }
                }
            } else {
                event.patientInstruction?.let { instruction ->
                    Text(UiCopy.BLUEPRINT_INSTRUCTION, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(instruction, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                event.nextSteps?.let { next ->
                    Text(UiCopy.BLUEPRINT_NEXT_STEPS, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(next, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (event.actionItems.isNotEmpty()) {
                    Text(UiCopy.BLUEPRINT_ACTION_POINTS, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    event.actionItems.forEach { item ->
                        Row(
                            modifier = Modifier.padding(bottom = 3.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (jargon.isNotEmpty()) {
                    Text(UiCopy.BLUEPRINT_PLAIN_LANGUAGE, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        UiCopy.BLUEPRINT_TAP_TERM_HINT,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        jargon.forEach { entry ->
                            val selected = expandedJargonTerm == entry.term
                            FilterChip(
                                selected = selected,
                                onClick = { onToggleJargon(entry.term) },
                                label = {
                                    Text(
                                        entry.term,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                modifier = Modifier.semantics {
                                    role = Role.Button
                                    contentDescription = entry.term
                                },
                            )
                        }
                    }
                    val open = remember(expandedJargonTerm, jargon) {
                        jargon.firstOrNull { it.term == expandedJargonTerm }
                    }
                    if (open != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    open.term,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(open.dutchExplanation, style = MaterialTheme.typography.bodySmall)
                                if (open.actionItem.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "${UiCopy.DECODER_ACTION_LABEL}: ${open.actionItem}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = statusColor(status)
    val label = when (status) {
        "completed" -> "Voltooid"
        "in-progress", "active" -> "Actief"
        "scheduled" -> "Gepland"
        else -> status
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

private fun phaseStatus(phase: TimelinePhase): String = when (phase) {
    TimelinePhase.Completed -> "completed"
    TimelinePhase.Current -> "in-progress"
    TimelinePhase.Future -> "scheduled"
}
