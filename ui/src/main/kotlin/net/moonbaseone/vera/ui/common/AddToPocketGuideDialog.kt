package net.moonbaseone.vera.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.appointments.AppointmentSummary
import net.moonbaseone.vera.shared.appointments.toSummary
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.pocket.PocketGuideStore
import net.moonbaseone.vera.shared.pocket.actionItemId
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.appointments.formatAppointmentWhen
import org.koin.compose.koinInject

@Composable
fun AddToPocketGuideButton(
    actionSource: String,
    actionText: String,
    modifier: Modifier = Modifier,
) {
    val pocketStore = koinInject<PocketGuideStore>()
    val carePlanRepository = koinInject<CarePlanRepository>()
    var showPicker by remember { mutableStateOf(false) }
    var appointments by remember { mutableStateOf<List<AppointmentSummary>>(emptyList()) }
    val actionId = remember(actionSource, actionText) { actionItemId(actionSource, actionText) }
    val guides by pocketStore.guides.collectAsState()

    val linkedAppointmentId = guides.values.firstOrNull { actionId in it.linkedActionItemIds }?.appointmentId
    val linkedLabel = linkedAppointmentId?.let { id ->
        appointments.firstOrNull { it.id == id }?.serviceType ?: id
    }

    LaunchedEffect(Unit) {
        appointments = carePlanRepository.fetchAppointments().map { it.toSummary() }
    }

    if (linkedLabel != null) {
        Text(
            "${UiCopy.IN_POCKET_GUIDE}: $linkedLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier.padding(top = 2.dp),
        )
    } else {
        Text(
            UiCopy.ADD_TO_POCKET_GUIDE,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier
                .semantics { role = Role.Button }
                .clickable { showPicker = true }
                .padding(top = 2.dp, bottom = 2.dp),
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(UiCopy.PICK_APPOINTMENT_FOR_GUIDE) },
            text = {
                LazyColumn {
                    items(appointments, key = { it.id }) { appt ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pocketStore.linkActionItem(appt.id, actionId, actionText)
                                    showPicker = false
                                }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(formatAppointmentWhen(appt.startIso), style = MaterialTheme.typography.labelLarge)
                            Text(appt.serviceType, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(UiCopy.OK)
                }
            },
        )
    }
}
