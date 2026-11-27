package net.moonbaseone.vera.ui.decoder

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
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import net.moonbaseone.vera.shared.decoder.DecoderViewModel
import net.moonbaseone.vera.shared.decoder.TranslatedGlossaryFields
import net.moonbaseone.vera.shared.domain.JargonEntry
import net.moonbaseone.vera.shared.ui.DesignTokens
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.common.AddToPocketGuideButton
import net.moonbaseone.vera.ui.common.SoftIconCircle
import net.moonbaseone.vera.ui.theme.errorBannerBackground
import net.moonbaseone.vera.ui.theme.lavenderWash
import net.moonbaseone.vera.ui.theme.mintWash
import net.moonbaseone.vera.ui.theme.sentinelAccent
import net.moonbaseone.vera.ui.theme.sentinelBackground
import net.moonbaseone.vera.ui.theme.warningBannerBackground
import net.moonbaseone.vera.ui.theme.warningBannerForeground
import net.moonbaseone.vera.ui.util.openExternalUrl

@Composable
fun DecoderScreen(viewModel: DecoderViewModel) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            PrivacyNote(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (!state.isTranslating && state.downloadProgress == null) {
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
                    shape = RoundedCornerShape(16.dp),
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
        }

        if (state.entries.isEmpty()) {
            item {
                Text(
                    UiCopy.DECODER_NO_RESULTS,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            items(state.entries, key = { it.term }) { entry ->
                JargonCard(
                    entry = entry,
                    translated = state.translatedEntries[entry.term],
                    expanded = state.expandedTerm == entry.term,
                    onToggle = { viewModel.toggleExpanded(entry.term) },
                    modifier = Modifier.padding(horizontal = 20.dp),
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
        shape = RoundedCornerShape(12.dp),
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
private fun PrivacyNote(modifier: Modifier = Modifier) {
    val accent = sentinelAccent()
    Surface(
        color = sentinelBackground(),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftIconCircle(
                icon = Icons.Outlined.Shield,
                size = 28.dp,
                wash = mintWash(),
                tint = accent,
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
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val explanation = translated?.explanation ?: entry.dutchExplanation
    val actionItem = translated?.actionItem ?: entry.actionItem

    Surface(
        shape = RoundedCornerShape(DesignTokens.CARD_RADIUS_DP.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable(onClick = onToggle),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoftIconCircle(
                    icon = Icons.Outlined.Translate,
                    size = 36.dp,
                    wash = lavenderWash(),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    entry.term,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                )
                if (entry.category.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = mintWash(),
                    ) {
                        Text(
                            entry.category,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
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
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            Icons.Outlined.TaskAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                UiCopy.DECODER_ACTION_LABEL,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(actionItem, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                entry.iknlRef?.let { url ->
                    TextButton(onClick = { context.openExternalUrl(url) }) {
                        Text(UiCopy.DECODER_IKNL_BUTTON)
                    }
                }
                if (actionItem.isNotBlank()) {
                    AddToPocketGuideButton(
                        actionSource = "decoder:${entry.term}",
                        actionText = actionItem,
                    )
                }
            }
        }
    }
}
