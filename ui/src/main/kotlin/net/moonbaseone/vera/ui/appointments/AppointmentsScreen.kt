package net.moonbaseone.vera.ui.appointments

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.appointments.AppointmentListItem
import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.appointments.PocketGuideStatus
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay
import net.moonbaseone.vera.ui.common.SectionAccentLabel
import net.moonbaseone.vera.ui.common.SoftIconCircle
import net.moonbaseone.vera.ui.theme.lavenderWash
import net.moonbaseone.vera.ui.theme.mintWash

@Composable
fun AppointmentsScreen(
    viewModel: AppointmentsViewModel,
    onOpenPocketGuide: (appointmentId: String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        val next = state.next
        if (next == null) {
            item {
                Text(
                    UiCopy.APPOINTMENTS_NO_DATA,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    NextAppointmentCard(
                        item = next,
                        isPlaying = state.playingAppointmentId == next.appointment.id,
                        onOpen = {
                            viewModel.ensureGuide(next.appointment.id)
                            onOpenPocketGuide(next.appointment.id)
                        },
                        onPlayRecording = { viewModel.playRecording(next.appointment.id) },
                        onStopPlayback = viewModel::stopPlayback,
                    )
                }
                Spacer(modifier = Modifier.height(22.dp))
            }

            if (state.upcoming.isNotEmpty()) {
                item {
                    SectionAccentLabel(
                        UiCopy.APPOINTMENTS_SOON_LABEL,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(state.upcoming, key = { it.appointment.id }) { item ->
                    CompactAppointmentRow(
                        item = item,
                        isPlaying = state.playingAppointmentId == item.appointment.id,
                        onOpen = {
                            viewModel.ensureGuide(item.appointment.id)
                            onOpenPocketGuide(item.appointment.id)
                        },
                        onPlayRecording = { viewModel.playRecording(item.appointment.id) },
                        onStopPlayback = viewModel::stopPlayback,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            if (state.past.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = viewModel::togglePastExpanded,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    ) {
                        Icon(
                            if (state.pastExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(UiCopy.APPOINTMENTS_PAST_LABEL)
                    }
                }
                if (state.pastExpanded) {
                    items(state.past, key = { "past-${it.appointment.id}" }) { item ->
                        CompactAppointmentRow(
                            item = item,
                            isPlaying = state.playingAppointmentId == item.appointment.id,
                            onOpen = {
                                viewModel.ensureGuide(item.appointment.id)
                                onOpenPocketGuide(item.appointment.id)
                            },
                            onPlayRecording = { viewModel.playRecording(item.appointment.id) },
                            onStopPlayback = viewModel::stopPlayback,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { msg ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun NextAppointmentCard(
    item: AppointmentListItem,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onPlayRecording: () -> Unit,
    onStopPlayback: () -> Unit,
) {
    val appt = item.appointment
    Surface(
        shape = RoundedCornerShape(DesignTokens.CARD_RADIUS_DP.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .semantics { contentDescription = appt.serviceType },
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoftIconCircle(
                    icon = Icons.Outlined.CalendarMonth,
                    size = 34.dp,
                    wash = mintWash(),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    formatAppointmentWhen(appt.startIso),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                if (item.guideStatus == PocketGuideStatus.RecordingPresent) {
                    IconButton(
                        onClick = if (isPlaying) onStopPlayback else onPlayRecording,
                        modifier = Modifier.semantics {
                            contentDescription = UiCopy.POCKET_PLAY_RECORDING
                        },
                    ) {
                        Icon(
                            if (isPlaying) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                            contentDescription = UiCopy.POCKET_PLAY_RECORDING,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                appt.serviceType,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (appt.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                MetaRow(Icons.Outlined.LocationOn, appt.location)
            }
            if (appt.practitioner.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                MetaRow(Icons.Outlined.Person, appt.practitioner)
            }
        }
    }
}

@Composable
private fun MetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CompactAppointmentRow(
    item: AppointmentListItem,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onPlayRecording: () -> Unit,
    onStopPlayback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appt = item.appointment
    Surface(
        shape = RoundedCornerShape(DesignTokens.CARD_RADIUS_DP.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onOpen)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatAppointmentWhen(appt.startIso),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    appt.serviceType,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (item.guideStatus == PocketGuideStatus.RecordingPresent) {
                IconButton(
                    onClick = if (isPlaying) onStopPlayback else onPlayRecording,
                    modifier = Modifier.semantics {
                        contentDescription = UiCopy.POCKET_PLAY_RECORDING
                    },
                ) {
                    Icon(
                        if (isPlaying) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                        contentDescription = UiCopy.POCKET_PLAY_RECORDING,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            SoftIconCircle(
                icon = Icons.Outlined.CalendarMonth,
                size = 44.dp,
                wash = lavenderWash(),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal fun formatAppointmentWhen(startIso: String): String =
    if (startIso.isBlank()) "—" else DateDisplay.formatIso(startIso)
