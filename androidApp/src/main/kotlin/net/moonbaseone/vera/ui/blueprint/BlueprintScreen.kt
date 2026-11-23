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
import net.moonbaseone.vera.shared.symptoms.SymptomLogViewModel
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay
import net.moonbaseone.vera.ui.symptoms.SymptomLogSection
import net.moonbaseone.vera.ui.theme.statusColor

private val TimelineNodeWidth = 140.dp
private val TimelineConnectorWidth = 24.dp

@Composable
fun BlueprintScreen(
    viewModel: BlueprintViewModel,
    symptomLogViewModel: SymptomLogViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        LegendRow(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
        )

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
                            showClinicalText = ui.showClinicalText,
                            onShowClinicalText = viewModel::setShowClinicalText,
                        )
                    }
                }
            }
        }

        SymptomLogSection(
            viewModel = symptomLogViewModel,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
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
        LegendItem(statusColor("completed"), UiCopy.BLUEPRINT_PHASE_COMPLETED)
        LegendItem(statusColor("in-progress"), UiCopy.BLUEPRINT_PHASE_CURRENT)
        LegendItem(statusColor("scheduled"), UiCopy.BLUEPRINT_PHASE_FUTURE)
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
    showClinicalText: Boolean,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                event.fhirStatus?.let { StatusBadge(it) }
            }
            if (event.dateIso.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${UiCopy.BLUEPRINT_SCHEDULED_PREFIX}${DateDisplay.formatIso(event.dateIso)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
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
                    label = {
                        Text(
                            UiCopy.BLUEPRINT_MODE_PLAIN,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = showClinicalText,
                    onClick = { onShowClinicalText(true) },
                    label = {
                        Text(
                            UiCopy.BLUEPRINT_MODE_CLINICAL,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.weight(1f),
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
                Text(
                    event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                event.iknlGuidelineRef?.let { url ->
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { uriHandler.openUri(url) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            UiCopy.BLUEPRINT_IKNL_LINK,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else {
                // Lead with the patient-friendly step explanation as body copy
                // (no "Wat u moet weten" heading — that felt like a second todo list).
                event.patientInstruction?.takeIf { it.isNotBlank() }?.let { instruction ->
                    Text(
                        instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val checklist = remember(event.patientInstruction, event.actionItems) {
                    distinctActionItems(event.patientInstruction, event.actionItems)
                }
                if (checklist.isNotEmpty()) {
                    Text(UiCopy.BLUEPRINT_ACTION_POINTS, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    checklist.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                item,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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

/** Drop checklist rows that merely restate the plain-language body. */
private fun distinctActionItems(
    patientInstruction: String?,
    actionItems: List<String>,
): List<String> {
    val body = patientInstruction.orEmpty().lowercase()
    if (body.isBlank()) return actionItems
    return actionItems.filter { item ->
        val normalized = item.trim().lowercase().trimEnd('.')
        normalized.isNotEmpty() && !body.contains(normalized)
    }
}
