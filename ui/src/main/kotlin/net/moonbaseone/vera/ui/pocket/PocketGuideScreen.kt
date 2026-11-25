package net.moonbaseone.vera.ui.pocket

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.domain.IknlGuidelines
import net.moonbaseone.vera.shared.pocket.PocketGuideViewModel
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.appointments.formatAppointmentWhen
import net.moonbaseone.vera.ui.util.openExternalUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketGuideScreen(
    appointmentId: String,
    viewModel: PocketGuideViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(appointmentId) {
        viewModel.load(appointmentId)
    }

    DisposableEffect(Unit) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewModel.dispose()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.appointment?.serviceType ?: UiCopy.APPOINTMENTS_POCKET_PANEL,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::readAloud,
                        modifier = Modifier.semantics {
                            contentDescription = UiCopy.APPOINTMENTS_READ_ALOUD
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = UiCopy.APPOINTMENTS_READ_ALOUD)
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val appt = state.appointment
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            if (appt != null) {
                Text(
                    formatAppointmentWhen(appt.startIso),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (appt.location.isNotBlank()) {
                    Text(appt.location, style = MaterialTheme.typography.bodyMedium)
                }
                if (appt.practitioner.isNotBlank()) {
                    Text(appt.practitioner, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(20.dp))

                SectionTitle(UiCopy.POCKET_PREP_TITLE)
                Text(UiCopy.APPOINTMENTS_REASON, style = MaterialTheme.typography.labelMedium)
                Text(appt.reason.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
                if (appt.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Memo", style = MaterialTheme.typography.labelMedium)
                    Text(appt.comment, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(UiCopy.APPOINTMENTS_SYMPTOMS_HEADER)
            if (state.recentSymptoms.isEmpty()) {
                Text(UiCopy.POCKET_NO_SYMPTOMS_HINT, style = MaterialTheme.typography.bodyMedium)
            } else {
                state.recentSymptoms.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(entry.text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { viewModel.speakSymptom(entry.id) }) {
                            Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = UiCopy.SYMPTOMS_SPEAK)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(UiCopy.POCKET_MY_QUESTIONS)
            val linked = state.guide?.linkedActionTexts.orEmpty()
            if (linked.isEmpty()) {
                Text(UiCopy.POCKET_NO_QUESTIONS_HINT, style = MaterialTheme.typography.bodyMedium)
            } else {
                linked.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(UiCopy.APPOINTMENTS_SUGGESTED_QUESTIONS)
            state.suggestedQuestions.forEach {
                Text("• $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(UiCopy.POCKET_RECORD_TITLE)
            Button(
                onClick = viewModel::toggleRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(
                    if (state.isRecording) Icons.Outlined.Stop else Icons.Outlined.MicNone,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (state.isRecording) UiCopy.POCKET_RECORD_STOP else UiCopy.POCKET_RECORD_START,
                )
            }
            if (!state.guide?.recordingPath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = viewModel::playRecording,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(UiCopy.POCKET_PLAY_RECORDING)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = { context.openExternalUrl(IknlGuidelines.BREAST) }) {
                Text(UiCopy.BLUEPRINT_IKNL_LINK)
            }
            Text(
                UiCopy.APPOINTMENTS_POCKET_FOOTER,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
}
