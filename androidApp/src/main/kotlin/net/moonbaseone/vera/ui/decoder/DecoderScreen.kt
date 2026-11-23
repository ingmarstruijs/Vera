package net.moonbaseone.vera.ui.decoder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.decoder.DecoderViewModel
import net.moonbaseone.vera.shared.decoder.SupportedDecoderLanguages
import net.moonbaseone.vera.shared.decoder.TranslatedGlossaryFields
import net.moonbaseone.vera.shared.domain.JargonEntry
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.theme.errorBannerBackground
import net.moonbaseone.vera.ui.theme.sentinelAccent
import net.moonbaseone.vera.ui.theme.sentinelBackground
import net.moonbaseone.vera.ui.theme.warningBannerBackground
import net.moonbaseone.vera.ui.theme.warningBannerForeground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoderScreen(viewModel: DecoderViewModel) {
    val state by viewModel.state.collectAsState()
    var languageMenuOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            Text(UiCopy.DECODER_LANGUAGE_LABEL, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = languageMenuOpen,
                onExpandedChange = { languageMenuOpen = it },
            ) {
                val label = SupportedDecoderLanguages
                    .firstOrNull { it.code == state.selectedLanguage }
                    ?.label
                    ?: state.selectedLanguage
                TextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuOpen) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = languageMenuOpen,
                    onDismissRequest = { languageMenuOpen = false },
                ) {
                    SupportedDecoderLanguages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.label) },
                            onClick = {
                                viewModel.setLanguage(lang.code)
                                languageMenuOpen = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            PrivacyNote()
            Spacer(modifier = Modifier.height(12.dp))

            state.downloadProgress?.let { progress ->
                Text(
                    UiCopy.DECODER_DOWNLOADING_MODEL,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (state.isTranslating) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        state.translationNotice ?: UiCopy.DECODER_TRANSLATING,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                state.translationNotice?.let { notice ->
                    StatusBanner(notice, warningBannerBackground(), warningBannerForeground())
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            state.translationError?.let { error ->
                StatusBanner(error, errorBannerBackground(), MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = UiCopy.DECODER_SEARCH_HINT },
                placeholder = { Text(UiCopy.DECODER_SEARCH_HINT) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Zoekopdracht wissen")
                        }
                    }
                },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.entries.isEmpty()) {
            item {
                Text(UiCopy.DECODER_NO_RESULTS, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            items(state.entries, key = { it.term }) { entry ->
                JargonCard(
                    entry = entry,
                    translated = state.translatedEntries[entry.term],
                    expanded = state.expandedTerm == entry.term,
                    onToggle = { viewModel.toggleExpanded(entry.term) },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun StatusBanner(message: String, background: Color, foreground: Color) {
    Surface(
        color = background,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = foreground,
        )
    }
}

@Composable
private fun PrivacyNote() {
    val accent = sentinelAccent()
    Surface(
        color = sentinelBackground(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accent),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Shield,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                UiCopy.DECODER_PRIVACY_NOTE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun JargonCard(
    entry: JargonEntry,
    translated: TranslatedGlossaryFields?,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val explanation = translated?.explanation ?: entry.dutchExplanation
    val actionItem = translated?.actionItem ?: entry.actionItem

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onToggle),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.term,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (entry.category.isNotEmpty()) {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        enabled = false,
                        label = { Text(entry.category, style = MaterialTheme.typography.labelSmall) },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(explanation, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            Icons.Outlined.TaskAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                UiCopy.DECODER_ACTION_LABEL,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(actionItem, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (entry.iknlRef != null) {
                    TextButton(onClick = { /* open later */ }) {
                        Text(UiCopy.DECODER_IKNL_BUTTON)
                    }
                }
            }
        }
    }
}
