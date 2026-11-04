package net.moonbaseone.vera.shared.decoder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.moonbaseone.vera.shared.domain.BuiltInGlossary
import net.moonbaseone.vera.shared.domain.JargonEntry
import net.moonbaseone.vera.shared.ui.UiCopy

data class TranslationLanguage(
    val code: String,
    val label: String,
)

val SupportedDecoderLanguages: List<TranslationLanguage> = listOf(
    TranslationLanguage("nl", "Nederlands"),
    TranslationLanguage("en", "Engels"),
    TranslationLanguage("de", "Duits"),
    TranslationLanguage("fr", "Frans"),
)

data class DecoderUiState(
    val query: String = "",
    val selectedLanguage: String = "nl",
    val entries: List<JargonEntry> = BuiltInGlossary,
    val expandedTerm: String? = null,
    /** Non-null when a non-NL language is selected before P7 ML Kit lands. */
    val translationNotice: String? = null,
)

/**
 * Glossary search / expand. Full on-device ML Kit translation is P7 —
 * non-NL selection surfaces a local stub notice only.
 */
class DecoderViewModel {
    private val _state = MutableStateFlow(DecoderUiState())
    val state: StateFlow<DecoderUiState> = _state.asStateFlow()

    fun setQuery(query: String) {
        _state.update { current ->
            current.copy(
                query = query,
                entries = filter(query),
            )
        }
    }

    fun setLanguage(code: String) {
        val notice = if (code == "nl") {
            null
        } else {
            UiCopy.DECODER_TRANSLATION_PENDING
        }
        _state.update {
            it.copy(
                selectedLanguage = code,
                translationNotice = notice,
            )
        }
    }

    fun toggleExpanded(term: String) {
        _state.update {
            it.copy(expandedTerm = if (it.expandedTerm == term) null else term)
        }
    }

    private fun filter(query: String): List<JargonEntry> {
        if (query.isBlank()) return BuiltInGlossary
        val q = query.lowercase()
        return BuiltInGlossary.filter { entry ->
            entry.term.lowercase().contains(q) ||
                entry.dutchExplanation.lowercase().contains(q) ||
                entry.category.lowercase().contains(q)
        }
    }
}
