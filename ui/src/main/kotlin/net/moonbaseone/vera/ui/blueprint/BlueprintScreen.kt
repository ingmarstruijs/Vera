package net.moonbaseone.vera.ui.blueprint

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.distinctUntilChanged
import net.moonbaseone.vera.shared.blueprint.BlueprintUiState
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.domain.TimelineEvent
import net.moonbaseone.vera.shared.domain.TimelinePhase
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay
import net.moonbaseone.vera.ui.common.AddToPocketGuideButton
import net.moonbaseone.vera.ui.common.GlossaryLinkedText
import net.moonbaseone.vera.ui.theme.statusColor
import net.moonbaseone.vera.ui.util.openExternalUrl

private val TimelineNodeWidth = 120.dp
private val TimelineConnectorWidth = 12.dp

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
    val state by viewModel.state.collectAsState()

    when (val ui = state) {
        BlueprintUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                CarePlanCarousel(
                    events = ui.events,
                    selectedId = ui.selectedId,
                    showClinicalText = ui.showClinicalText,
                    onSelectEvent = viewModel::selectEvent,
                    onShowClinicalText = viewModel::setShowClinicalText,
                )
            }
        }
    }
}

@Composable
private fun CarePlanCarousel(
    events: List<TimelineEvent>,
    selectedId: String?,
    showClinicalText: Boolean,
    onSelectEvent: (String) -> Unit,
    onShowClinicalText: (Boolean) -> Unit,
) {
    val selectedIndex = remember(selectedId, events) {
        events.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { events.size },
    )

    LaunchedEffect(selectedIndex) {
        if (pagerState.currentPage != selectedIndex) {
            pagerState.animateScrollToPage(selectedIndex)
        }
    }
    LaunchedEffect(pagerState, events) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                events.getOrNull(page)?.let { onSelectEvent(it.id) }
            }
    }

    // Fixed chrome (timeline) + pager; only the detail card scrolls vertically.
    Column(modifier = Modifier.fillMaxSize()) {
        TimelineStrip(
            events = events,
            selectedId = selectedId,
            onSelect = onSelectEvent,
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            beyondViewportPageCount = 1,
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 8.dp,
            userScrollEnabled = true,
        ) { page ->
            EventDetailPanel(
                event = events[page],
                showClinicalText = showClinicalText,
                onShowClinicalText = onShowClinicalText,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(bottom = 8.dp),
            )
        }
    }
}

@Composable
private fun TimelineStrip(
    events: List<TimelineEvent>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val focusId = selectedId
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
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            events.forEachIndexed { index, event ->
                TimelineNode(
                    event = event,
                    selected = selectedId == event.id,
                    onClick = { onSelect(event.id) },
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
    selected: Boolean,
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
        color = if (selected) phaseColor else phaseColor.copy(alpha = 0.12f),
        border = BorderStroke(if (selected) 2.dp else 1.5.dp, phaseColor),
        modifier = Modifier
            .width(TimelineNodeWidth)
            .height(68.dp)
            .semantics {
                role = Role.Button
                contentDescription = "${event.title}. Status: $phaseLabel."
            },
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                phaseLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else phaseColor,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                event.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                // Underlined glossary terms open a single explain sheet; no chip strip.
                val body = event.patientInstruction?.takeIf { it.isNotBlank() }
                    ?: event.description.takeIf { it.isNotBlank() }
                body?.let {
                    GlossaryLinkedText(text = it, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val actionRows = remember(event.id, event.patientInstruction, event.actionItems) {
                    distinctActionItems(event.patientInstruction, event.actionItems)
                        .map { item -> "careplan:${event.id}" to item }
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
