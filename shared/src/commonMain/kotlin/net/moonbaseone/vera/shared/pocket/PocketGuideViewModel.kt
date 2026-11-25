package net.moonbaseone.vera.shared.pocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.appointments.AppointmentSummary
import net.moonbaseone.vera.shared.appointments.toSummary
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.symptoms.SymptomEntry
import net.moonbaseone.vera.shared.symptoms.SymptomStore
import net.moonbaseone.vera.shared.ui.UiCopy

data class PocketGuideUiState(
    val isLoading: Boolean = true,
    val appointment: AppointmentSummary? = null,
    val guide: PocketGuide? = null,
    val recentSymptoms: List<SymptomEntry> = emptyList(),
    val suggestedQuestions: List<String> = emptyList(),
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null,
)

class PocketGuideViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val pocketGuideStore: PocketGuideStore,
    private val symptomStore: SymptomStore,
    private val audioRecorder: AppointmentAudioRecorder = FakeAppointmentAudioRecorder(),
    private val audioPlayer: AppointmentAudioPlayer = FakeAppointmentAudioPlayer(),
    private val textToSpeech: TextToSpeech = FakeTextToSpeech(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(PocketGuideUiState())
    val state: StateFlow<PocketGuideUiState> = _state.asStateFlow()

    private var appointmentId: String? = null

    fun load(appointmentId: String) {
        this.appointmentId = appointmentId
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val appt = carePlanRepository.fetchAppointments()
                    .map { it.toSummary() }
                    .firstOrNull { it.id == appointmentId }
                val guide = pocketGuideStore.ensure(appointmentId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        appointment = appt,
                        guide = guide,
                        recentSymptoms = symptomStore.recent(10),
                        suggestedQuestions = UiCopy.APPOINTMENTS_DEFAULT_QUESTIONS,
                        isRecording = audioRecorder.isRecording(),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: UiCopy.APPOINTMENTS_NO_DATA,
                    )
                }
            }
        }
        scope.launch {
            symptomStore.entries.collect { list ->
                _state.update { it.copy(recentSymptoms = list.take(10)) }
            }
        }
        scope.launch {
            pocketGuideStore.guides.collect { map ->
                val id = this@PocketGuideViewModel.appointmentId ?: return@collect
                map[id]?.let { g -> _state.update { it.copy(guide = g) } }
            }
        }
    }

    fun readAloud() {
        val appt = _state.value.appointment ?: return
        val guide = _state.value.guide
        val body = buildString {
            appendLine(appt.serviceType)
            appendLine(appt.startIso)
            appendLine(appt.reason)
            guide?.linkedActionTexts?.forEach { appendLine(it) }
            _state.value.recentSymptoms.forEach { appendLine(it.text) }
            UiCopy.APPOINTMENTS_DEFAULT_QUESTIONS.forEach { appendLine(it) }
        }
        scope.launch {
            textToSpeech.speak(body, localeTag = "nl-NL")
        }
    }

    fun speakSymptom(entryId: String) {
        val entry = _state.value.recentSymptoms.firstOrNull { it.id == entryId } ?: return
        scope.launch {
            textToSpeech.speak(entry.text, localeTag = "nl-NL")
        }
    }

    fun toggleRecording() {
        val id = appointmentId ?: return
        scope.launch {
            if (audioRecorder.isRecording()) {
                val result = audioRecorder.stop()
                result.fold(
                    onSuccess = { path ->
                        pocketGuideStore.setRecordingPath(id, path)
                        _state.update { it.copy(isRecording = false) }
                    },
                    onFailure = { err ->
                        _state.update {
                            it.copy(
                                isRecording = false,
                                errorMessage = err.message,
                            )
                        }
                    },
                )
            } else {
                val result = audioRecorder.start(id)
                result.fold(
                    onSuccess = {
                        _state.update { it.copy(isRecording = true, errorMessage = null) }
                    },
                    onFailure = { err ->
                        _state.update { it.copy(errorMessage = err.message) }
                    },
                )
            }
        }
    }

    fun playRecording() {
        val path = _state.value.guide?.recordingPath ?: return
        scope.launch {
            _state.update { it.copy(isPlaying = true) }
            audioPlayer.play(path)
            _state.update { it.copy(isPlaying = false) }
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
        _state.update { it.copy(isPlaying = false) }
    }

    fun dispose() {
        if (audioRecorder.isRecording()) {
            scope.launch { audioRecorder.stop() }
        }
        audioPlayer.stop()
        textToSpeech.stop()
        scope.cancel()
    }
}
