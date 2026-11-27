package net.moonbaseone.vera.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.util.formatSymptomWhen

@Composable
fun SymptomDetailDialog(
    text: String,
    createdAtMillis: Long,
    onDismiss: () -> Unit,
) {
    val whenLabel = formatSymptomWhen(createdAtMillis)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(UiCopy.SYMPTOMS_TITLE) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                if (whenLabel.isNotBlank()) {
                    Text(
                        whenLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(text, style = MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(UiCopy.APPOINTMENTS_CLOSE_FULL)
            }
        },
    )
}

/** Truncated symptom line with optional timestamp; tap opens detail via [onOpen]. */
@Composable
fun SymptomPreviewText(
    text: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    createdAtMillis: Long = 0L,
    maxLines: Int = 2,
) {
    val whenLabel = formatSymptomWhen(createdAtMillis)
    Column(
        modifier = modifier
            .clickable(onClick = onOpen)
            .semantics {
                role = Role.Button
                contentDescription = UiCopy.SYMPTOMS_VIEW_FULL
            },
    ) {
        if (whenLabel.isNotBlank()) {
            Text(
                whenLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
