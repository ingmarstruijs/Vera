package net.moonbaseone.vera.ui.blueprint

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay
import net.moonbaseone.vera.ui.common.AddToPocketGuideButton
import net.moonbaseone.vera.ui.common.CompactAppointmentHint
import net.moonbaseone.vera.ui.common.GlossaryLinkedText
import net.moonbaseone.vera.ui.common.ModeSegmentedToggle
import net.moonbaseone.vera.ui.common.SoftIconCircle
import net.moonbaseone.vera.ui.theme.lavenderWash
import net.moonbaseone.vera.ui.theme.mintWash
import net.moonbaseone.vera.ui.theme.statusColor
import net.moonbaseone.vera.ui.util.openExternalUrl

private val TimelineChipHeight = 52.dp
private val TimelineGap = 8.dp
private val SoftCardShape = RoundedCornerShape(DesignTokens.CARD_RADIUS_DP.dp)

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
fun BlueprintScreen(
    viewModel: BlueprintViewModel,
    nextAppointmentWhen: String? = null,
    nextAppointmentTitle: String? = null,
    onOpenAppointments: () -> Unit = {},
) {
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
                    nextAppointmentWhen = nextAppointmentWhen,
                    nextAppointmentTitle = nextAppointmentTitle,
                    onOpenAppointments = onOpenAppointments,
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
    nextAppointmentWhen: String?,
    nextAppointmentTitle: String?,
    onOpenAppointments: () -> Unit,
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

    // Segment strip + pager; only the detail card scrolls vertically.
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
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 10.dp,
            userScrollEnabled = true,
        ) { page ->
            EventDetailPanel(
                event = events[page],
                showClinicalText = showClinicalText,
                onShowClinicalText = onShowClinicalText,
                nextAppointmentWhen = nextAppointmentWhen,
                nextAppointmentTitle = nextAppointmentTitle,
                onOpenAppointments = onOpenAppointments,
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

    // Approximate chip width for scroll-centering (icon + label + date + padding).
    val chipWidthApprox = 148.dp

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val viewportPx = with(density) { maxWidth.toPx() }
        val nodePx = with(density) { chipWidthApprox.toPx() }
        val stridePx = with(density) { (chipWidthApprox + TimelineGap).toPx() }

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
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TimelineGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            events.forEach { event ->
                TimelinePhaseChip(
                    event = event,
                    selected = selectedId == event.id,
                    onClick = { onSelect(event.id) },
                )
            }
        }
    }
}

/** Phase chip — status icon + short care-step name + compact date; selection is a border. */
@Composable
private fun TimelinePhaseChip(
    event: TimelineEvent,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val chipLabel = event.chipLabel()
    val dateLabel = event.dateIso.takeIf { it.isNotBlank() }?.let { DateDisplay.formatIso(it) }.orEmpty()
    val statusLabel = phaseStatusLabel(event.phase)
    val phaseAccent = statusColor(event.fhirStatus ?: phaseStatus(event.phase))
    val bg = when (event.phase) {
        TimelinePhase.Completed -> lavenderWash()
        TimelinePhase.Current -> mintWash()
        TimelinePhase.Future -> MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    }
    val fg = when (event.phase) {
        TimelinePhase.Future -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> phaseAccent
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) phaseAccent else phaseAccent.copy(alpha = 0.35f),
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(TimelineChipHeight)
            .semantics {
                role = Role.Button
                contentDescription = buildString {
                    append(chipLabel)
                    append(": ")
                    append(statusLabel)
                    if (dateLabel.isNotEmpty()) {
                        append(", ")
                        append(dateLabel)
                    }
                }
            },
    ) {
        Row(
            // Capsule ends need extra inset so the icon isn't tight against the curve.
            modifier = Modifier.padding(start = 18.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = phaseStatusIcon(event.phase),
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(18.dp),
            )
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    chipLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (dateLabel.isNotEmpty()) {
                    Text(
                        dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = fg.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun phaseStatusIcon(phase: TimelinePhase): ImageVector = when (phase) {
    TimelinePhase.Completed -> Icons.Outlined.CheckCircle
    TimelinePhase.Current -> Icons.Outlined.RadioButtonChecked
    TimelinePhase.Future -> Icons.Outlined.Schedule
}

private fun phaseStatus(phase: TimelinePhase): String = when (phase) {
    TimelinePhase.Completed -> "completed"
    TimelinePhase.Current -> "in-progress"
    TimelinePhase.Future -> "scheduled"
}

private fun phaseStatusLabel(phase: TimelinePhase): String = when (phase) {
    TimelinePhase.Completed -> UiCopy.BLUEPRINT_PHASE_COMPLETED
    TimelinePhase.Current -> UiCopy.BLUEPRINT_PHASE_CURRENT
    TimelinePhase.Future -> UiCopy.BLUEPRINT_PHASE_FUTURE
}

@Composable
private fun PhaseStatusPill(
    phase: TimelinePhase,
    fhirStatus: String?,
) {
    val accent = statusColor(fhirStatus ?: phaseStatus(phase))
    val bg = when (phase) {
        TimelinePhase.Completed -> lavenderWash()
        TimelinePhase.Current -> mintWash()
        TimelinePhase.Future -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Text(
            phaseStatusLabel(phase),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = accent,
        )
    }
}

@Composable
private fun EventDetailPanel(
    event: TimelineEvent,
    showClinicalText: Boolean,
    onShowClinicalText: (Boolean) -> Unit,
    nextAppointmentWhen: String?,
    nextAppointmentTitle: String?,
    onOpenAppointments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Surface(
        shape = SoftCardShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        tonalElevation = 0.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            // Mock layout: title left, soft icon right.
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PhaseStatusPill(phase = event.phase, fhirStatus = event.fhirStatus)
                    if (event.dateIso.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${UiCopy.BLUEPRINT_SCHEDULED_PREFIX}${DateDisplay.formatIso(event.dateIso)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                SoftIconCircle(
                    icon = Icons.Outlined.LocalHospital,
                    size = 48.dp,
                    wash = mintWash(),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            ModeSegmentedToggle(
                leftLabel = UiCopy.BLUEPRINT_MODE_PLAIN,
                rightLabel = UiCopy.BLUEPRINT_MODE_CLINICAL,
                rightSelected = showClinicalText,
                onLeft = { onShowClinicalText(false) },
                onRight = { onShowClinicalText(true) },
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (showClinicalText) {
                Text(
                    event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                val body = event.patientInstruction?.takeIf { it.isNotBlank() }
                    ?: event.description.takeIf { it.isNotBlank() }
                body?.let {
                    GlossaryLinkedText(text = it, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (event.phase == TimelinePhase.Current && !nextAppointmentWhen.isNullOrBlank()) {
                    CompactAppointmentHint(
                        whenLabel = nextAppointmentWhen,
                        title = nextAppointmentTitle,
                        onOpenAppointments = onOpenAppointments,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                val actionRows = remember(event.id, event.patientInstruction, event.actionItems) {
                    distinctActionItems(event.patientInstruction, event.actionItems)
                        .map { item -> "careplan:${event.id}" to item }
                }
                if (actionRows.isNotEmpty()) {
                    Text(UiCopy.BLUEPRINT_ACTION_POINTS, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            event.iknlGuidelineRef?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
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
