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
import net.moonbaseone.vera.shared.pocket.AppointmentAudioPlayer
import net.moonbaseone.vera.shared.pocket.AppointmentAudioRecorder
import net.moonbaseone.vera.shared.pocket.FakeAppointmentAudioPlayer
import net.moonbaseone.vera.shared.pocket.FakeAppointmentAudioRecorder
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.speech.SpeechToText
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.ui.UiCopy

data class SymptomsUiState(
    val entries: List<SymptomEntry> = emptyList(),
    val isListening: Boolean = false,
    val draftText: String = "",
    /** True when STT and/or local mic recording can capture a voice note. */
    val speechAvailable: Boolean = false,
    val errorMessage: String? = null,
    val playingEntryId: String? = null,
)

class SymptomsViewModel(
    private val symptomStore: SymptomStore,
    private val speechToText: SpeechToText = FakeSpeechToText(),
    private val textToSpeech: TextToSpeech = FakeTextToSpeech(),
    private val audioRecorder: AppointmentAudioRecorder = FakeAppointmentAudioRecorder(),
    private val audioPlayer: AppointmentAudioPlayer = FakeAppointmentAudioPlayer(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(SymptomsUiState())
    val state: StateFlow<SymptomsUiState> = _state.asStateFlow()

    /** When false, a completed recognition session must not persist text. */
    private var commitOnComplete: Boolean = true

    /** True while capturing via MediaRecorder (GrapheneOS / no STT). */
    private var audioCaptureActive: Boolean = false

    init {
        scope.launch {
            val stt = speechToText.isAvailable()
            val mic = audioRecorder.isAvailable()
            _state.update { it.copy(speechAvailable = stt || mic) }
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
            commitOnComplete = true
            _state.update { it.copy(isListening = true, draftText = "", errorMessage = null) }

            if (speechToText.isAvailable()) {
                audioCaptureActive = false
                startSttSession()
            } else {
                startAudioSession()
            }
        }
    }

    private suspend fun startSttSession() {
        val result = speechToText.listen(
            localeTag = "nl-NL",
            onPartial = { partial ->
                _state.update { it.copy(draftText = partial) }
            },
        )
        result.fold(
            onSuccess = { text ->
                val shouldCommit = commitOnComplete
                val finalText = text.trim().ifBlank { _state.value.draftText.trim() }
                if (shouldCommit && finalText.isNotEmpty()) {
                    symptomStore.add(finalText)
                }
                _state.update { it.copy(isListening = false, draftText = "") }
            },
            onFailure = { err ->
                _state.update { current ->
                    if (!current.isListening || !commitOnComplete) {
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

    private suspend fun startAudioSession() {
        audioCaptureActive = true
        val started = audioRecorder.start("symptom")
        started.onFailure { err ->
            audioCaptureActive = false
            _state.update {
                it.copy(
                    isListening = false,
                    draftText = "",
                    errorMessage = err.message ?: UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE,
                )
            }
        }
    }

    /** Finish recognition/recording and keep the result if any. */
    fun stopAndSave() {
        if (!_state.value.isListening) return
        commitOnComplete = true
        if (audioCaptureActive) {
            scope.launch {
                val pathResult = audioRecorder.stop()
                audioCaptureActive = false
                pathResult.fold(
                    onSuccess = { path ->
                        symptomStore.add(
                            text = UiCopy.SYMPTOMS_VOICE_NOTE,
                            audioPath = path,
                        )
                        _state.update { it.copy(isListening = false, draftText = "") }
                    },
                    onFailure = { err ->
                        _state.update {
                            it.copy(
                                isListening = false,
                                draftText = "",
                                errorMessage = err.message,
                            )
                        }
                    },
                )
            }
        } else {
            speechToText.stop()
        }
    }

    /** Abort recognition/recording and drop any draft. */
    fun stopAndDiscard() {
        if (!_state.value.isListening) return
        commitOnComplete = false
        if (audioCaptureActive) {
            scope.launch {
                val pathResult = audioRecorder.stop()
                audioCaptureActive = false
                pathResult.getOrNull()?.let { audioRecorder.deleteFile(it) }
                _state.update { it.copy(isListening = false, draftText = "") }
            }
        } else {
            speechToText.cancel()
            _state.update { it.copy(isListening = false, draftText = "") }
        }
    }

    /** Abort an in-flight session (e.g. sheet dismissed). */
    fun cancelListening() {
        commitOnComplete = false
        if (audioCaptureActive) {
            scope.launch {
                val pathResult = audioRecorder.stop()
                audioCaptureActive = false
                pathResult.getOrNull()?.let { audioRecorder.deleteFile(it) }
                _state.update { it.copy(isListening = false, draftText = "") }
            }
        } else {
            speechToText.cancel()
            _state.update { it.copy(isListening = false, draftText = "") }
        }
    }

    fun toggleListening() {
        if (_state.value.isListening) stopAndSave() else startListening()
    }

    fun speak(entryId: String) {
        val entry = _state.value.entries.firstOrNull { it.id == entryId } ?: return
        scope.launch {
            val path = entry.audioPath
            if (!path.isNullOrBlank() && audioPlayer.isAvailable()) {
                if (_state.value.playingEntryId == entryId) {
                    audioPlayer.stop()
                    _state.update { it.copy(playingEntryId = null) }
                    return@launch
                }
                textToSpeech.stop()
                _state.update { it.copy(playingEntryId = entryId) }
                audioPlayer.play(path).fold(
                    onSuccess = { _state.update { it.copy(playingEntryId = null) } },
                    onFailure = { err ->
                        _state.update {
                            it.copy(
                                playingEntryId = null,
                                errorMessage = err.message,
                            )
                        }
                    },
                )
            } else {
                textToSpeech.speak(entry.text, localeTag = "nl-NL")
            }
        }
    }

    fun addTyped(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        symptomStore.add(trimmed)
        return true
    }

    fun delete(entryId: String) {
        val entry = _state.value.entries.firstOrNull { it.id == entryId }
        entry?.audioPath?.let { audioRecorder.deleteFile(it) }
        if (_state.value.playingEntryId == entryId) {
            audioPlayer.stop()
            _state.update { it.copy(playingEntryId = null) }
        }
        symptomStore.delete(entryId)
    }

    fun dispose() {
        speechToText.cancel()
        textToSpeech.stop()
        audioPlayer.stop()
        if (audioCaptureActive) {
            scope.launch {
                audioRecorder.stop().getOrNull()?.let { audioRecorder.deleteFile(it) }
            }
        }
        scope.cancel()
    }
}
