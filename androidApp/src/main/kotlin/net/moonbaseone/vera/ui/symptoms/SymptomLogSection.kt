package net.moonbaseone.vera.ui.symptoms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import net.moonbaseone.vera.shared.symptoms.SymptomLogViewModel
import net.moonbaseone.vera.shared.symptoms.SymptomNote
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy

@Composable
fun SymptomLogSection(
    viewModel: SymptomLogViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var pendingReplaceId by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.toggleVoiceLogging(pendingReplaceId)
        pendingReplaceId = null
    }

    fun startVoice(replaceId: String? = null) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.toggleVoiceLogging(replaceId)
        } else {
            pendingReplaceId = replaceId
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(UiCopy.APPOINTMENTS_SYMPTOMS_SECTION, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (state.speechAvailable) {
            Button(
                onClick = { startVoice(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = if (state.isListening) {
                    ButtonDefaults.buttonColors(containerColor = Color(DesignTokens.ERROR_RED))
                } else {
                    ButtonDefaults.buttonColors()
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
                if (state.draftText.isNotBlank()) {
                    Text(
                        state.draftText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Text(
                UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        state.errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(msg, color = Color(DesignTokens.ERROR_RED), style = MaterialTheme.typography.bodySmall)
        }

        if (state.notes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    UiCopy.SYMPTOMS_HISTORY_TITLE,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = viewModel::readAloudAll) {
                    Text(UiCopy.APPOINTMENTS_READ_ALOUD)
                }
                TextButton(onClick = viewModel::clearNotes) {
                    Text(UiCopy.APPOINTMENTS_CLEAR)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            state.notes.forEach { note ->
                SymptomHistoryRow(
                    note = note,
                    onDelete = { viewModel.deleteNote(note.id) },
                    onReRecord = { startVoice(note.id) },
                    onReadAloud = { viewModel.readAloudNote(note.id) },
                )
            }
        }
    }
}

@Composable
private fun SymptomHistoryRow(
    note: SymptomNote,
    onDelete: () -> Unit,
    onReRecord: () -> Unit,
    onReadAloud: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (note.recordedAtLabel.isNotBlank()) {
                Text(
                    note.recordedAtLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    note.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onReadAloud) {
                    Icon(
                        Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = UiCopy.APPOINTMENTS_READ_SYMPTOM,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onReRecord) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = UiCopy.APPOINTMENTS_RE_RECORD,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = UiCopy.APPOINTMENTS_DELETE_NOTE,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
