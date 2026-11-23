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
import kotlin.random.Random

data class SymptomNote(
    val id: String,
    val text: String,
    /** Newest notes appear first in the UI; label is set at capture time. */
    val recordedAtLabel: String = "",
)

data class SymptomLogUiState(
    val notes: List<SymptomNote> = emptyList(),
    val draftText: String = "",
    val reRecordNoteId: String? = null,
    val isListening: Boolean = false,
    val speechAvailable: Boolean = false,
    val errorMessage: String? = null,
) {
    val notesText: String
        get() = notes.asReversed().joinToString("\n") { it.text }
}

/**
 * Home-screen symptom diary: capture, history, re-read and re-record.
 * Shared so appointments can pull the latest notes into a pocket guide.
 */
class SymptomLogViewModel(
    private val speechToText: SpeechToText = FakeSpeechToText(),
    private val textToSpeech: TextToSpeech = FakeTextToSpeech(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(SymptomLogUiState())
    val state: StateFlow<SymptomLogUiState> = _state.asStateFlow()

    fun load() {
        scope.launch {
            _state.update { it.copy(speechAvailable = speechToText.isAvailable()) }
        }
    }

    fun toggleVoiceLogging(replaceNoteId: String? = null) {
        scope.launch {
            val current = _state.value
            if (!current.speechAvailable) return@launch
            if (current.isListening) {
                speechToText.stop()
                _state.update {
                    it.copy(isListening = false, draftText = "", reRecordNoteId = null)
                }
                return@launch
            }
            _state.update {
                it.copy(
                    isListening = true,
                    errorMessage = null,
                    reRecordNoteId = replaceNoteId,
                    draftText = "",
                )
            }
            val result = speechToText.listen(
                localeTag = "nl-NL",
                onPartial = { partial ->
                    _state.update { state -> state.copy(draftText = partial.trim()) }
                },
            )
            result.fold(
                onSuccess = { text ->
                    val trimmed = text.trim()
                    _state.update { state ->
                        val notes = if (trimmed.isEmpty()) {
                            state.notes
                        } else {
                            applyUtterance(state.notes, state.reRecordNoteId, trimmed)
                        }
                        state.copy(
                            isListening = false,
                            draftText = "",
                            reRecordNoteId = null,
                            notes = notes,
                        )
                    }
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(
                            isListening = false,
                            draftText = "",
                            reRecordNoteId = null,
                            errorMessage = err.message ?: UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE,
                        )
                    }
                },
            )
        }
    }

    fun deleteNote(id: String) {
        _state.update { state ->
            state.copy(notes = state.notes.filterNot { it.id == id })
        }
    }

    fun clearNotes() {
        _state.update { it.copy(notes = emptyList(), draftText = "") }
    }

    fun readAloudNote(id: String) {
        val note = _state.value.notes.firstOrNull { it.id == id } ?: return
        scope.launch {
            textToSpeech.speak(note.text, localeTag = "nl-NL")
        }
    }

    fun readAloudAll() {
        val text = _state.value.notesText
        if (text.isBlank()) return
        scope.launch {
            textToSpeech.speak(text, localeTag = "nl-NL")
        }
    }

    fun dispose() {
        speechToText.cancel()
        textToSpeech.stop()
        scope.cancel()
    }

    private fun applyUtterance(
        notes: List<SymptomNote>,
        replaceId: String?,
        text: String,
    ): List<SymptomNote> {
        if (replaceId != null) {
            val index = notes.indexOfFirst { it.id == replaceId }
            if (index >= 0) {
                return notes.toMutableList().also {
                    it[index] = notes[index].copy(text = text)
                }
            }
        }
        // Newest first.
        return listOf(
            SymptomNote(
                id = newNoteId(),
                text = text,
                recordedAtLabel = UiCopy.SYMPTOMS_JUST_NOW,
            ),
        ) + notes
    }

    private fun newNoteId(): String = "sym-${Random.nextLong().toString(16)}"
}
