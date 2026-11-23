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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.moonbaseone.vera.shared.appointments.AppointmentSummary
import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.appointments.PocketGuide
import net.moonbaseone.vera.shared.ui.UiCopy

@Composable
fun AppointmentsScreen(viewModel: AppointmentsViewModel) {
    val state by viewModel.state.collectAsState()

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
            Spacer(modifier = Modifier.height(12.dp))
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
                    hasSavedGuide = state.savedGuideFor(appt.id) != null,
                    onSelect = { viewModel.select(appt.id) },
                    onGenerateGuide = { viewModel.generatePocketGuide(appt.id) },
                    onOpenSaved = { viewModel.openSavedGuide(appt.id) },
                )
            }
        }

        if (state.savedGuides.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(UiCopy.APPOINTMENTS_SAVED_GUIDES, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(state.savedGuides, key = { "saved-${it.appointmentId}" }) { guide ->
                SavedGuideRow(
                    guide = guide,
                    onOpen = { viewModel.openSavedGuide(guide.appointmentId) },
                )
            }
        }

        state.errorMessage?.let { msg ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }

    if (state.guideDialogOpen) {
        state.pocketGuide?.let { guide ->
            Dialog(
                onDismissRequest = viewModel::dismissGuideDialog,
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp).size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                UiCopy.APPOINTMENTS_POCKET_PANEL,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(onClick = viewModel::readAloudPocketGuide) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = UiCopy.APPOINTMENTS_READ_ALOUD,
                                )
                            }
                            TextButton(onClick = viewModel::dismissGuideDialog) {
                                Text(UiCopy.APPOINTMENTS_CLOSE_FULL)
                            }
                        }
                        HorizontalDivider()
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                        ) {
                            PocketGuideContent(
                                guide = guide,
                                onExtraNotesChange = viewModel::updateExtraNotes,
                            )
                        }
                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            if (state.guideJustSaved || guide.isSaved) {
                                Text(
                                    UiCopy.APPOINTMENTS_GUIDE_SAVED,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            Button(
                                onClick = viewModel::savePocketGuide,
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(UiCopy.APPOINTMENTS_SAVE_GUIDE)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedGuideRow(guide: PocketGuide, onOpen: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable(onClick = onOpen),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        guide.serviceType,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        guide.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
            ) {
                Text(UiCopy.APPOINTMENTS_OPEN_SAVED)
            }
        }
    }
}

@Composable
private fun PocketGuideContent(
    guide: PocketGuide,
    onExtraNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(guide.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        MetaRow(UiCopy.APPOINTMENTS_META_DATE, guide.date)
        MetaRow(UiCopy.APPOINTMENTS_META_TYPE, guide.serviceType)
        MetaRow(UiCopy.APPOINTMENTS_META_LOCATION, guide.location)
        Spacer(modifier = Modifier.height(16.dp))
        GuideSection(UiCopy.APPOINTMENTS_REASON, guide.reason)
        Spacer(modifier = Modifier.height(14.dp))
        GuideSection(UiCopy.APPOINTMENTS_SYMPTOMS_HEADER, guide.symptoms)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            UiCopy.APPOINTMENTS_SUGGESTED_QUESTIONS,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.height(6.dp))
        guide.questions.forEach { question ->
            Text(
                "• $question",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        GuideSection(UiCopy.APPOINTMENTS_MEMO, guide.memo)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            UiCopy.APPOINTMENTS_EXTRA_NOTES,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = guide.extraNotes,
            onValueChange = onExtraNotesChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp),
            placeholder = { Text(UiCopy.APPOINTMENTS_EXTRA_NOTES_HINT) },
            minLines = 3,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            guide.footer,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    ) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GuideSection(title: String, body: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentSummary,
    selected: Boolean,
    hasSavedGuide: Boolean,
    onSelect: () -> Unit,
    onGenerateGuide: () -> Unit,
    onOpenSaved: () -> Unit,
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
                Text(
                    appointment.serviceType,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val whenLabel = appointment.startDisplay.ifBlank { appointment.startIso }
            if (whenLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    whenLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (appointment.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    appointment.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (appointment.practitioner.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    appointment.practitioner,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Spacer(modifier = Modifier.height(12.dp))
                if (hasSavedGuide) {
                    OutlinedButton(
                        onClick = onOpenSaved,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(UiCopy.APPOINTMENTS_OPEN_SAVED)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
