package net.moonbaseone.vera.shared.symptoms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.speech.SpeechToText
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.ui.UiCopy

data class SymptomsUiState(
    val entries: List<SymptomEntry> = emptyList(),
    val isListening: Boolean = false,
    val draftText: String = "",
    val speechAvailable: Boolean = false,
    val errorMessage: String? = null,
)

class SymptomsViewModel(
    private val symptomStore: SymptomStore,
    private val speechToText: SpeechToText = FakeSpeechToText(),
    private val textToSpeech: TextToSpeech = FakeTextToSpeech(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(SymptomsUiState())
    val state: StateFlow<SymptomsUiState> = _state.asStateFlow()

    init {
        scope.launch {
            val available = speechToText.isAvailable()
            _state.update { it.copy(speechAvailable = available) }
        }
        scope.launch {
            symptomStore.entries.collect { list ->
                _state.update { it.copy(entries = list) }
            }
        }
    }

    fun startListening() {
        scope.launch {
            val current = _state.value
            if (!current.speechAvailable || current.isListening) return@launch
            _state.update { it.copy(isListening = true, draftText = "", errorMessage = null) }
            val result = speechToText.listen(
                localeTag = "nl-NL",
                onPartial = { partial ->
                    _state.update { it.copy(draftText = partial) }
                },
            )
            result.fold(
                onSuccess = { text ->
                    val finalText = text.trim().ifBlank { _state.value.draftText.trim() }
                    if (finalText.isNotEmpty()) {
                        symptomStore.add(finalText)
                    }
                    _state.update { it.copy(isListening = false, draftText = "") }
                },
                onFailure = { err ->
                    _state.update { current ->
                        if (!current.isListening) {
                            // Explicit cancel already cleared listening; don't surface an error.
                            current.copy(isListening = false, draftText = "")
                        } else {
                            current.copy(
                                isListening = false,
                                draftText = "",
                                errorMessage = err.message
                                    ?: UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE,
                            )
                        }
                    }
                },
            )
        }
    }

    fun stopListening() {
        speechToText.stop()
        _state.update { it.copy(isListening = false) }
    }

    /** Abort an in-flight recognition session (e.g. sheet dismissed). */
    fun cancelListening() {
        speechToText.cancel()
        _state.update { it.copy(isListening = false, draftText = "") }
    }

    fun toggleListening() {
        if (_state.value.isListening) stopListening() else startListening()
    }

    fun speak(entryId: String) {
        val entry = _state.value.entries.firstOrNull { it.id == entryId } ?: return
        scope.launch {
            textToSpeech.speak(entry.text, localeTag = "nl-NL")
        }
    }

    fun delete(entryId: String) {
        symptomStore.delete(entryId)
    }

    fun dispose() {
        speechToText.cancel()
        textToSpeech.stop()
        scope.cancel()
    }
}
