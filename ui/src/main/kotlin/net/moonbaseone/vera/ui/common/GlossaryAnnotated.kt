package net.moonbaseone.vera.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import net.moonbaseone.vera.shared.domain.GlossaryLinker
import net.moonbaseone.vera.shared.domain.JargonEntry
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.ui.util.openExternalUrl

@Composable
fun GlossaryLinkedText(
    text: String,
    modifier: Modifier = Modifier,
    onAddToPocketGuide: ((JargonEntry) -> Unit)? = null,
) {
    val spans = remember(text) { GlossaryLinker.findSpans(text) }
    var openEntry by remember { mutableStateOf<JargonEntry?>(null) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val linkColor = MaterialTheme.colorScheme.primary

    val annotated = remember(text, spans, linkColor) {
        buildAnnotatedString {
            if (spans.isEmpty()) {
                append(text)
            } else {
                var cursor = 0
                spans.forEach { span ->
                    if (span.startIndex > cursor) {
                        append(text.substring(cursor, span.startIndex))
                    }
                    pushStringAnnotation(tag = "jargon", annotation = span.entry.term)
                    withStyle(
                        SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(text.substring(span.startIndex, span.endIndex))
                    }
                    pop()
                    cursor = span.endIndex
                }
                if (cursor < text.length) append(text.substring(cursor))
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.pointerInput(annotated) {
            detectTapGestures { pos ->
                val layoutResult = layout ?: return@detectTapGestures
                val offset = layoutResult.getOffsetForPosition(pos)
                annotated.getStringAnnotations("jargon", offset, offset)
                    .firstOrNull()
                    ?.let { ann ->
                        openEntry = spans.firstOrNull { it.entry.term == ann.item }?.entry
                    }
            }
        },
        onTextLayout = { layout = it },
    )

    openEntry?.let { entry ->
        JargonExplainSheet(
            entry = entry,
            onDismiss = { openEntry = null },
            onAddToPocketGuide = onAddToPocketGuide?.let { cb ->
                {
                    cb(entry)
                    openEntry = null
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JargonExplainSheet(
    entry: JargonEntry,
    onDismiss: () -> Unit,
    onAddToPocketGuide: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(entry.term, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(entry.dutchExplanation, style = MaterialTheme.typography.bodyMedium)
            entry.iknlRef?.let { url ->
                TextButton(onClick = { context.openExternalUrl(url) }) {
                    Text(UiCopy.DECODER_IKNL_BUTTON)
                }
            }
            if (entry.actionItem.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(UiCopy.DECODER_ACTION_LABEL, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(entry.actionItem, style = MaterialTheme.typography.bodySmall)
                if (onAddToPocketGuide != null) {
                    Text(
                        UiCopy.ADD_TO_POCKET_GUIDE,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onAddToPocketGuide)
                            .padding(top = 2.dp, bottom = 2.dp),
                    )
                } else {
                    AddToPocketGuideButton(
                        actionSource = "jargon:${entry.term}",
                        actionText = entry.actionItem,
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
