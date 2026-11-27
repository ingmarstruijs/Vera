package net.moonbaseone.vera.ui.symptoms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import net.moonbaseone.vera.shared.symptoms.SymptomEntry
import net.moonbaseone.vera.shared.symptoms.SymptomsViewModel
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy

@Composable
fun SymptomsScreen(viewModel: SymptomsViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.toggleListening()
    }

    fun requestOrToggle() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.toggleListening()
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        if (state.speechAvailable) {
            Button(
                onClick = { requestOrToggle() },
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
                    if (state.isListening) UiCopy.APPOINTMENTS_VOICE_STOP else UiCopy.SYMPTOMS_RECORD_CTA,
                )
            }
            if (state.isListening) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    UiCopy.APPOINTMENTS_VOICE_LISTENING,
                    color = Color(DesignTokens.ERROR_RED),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (state.draftText.isNotBlank()) {
                    Text(state.draftText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            Text(UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE, style = MaterialTheme.typography.bodySmall)
        }

        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (state.entries.isEmpty()) {
            Text(UiCopy.SYMPTOMS_EMPTY, style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.entries, key = { it.id }) { entry ->
                    SymptomRow(
                        entry = entry,
                        onSpeak = { viewModel.speak(entry.id) },
                        onDelete = { viewModel.delete(entry.id) },
                    )
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun SymptomRecordSheetContent(
    viewModel: SymptomsViewModel,
    onFinished: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.startListening()
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text(UiCopy.SYMPTOMS_RECORD_CTA, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                if (state.isListening) {
                    viewModel.stopListening()
                    onFinished()
                } else {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) viewModel.startListening()
                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Icon(
                if (state.isListening) Icons.Outlined.Stop else Icons.Outlined.MicNone,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (state.isListening) UiCopy.APPOINTMENTS_VOICE_STOP else UiCopy.SYMPTOMS_RECORD_CTA,
            )
        }
        if (state.draftText.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.draftText)
        }
    }
}

@Composable
private fun SymptomRow(
    entry: SymptomEntry,
    onSpeak: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                entry.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onSpeak,
                modifier = Modifier.semantics { contentDescription = UiCopy.SYMPTOMS_SPEAK },
            ) {
                Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = UiCopy.SYMPTOMS_SPEAK)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.semantics { contentDescription = UiCopy.SYMPTOMS_DELETE },
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = UiCopy.SYMPTOMS_DELETE, modifier = Modifier.size(22.dp))
            }
        }
    }
}
