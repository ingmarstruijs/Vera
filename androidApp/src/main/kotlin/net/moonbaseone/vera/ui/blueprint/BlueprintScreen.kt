package net.moonbaseone.vera.ui.blueprint

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.theme.statusColor

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
            .padding(bottom = 24.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(UiCopy.BLUEPRINT_TITLE, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                UiCopy.BLUEPRINT_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterChip(
                selected = false,
                onClick = {},
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Outlined.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = { Text(UiCopy.BLUEPRINT_IKNL_SOURCE, style = MaterialTheme.typography.labelSmall) },
            )
        }

        LegendRow(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

        when (val ui = state) {
            BlueprintUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is BlueprintUiState.Error -> {
                Text(
                    ui.message,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            is BlueprintUiState.Ready -> {
                if (ui.events.isEmpty()) {
                    Text(
                        UiCopy.BLUEPRINT_NO_DATA,
                        modifier = Modifier.padding(32.dp),
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
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(Color(0xFF0D47A1), UiCopy.BLUEPRINT_PHASE_COMPLETED)
        LegendItem(Color(0xFF1B5E20), UiCopy.BLUEPRINT_PHASE_CURRENT)
        LegendItem(Color(0xFFE07B00), UiCopy.BLUEPRINT_PHASE_FUTURE)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TimelineStrip(
    events: List<TimelineEvent>,
    expandedId: String?,
    onToggle: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
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
                        .width(24.dp)
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
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
        shape = RoundedCornerShape(14.dp),
        color = if (expanded) phaseColor else phaseColor.copy(alpha = 0.12f),
        border = BorderStroke(if (expanded) 2.dp else 1.5.dp, phaseColor),
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .semantics {
                role = Role.Button
                contentDescription = "${event.title}. Status: $phaseLabel."
            },
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
            Spacer(modifier = Modifier.height(6.dp))
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
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                event.fhirStatus?.let { StatusBadge(it) }
            }
            if (event.dateIso.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${UiCopy.BLUEPRINT_SCHEDULED_PREFIX}${event.dateIso}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(event.description, style = MaterialTheme.typography.bodyMedium)

            if (jargon.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(UiCopy.BLUEPRINT_PLAIN_LANGUAGE, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                jargon.forEach { entry ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.term, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(entry.dutchExplanation, style = MaterialTheme.typography.bodySmall)
                            if (entry.actionItem.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Actie: ${entry.actionItem}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }

            if (event.iknlGuidelineRef != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { /* open URL in later phase */ }) {
                    Text(UiCopy.BLUEPRINT_IKNL_LINK)
                }
            }

            if (event.actionItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(UiCopy.BLUEPRINT_ACTION_POINTS, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                event.actionItems.forEach { item ->
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
