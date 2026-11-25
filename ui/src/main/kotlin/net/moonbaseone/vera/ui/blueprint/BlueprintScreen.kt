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
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.blueprint.BlueprintUiState
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.domain.JargonEntry
import net.moonbaseone.vera.shared.domain.TimelineEvent
import net.moonbaseone.vera.shared.domain.TimelinePhase
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay
import net.moonbaseone.vera.ui.common.AddToPocketGuideButton
import net.moonbaseone.vera.ui.common.GlossaryLinkedText
import net.moonbaseone.vera.ui.theme.statusColor
import net.moonbaseone.vera.ui.util.openExternalUrl

private val TimelineNodeWidth = 108.dp
private val TimelineConnectorWidth = 16.dp

/**
 * Debug-only demo dataset picker. Shown from the shell when the care-plan title is
 * long-pressed and `isDebug` is true (never reachable in release builds).
 */
@Composable
fun DebugDatasetPickerDialog(
    viewModel: BlueprintViewModel,
    onDismiss: () -> Unit,
) {
    val selectedDataset by viewModel.selectedDatasetId.collectAsState()
    val selectedInfo = remember(selectedDataset) {
        viewModel.availableDatasets.first { it.id == selectedDataset }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiCopy.BLUEPRINT_DATASET_LABEL) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    selectedInfo.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                viewModel.availableDatasets.forEach { dataset ->
                    FilterChip(
                        selected = dataset.id == selectedDataset,
                        onClick = {
                            viewModel.selectDataset(dataset.id)
                            onDismiss()
                        },
                        label = {
                            Text(dataset.title, style = MaterialTheme.typography.labelMedium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(UiCopy.OK)
            }
        },
    )
}

@Composable
fun BlueprintScreen(viewModel: BlueprintViewModel) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val selectedDataset by viewModel.selectedDatasetId.collectAsState()
    val selectedInfo = remember(selectedDataset) {
        viewModel.availableDatasets.first { it.id == selectedDataset }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                UiCopy.BLUEPRINT_SOURCE_NOTE,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = false,
                onClick = { context.openExternalUrl(selectedInfo.guidelineUrl) },
                leadingIcon = {
                    Icon(Icons.Outlined.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = { Text(UiCopy.BLUEPRINT_IKNL_SOURCE, style = MaterialTheme.typography.labelSmall) },
            )
        }

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
                        val jargon = remember(expanded.id) { viewModel.jargonMatches(expanded) }
                        EventDetailPanel(
                            event = expanded,
                            jargon = jargon,
                            expandedJargonTerm = ui.expandedJargonTerm,
                            onToggleJargon = viewModel::toggleJargon,
                            showClinicalText = ui.showClinicalText,
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
            .height(72.dp)
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
    jargon: List<JargonEntry>,
    expandedJargonTerm: String?,
    onToggleJargon: (String) -> Unit,
    showClinicalText: Boolean,
    onShowClinicalText: (Boolean) -> Unit,
) {
    val context = LocalContext.current
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
            } else {
                // Lead with the patient-friendly step explanation as body copy
                // (no "Wat u moet weten" heading — that felt like a second todo list).
                val body = event.patientInstruction?.takeIf { it.isNotBlank() }
                    ?: event.description.takeIf { it.isNotBlank() }
                body?.let {
                    GlossaryLinkedText(text = it, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
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
                            FilterChip(
                                selected = expandedJargonTerm == entry.term,
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
                    val openJargon = remember(expandedJargonTerm, jargon) {
                        jargon.firstOrNull { it.term == expandedJargonTerm }
                    }
                    // Uitleg only — jargon actiepunten are listed under Actiepunten below.
                    if (openJargon != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    openJargon.term,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(openJargon.dutchExplanation, style = MaterialTheme.typography.bodySmall)
                                openJargon.iknlRef?.let { url ->
                                    TextButton(
                                        onClick = { context.openExternalUrl(url) },
                                        modifier = Modifier.padding(0.dp),
                                    ) {
                                        Text(UiCopy.DECODER_IKNL_BUTTON)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val openJargonForActions = remember(expandedJargonTerm, jargon) {
                    jargon.firstOrNull { it.term == expandedJargonTerm }
                }
                val actionRows = remember(
                    event.id,
                    event.patientInstruction,
                    event.actionItems,
                    openJargonForActions?.term,
                    openJargonForActions?.actionItem,
                ) {
                    buildList {
                        val jargonAction = openJargonForActions?.actionItem?.takeIf { it.isNotBlank() }
                        if (jargonAction != null && openJargonForActions != null) {
                            add("jargon:${openJargonForActions.term}" to jargonAction)
                        }
                        distinctActionItems(event.patientInstruction, event.actionItems).forEach { item ->
                            add("careplan:${event.id}" to item)
                        }
                    }.distinctBy { it.second }
                }
                if (actionRows.isNotEmpty()) {
                    Text(UiCopy.BLUEPRINT_ACTION_POINTS, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    actionRows.forEach { (source, item) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item, style = MaterialTheme.typography.bodySmall)
                                AddToPocketGuideButton(
                                    actionSource = source,
                                    actionText = item,
                                )
                            }
                        }
                    }
                }
            }

            event.iknlGuidelineRef?.let { url ->
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { context.openExternalUrl(url) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        UiCopy.BLUEPRINT_IKNL_LINK,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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

