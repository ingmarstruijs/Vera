package net.moonbaseone.vera.ui.appointments

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay

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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        val next = state.next
        if (next == null) {
            item {
                Text(UiCopy.APPOINTMENTS_NO_DATA, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            item {
                Text(UiCopy.APPOINTMENTS_NEXT_LABEL, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                NextAppointmentCard(
                    item = next,
                    onOpen = {
                        viewModel.ensureGuide(next.appointment.id)
                        onOpenPocketGuide(next.appointment.id)
                    },
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (state.upcoming.isNotEmpty()) {
                item {
                    Text(UiCopy.APPOINTMENTS_SOON_LABEL, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(state.upcoming, key = { it.appointment.id }) { item ->
                    CompactAppointmentRow(
                        item = item,
                        onOpen = {
                            viewModel.ensureGuide(item.appointment.id)
                            onOpenPocketGuide(item.appointment.id)
                        },
                    )
                }
            }

            if (state.past.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = viewModel::togglePastExpanded) {
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
                            onOpen = {
                                viewModel.ensureGuide(item.appointment.id)
                                onOpenPocketGuide(item.appointment.id)
                            },
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { msg ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(msg, color = MaterialTheme.colorScheme.error)
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun NextAppointmentCard(
    item: AppointmentListItem,
    onOpen: () -> Unit,
) {
    val appt = item.appointment
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                formatAppointmentWhen(appt.startIso),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(appt.serviceType, style = MaterialTheme.typography.titleMedium)
            if (appt.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(appt.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (appt.practitioner.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(appt.practitioner, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(10.dp))
            GuideStatusChip(item.guideStatus)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        contentDescription = ctaLabel(item.guideStatus)
                    },
            ) {
                Text(ctaLabel(item.guideStatus))
            }
        }
    }
}

@Composable
private fun CompactAppointmentRow(
    item: AppointmentListItem,
    onOpen: () -> Unit,
) {
    val appt = item.appointment
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatAppointmentWhen(appt.startIso), style = MaterialTheme.typography.labelLarge)
                Text(appt.serviceType, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                GuideStatusChip(item.guideStatus)
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun GuideStatusChip(status: PocketGuideStatus) {
    val label = when (status) {
        PocketGuideStatus.None -> UiCopy.APPOINTMENTS_GUIDE_NONE
        PocketGuideStatus.Ready -> UiCopy.APPOINTMENTS_GUIDE_READY
        PocketGuideStatus.RecordingPresent -> UiCopy.APPOINTMENTS_GUIDE_RECORDING
    }
    FilterChip(
        selected = status != PocketGuideStatus.None,
        onClick = {},
        enabled = false,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
    )
}

private fun ctaLabel(status: PocketGuideStatus): String =
    if (status == PocketGuideStatus.None) {
        UiCopy.APPOINTMENTS_CREATE_GUIDE
    } else {
        UiCopy.APPOINTMENTS_OPEN_GUIDE
    }

/** Dutch-friendly date/time for an appointment ISO timestamp. */
internal fun formatAppointmentWhen(startIso: String): String =
    if (startIso.isBlank()) "—" else DateDisplay.formatIso(startIso)
