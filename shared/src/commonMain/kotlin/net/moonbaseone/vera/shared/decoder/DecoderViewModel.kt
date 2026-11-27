package net.moonbaseone.vera.shared.decoder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.domain.BuiltInGlossary
import net.moonbaseone.vera.shared.domain.JargonEntry
import net.moonbaseone.vera.shared.i18n.StringCatalog
import net.moonbaseone.vera.shared.translation.LocalTranslationService
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

data class TranslatedGlossaryFields(
    val explanation: String,
    val actionItem: String,
)

data class DecoderUiState(
    val query: String = "",
    val selectedLanguage: String = "nl",
    val entries: List<JargonEntry> = BuiltInGlossary,
    val expandedTerm: String? = null,
    /** term → translated explanation + action (non-NL). */
    val translatedEntries: Map<String, TranslatedGlossaryFields> = emptyMap(),
    val isTranslating: Boolean = false,
    /** 0f..1f while downloading a language model; null when idle. */
    val downloadProgress: Float? = null,
    /** 0f..1f while translating glossary entries; null when idle. */
    val translationProgress: Float? = null,
    val translationNotice: String? = null,
    val translationError: String? = null,
)

/**
 * Glossary search / expand with on-device translation via [LocalTranslationService].
 * Target language follows [StringCatalog.activeLocale] (app language setting).
 */
class DecoderViewModel(
    private val translation: LocalTranslationService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(DecoderUiState())
    val state: StateFlow<DecoderUiState> = _state.asStateFlow()

    private var languageJob: Job? = null

    init {
        scope.launch {
            StringCatalog.activeLocaleFlow.collect { locale ->
                setLanguage(locale.tag)
            }
        }
    }

    fun setQuery(query: String) {
        _state.update { current ->
            current.copy(
                query = query,
                entries = filter(query),
            )
        }
    }

    fun setLanguage(code: String) {
        val current = _state.value
        if (current.selectedLanguage == code) {
            if (code == "nl") return
            if (current.isTranslating ||
                current.translatedEntries.isNotEmpty() ||
                current.translationError != null ||
                current.translationNotice != null
            ) {
                return
            }
        }
        languageJob?.cancel()
        if (code == "nl") {
            _state.update {
                it.copy(
                    selectedLanguage = code,
                    translatedEntries = emptyMap(),
                    isTranslating = false,
                    downloadProgress = null,
                    translationProgress = null,
                    translationNotice = null,
                    translationError = null,
                )
            }
            return
        }

        _state.update {
            it.copy(
                selectedLanguage = code,
                translatedEntries = emptyMap(),
                isTranslating = false,
                downloadProgress = null,
                translationProgress = null,
                translationNotice = null,
                translationError = null,
            )
        }

        languageJob = scope.launch {
            if (!translation.isAvailable()) {
                _state.update {
                    it.copy(translationNotice = UiCopy.DECODER_TRANSLATION_UNAVAILABLE)
                }
                return@launch
            }

            if (!translation.isModelDownloaded(code)) {
                _state.update {
                    it.copy(
                        downloadProgress = 0.05f,
                        translationNotice = UiCopy.DECODER_DOWNLOADING_MODEL,
                        translationError = null,
                    )
                }
                val download = translation.downloadModel(code) { progress ->
                    _state.update { s ->
                        s.copy(
                            downloadProgress = progress.coerceIn(0f, 1f),
                            translationNotice = UiCopy.DECODER_DOWNLOADING_MODEL,
                        )
                    }
                }
                download.onFailure { err ->
                    _state.update {
                        it.copy(
                            downloadProgress = null,
                            translationProgress = null,
                            translationNotice = null,
                            translationError = err.message ?: UiCopy.DECODER_DOWNLOAD_FAILED,
                        )
                    }
                    return@launch
                }
                _state.update {
                    it.copy(downloadProgress = null, translationNotice = null)
                }
            }

            translateAll(code)
        }
    }

    fun toggleExpanded(term: String) {
        _state.update {
            it.copy(expandedTerm = if (it.expandedTerm == term) null else term)
        }
    }

    fun dispose() {
        languageJob?.cancel()
        scope.cancel()
    }

    private suspend fun translateAll(langCode: String) {
        val total = BuiltInGlossary.size.coerceAtLeast(1)
        _state.update {
            it.copy(
                isTranslating = true,
                downloadProgress = null,
                translationProgress = 0f,
                translatedEntries = emptyMap(),
                translationNotice = UiCopy.DECODER_TRANSLATING,
                translationError = null,
            )
        }
        val translated = linkedMapOf<String, TranslatedGlossaryFields>()
        var firstError: String? = null

        BuiltInGlossary.forEachIndexed { index, entry ->
            // Stop publishing if the user switched language mid-flight.
            if (_state.value.selectedLanguage != langCode) return

            val explanation = translation.translate(entry.dutchExplanation, langCode)
            val action = translation.translate(entry.actionItem, langCode)
            if (explanation.failure != null && firstError == null) {
                firstError = explanation.failure.message
            }
            if (action.failure != null && firstError == null) {
                firstError = action.failure.message
            }
            translated[entry.term] = TranslatedGlossaryFields(
                explanation = explanation.text ?: entry.dutchExplanation,
                actionItem = action.text ?: entry.actionItem,
            )
            val progress = ((index + 1).toFloat() / total.toFloat()).coerceIn(0f, 1f)
            _state.update {
                it.copy(
                    translatedEntries = translated.toMap(),
                    translationProgress = progress,
                    translationNotice = UiCopy.DECODER_TRANSLATING,
                )
            }
        }

        if (_state.value.selectedLanguage != langCode) return

        _state.update {
            it.copy(
                isTranslating = false,
                downloadProgress = null,
                translationProgress = null,
                translatedEntries = translated,
                translationNotice = null,
                translationError = firstError,
            )
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
