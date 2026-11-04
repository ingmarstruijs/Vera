package net.moonbaseone.vera.ui.appointments

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import net.moonbaseone.vera.shared.appointments.AppointmentSummary
import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy

@Composable
fun AppointmentsScreen(viewModel: AppointmentsViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.toggleVoiceLogging()
    }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

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
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(UiCopy.APPOINTMENTS_TITLE, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                UiCopy.APPOINTMENTS_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.appointments.isEmpty()) {
            item {
                Text(UiCopy.APPOINTMENTS_NO_DATA, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            items(state.appointments, key = { it.id }) { appt ->
                AppointmentCard(
                    appointment = appt,
                    selected = state.selectedId == appt.id,
                    onSelect = { viewModel.select(appt.id) },
                    onGenerateGuide = { viewModel.generatePocketGuide(appt.id) },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(UiCopy.APPOINTMENTS_SYMPTOMS_SECTION, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (state.speechAvailable) {
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            viewModel.toggleVoiceLogging()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = if (state.isListening) {
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(DesignTokens.ERROR_RED),
                        )
                    } else {
                        androidx.compose.material3.ButtonDefaults.buttonColors()
                    },
                ) {
                    Icon(
                        if (state.isListening) Icons.Outlined.Stop else Icons.Outlined.MicNone,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (state.isListening) {
                            UiCopy.APPOINTMENTS_VOICE_STOP
                        } else {
                            UiCopy.APPOINTMENTS_VOICE_LOG
                        },
                    )
                }
                if (state.isListening) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        UiCopy.APPOINTMENTS_VOICE_LISTENING,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(DesignTokens.ERROR_RED),
                    )
                }
            } else {
                Text(
                    UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (state.symptomNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                UiCopy.APPOINTMENTS_NOTES_LABEL,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = viewModel::clearSymptomNotes) {
                                Text(UiCopy.APPOINTMENTS_CLEAR)
                            }
                        }
                        Text(state.symptomNotes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            state.errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(msg, color = Color(DesignTokens.ERROR_RED), style = MaterialTheme.typography.bodySmall)
            }
        }

        state.pocketGuide?.let { guide ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        UiCopy.APPOINTMENTS_POCKET_PANEL,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = viewModel::readAloudPocketGuide,
                        modifier = Modifier.semantics {
                            contentDescription = UiCopy.APPOINTMENTS_READ_ALOUD
                        },
                    ) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = UiCopy.APPOINTMENTS_READ_ALOUD)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        guide,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentSummary,
    selected: Boolean,
    onSelect: () -> Unit,
    onGenerateGuide: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onSelect),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(appointment.serviceType, style = MaterialTheme.typography.titleSmall)
            }
            if (appointment.startIso.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    appointment.startIso,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (appointment.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    appointment.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
            if (appointment.practitioner.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    appointment.practitioner,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                )
            }
            if (selected) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGenerateGuide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(UiCopy.APPOINTMENTS_POCKET_GUIDE)
                }
            }
        }
    }
}
