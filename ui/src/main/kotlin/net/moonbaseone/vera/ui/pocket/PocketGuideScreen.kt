package net.moonbaseone.vera.ui.pocket

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import net.moonbaseone.vera.shared.pocket.PocketGuideViewModel
import net.moonbaseone.vera.shared.symptoms.SymptomEntry
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.appointments.formatAppointmentWhen
import net.moonbaseone.vera.ui.common.SymptomDetailDialog
import net.moonbaseone.vera.ui.common.SymptomPreviewText
import kotlin.math.roundToLong

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
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var draftQuestion by remember { mutableStateOf("") }
    var symptomDetail by remember { mutableStateOf<SymptomEntry?>(null) }
    var symptomsExpanded by remember { mutableStateOf(true) }
    var myQuestionsExpanded by remember { mutableStateOf(true) }
    var suggestedExpanded by remember { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.startRecording()
    }

    fun requestOrToggleRecording() {
        if (state.isRecording) {
            viewModel.stopAndSaveRecording()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startRecording()
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(UiCopy.POCKET_DELETE_RECORDING) },
            text = { Text(UiCopy.POCKET_DELETE_RECORDING_CONFIRM) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteRecording()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(DesignTokens.ERROR_RED),
                    ),
                ) {
                    Text(UiCopy.POCKET_DELETE_RECORDING_CONFIRM_ACTION)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(UiCopy.APPOINTMENTS_CLOSE_FULL)
                }
            },
        )
    }

    symptomDetail?.let { entry ->
        SymptomDetailDialog(
            text = entry.text,
            createdAtMillis = entry.createdAtMillis,
            onDismiss = { symptomDetail = null },
        )
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
                    Text(
                        appt.practitioner,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                SectionTitle(UiCopy.POCKET_PREP_TITLE)
                Text(UiCopy.APPOINTMENTS_REASON, style = MaterialTheme.typography.labelMedium)
                Text(appt.reason.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
                if (appt.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(UiCopy.APPOINTMENTS_MEMO, style = MaterialTheme.typography.labelMedium)
                    Text(
                        UiCopy.POCKET_HOSPITAL_MEMO_HINT,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(appt.comment, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            CollapsibleSection(
                title = UiCopy.APPOINTMENTS_SYMPTOMS_HEADER,
                expanded = symptomsExpanded,
                onToggle = { symptomsExpanded = !symptomsExpanded },
            ) {
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
                            SymptomPreviewText(
                                text = entry.text,
                                createdAtMillis = entry.createdAtMillis,
                                onOpen = { symptomDetail = entry },
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { viewModel.speakSymptom(entry.id) }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = UiCopy.SYMPTOMS_SPEAK,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            CollapsibleSection(
                title = UiCopy.POCKET_MY_QUESTIONS,
                expanded = myQuestionsExpanded,
                onToggle = { myQuestionsExpanded = !myQuestionsExpanded },
            ) {
                val linkedIds = state.guide?.linkedActionItemIds.orEmpty()
                val linkedTexts = state.guide?.linkedActionTexts.orEmpty()
                if (linkedTexts.isEmpty()) {
                    Text(
                        UiCopy.POCKET_NO_QUESTIONS_HINT,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    linkedTexts.forEachIndexed { index, text ->
                        val actionId = linkedIds.getOrNull(index)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "• $text",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (actionId != null) {
                                IconButton(
                                    onClick = { viewModel.removeQuestion(actionId) },
                                    modifier = Modifier.semantics {
                                        contentDescription = UiCopy.APPOINTMENTS_DELETE_NOTE
                                    },
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = UiCopy.APPOINTMENTS_DELETE_NOTE,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(UiCopy.POCKET_RECORD_TITLE)
            if (state.isRecording) {
                Text(
                    UiCopy.APPOINTMENTS_VOICE_LISTENING,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(DesignTokens.ERROR_RED),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = { requestOrToggleRecording() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = if (state.isRecording) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(DesignTokens.ERROR_RED),
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                Text(
                    if (state.isRecording) {
                        UiCopy.POCKET_RECORD_STOP
                    } else {
                        UiCopy.POCKET_RECORD_START
                    },
                )
            }
            if (!state.guide?.recordingPath.isNullOrBlank() && !state.isRecording) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    UiCopy.POCKET_PLAY_RECORDING,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                RecordingPlaybackControls(
                    positionMs = state.playbackPositionMs,
                    durationMs = state.playbackDurationMs,
                    isPlaying = state.isPlaying,
                    onSeek = viewModel::seekTo,
                    onTogglePlay = viewModel::togglePlayback,
                    onStop = viewModel::stopPlayback,
                    onSeekBack = { viewModel.seekBy(-10_000L) },
                    onSeekForward = { viewModel.seekBy(10_000L) },
                    onDelete = { showDeleteConfirm = true },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            CollapsibleSection(
                title = UiCopy.APPOINTMENTS_SUGGESTED_QUESTIONS,
                expanded = suggestedExpanded,
                onToggle = { suggestedExpanded = !suggestedExpanded },
            ) {
                state.suggestedQuestions.forEach { suggestion ->
                    TextButton(
                        onClick = { viewModel.addSuggestedQuestion(suggestion) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "+ $suggestion",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(UiCopy.POCKET_ADD_QUESTION)
            OutlinedTextField(
                value = draftQuestion,
                onValueChange = { draftQuestion = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            TextButton(
                onClick = {
                    val text = draftQuestion
                    if (text.isNotBlank()) {
                        viewModel.addManualQuestion(text)
                        draftQuestion = ""
                    }
                },
                enabled = draftQuestion.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "+ ${UiCopy.POCKET_ADD_QUESTION}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            state.errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(msg, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RecordingPlaybackControls(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onDelete: () -> Unit,
) {
    val safeDuration = durationMs.coerceAtLeast(0L)
    val progress = if (safeDuration > 0L) {
        (positionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { fraction ->
                if (safeDuration > 0L) {
                    onSeek((fraction * safeDuration).roundToLong())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = UiCopy.POCKET_PLAY_RECORDING },
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                formatPlaybackTime(positionMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                formatPlaybackTime(safeDuration),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onSeekBack,
                modifier = Modifier.semantics { contentDescription = UiCopy.POCKET_SEEK_BACK },
            ) {
                Icon(Icons.Outlined.Replay10, contentDescription = UiCopy.POCKET_SEEK_BACK)
            }
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier.semantics {
                    contentDescription = if (isPlaying) {
                        UiCopy.POCKET_RECORD_STOP
                    } else {
                        UiCopy.POCKET_PLAY_RECORDING
                    }
                },
            ) {
                Icon(
                    if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
            IconButton(
                onClick = onStop,
                modifier = Modifier.semantics { contentDescription = UiCopy.APPOINTMENTS_VOICE_STOP },
            ) {
                Icon(Icons.Outlined.Stop, contentDescription = UiCopy.APPOINTMENTS_VOICE_STOP)
            }
            IconButton(
                onClick = onSeekForward,
                modifier = Modifier.semantics { contentDescription = UiCopy.POCKET_SEEK_FORWARD },
            ) {
                Icon(Icons.Outlined.Forward10, contentDescription = UiCopy.POCKET_SEEK_FORWARD)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.semantics {
                    contentDescription = UiCopy.POCKET_DELETE_RECORDING
                },
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = UiCopy.POCKET_DELETE_RECORDING,
                    tint = Color(DesignTokens.ERROR_RED),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 4.dp)
                .semantics { contentDescription = title },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

private fun formatPlaybackTime(ms: Long): String {
    val totalSec = (ms.coerceAtLeast(0L) / 1000L).toInt()
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return "%d:%02d".format(minutes, seconds)
}
