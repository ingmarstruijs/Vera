package net.moonbaseone.vera.shared.pocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.appointments.AppointmentSummary
import net.moonbaseone.vera.shared.appointments.toSummary
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.i18n.StringCatalog
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.symptoms.SymptomEntry
import net.moonbaseone.vera.shared.symptoms.SymptomStore
import net.moonbaseone.vera.shared.translation.DatasetContentLocalizer
import net.moonbaseone.vera.shared.ui.UiCopy

data class PocketGuideUiState(
    val isLoading: Boolean = true,
    val appointment: AppointmentSummary? = null,
    val guide: PocketGuide? = null,
    val recentSymptoms: List<SymptomEntry> = emptyList(),
    val suggestedQuestions: List<String> = emptyList(),
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val playbackPositionMs: Long = 0L,
    val playbackDurationMs: Long = 0L,
    val errorMessage: String? = null,
)

class PocketGuideViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val pocketGuideStore: PocketGuideStore,
    private val symptomStore: SymptomStore,
    private val audioRecorder: AppointmentAudioRecorder = FakeAppointmentAudioRecorder(),
    private val audioPlayer: AppointmentAudioPlayer = FakeAppointmentAudioPlayer(),
    private val textToSpeech: TextToSpeech = FakeTextToSpeech(),
    private val contentLocalizer: DatasetContentLocalizer? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(PocketGuideUiState())
    val state: StateFlow<PocketGuideUiState> = _state.asStateFlow()

    private var appointmentId: String? = null
    private var progressJob: Job? = null
    private var preparedPath: String? = null
    private var sourceAppointment: AppointmentSummary? = null
    private var sourceGuide: PocketGuide? = null
    private var localizeJob: Job? = null
    private var collectorsStarted = false

    fun load(appointmentId: String) {
        this.appointmentId = appointmentId
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val appt = carePlanRepository.fetchAppointments()
                    .map { it.toSummary() }
                    .firstOrNull { it.id == appointmentId }
                val guide = pocketGuideStore.ensure(appointmentId)
                sourceAppointment = appt
                sourceGuide = guide
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
                preparePlaybackIfNeeded(guide.recordingPath)
                publishLocalized()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: UiCopy.APPOINTMENTS_NO_DATA,
                    )
                }
            }
        }
        if (!collectorsStarted) {
            collectorsStarted = true
            scope.launch {
                symptomStore.entries.collect { list ->
                    _state.update { it.copy(recentSymptoms = list.take(10)) }
                }
            }
            scope.launch {
                pocketGuideStore.guides.collect { map ->
                    val id = this@PocketGuideViewModel.appointmentId ?: return@collect
                    map[id]?.let { g ->
                        sourceGuide = g
                        _state.update { it.copy(guide = g) }
                        preparePlaybackIfNeeded(g.recordingPath)
                        publishLocalized()
                    }
                }
            }
            scope.launch {
                StringCatalog.activeLocaleFlow.collect {
                    publishLocalized()
                }
            }
        }
    }

    private fun publishLocalized() {
        val localizer = contentLocalizer ?: return
        val appt = sourceAppointment
        localizeJob?.cancel()
        localizeJob = scope.launch {
            val target = StringCatalog.activeLocale.tag
            // Questions stay as stored (may be typed by the patient); only hospital-facing
            // appointment fields are localized from Dutch source.
            val localizedAppt = if (appt == null) {
                null
            } else if (target == "nl" || !localizer.ensureReady(target)) {
                appt
            } else {
                localizer.localizeAppointment(appt, target)
            }
            if (appointmentId == null) return@launch
            _state.update {
                it.copy(
                    appointment = localizedAppt,
                    suggestedQuestions = UiCopy.APPOINTMENTS_DEFAULT_QUESTIONS,
                )
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
            val path = entry.audioPath
            if (!path.isNullOrBlank() && audioPlayer.isAvailable()) {
                textToSpeech.stop()
                audioPlayer.play(path)
            } else {
                textToSpeech.speak(entry.text, localeTag = "nl-NL")
            }
        }
    }

    fun addManualQuestion(text: String) {
        val id = appointmentId ?: return
        pocketGuideStore.addManualQuestion(id, text)
    }

    fun removeQuestion(actionId: String) {
        val id = appointmentId ?: return
        pocketGuideStore.unlinkActionItem(id, actionId)
    }

    fun addSuggestedQuestion(text: String) {
        addManualQuestion(text)
    }

    fun startRecording() {
        val id = appointmentId ?: return
        if (_state.value.isRecording) return
        scope.launch {
            stopPlaybackInternal()
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

    /** Stop recording and keep the file (default). */
    fun stopAndSaveRecording() {
        val id = appointmentId ?: return
        if (!_state.value.isRecording) return
        scope.launch {
            val result = audioRecorder.stop()
            result.fold(
                onSuccess = { path ->
                    pocketGuideStore.setRecordingPath(id, path)
                    _state.update { it.copy(isRecording = false) }
                    preparePlaybackIfNeeded(path)
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
        }
    }

    fun togglePlayback() {
        if (_state.value.isPlaying) {
            pausePlayback()
        } else {
            playRecording()
        }
    }

    fun playRecording() {
        val path = _state.value.guide?.recordingPath ?: return
        scope.launch {
            if (preparedPath != path) {
                preparePlaybackIfNeeded(path)
            }
            if (_state.value.playbackDurationMs <= 0L) return@launch
            if (audioPlayer.getPositionMs() >= audioPlayer.getDurationMs() &&
                audioPlayer.getDurationMs() > 0L
            ) {
                audioPlayer.seekTo(0L)
            }
            audioPlayer.play()
            _state.update {
                it.copy(
                    isPlaying = true,
                    playbackPositionMs = audioPlayer.getPositionMs(),
                )
            }
            startProgressTicker()
        }
    }

    fun pausePlayback() {
        audioPlayer.pause()
        progressJob?.cancel()
        _state.update {
            it.copy(
                isPlaying = false,
                playbackPositionMs = audioPlayer.getPositionMs(),
            )
        }
    }

    fun stopPlayback() {
        stopPlaybackInternal()
    }

    fun seekBy(deltaMs: Long) {
        audioPlayer.seekBy(deltaMs)
        _state.update { it.copy(playbackPositionMs = audioPlayer.getPositionMs()) }
    }

    fun seekTo(positionMs: Long) {
        audioPlayer.seekTo(positionMs)
        _state.update { it.copy(playbackPositionMs = audioPlayer.getPositionMs()) }
    }

    fun deleteRecording() {
        val id = appointmentId ?: return
        val path = _state.value.guide?.recordingPath
        scope.launch {
            stopPlaybackInternal()
            preparedPath = null
            if (!path.isNullOrBlank()) {
                audioRecorder.deleteFile(path)
            }
            pocketGuideStore.setRecordingPath(id, null)
            _state.update {
                it.copy(
                    playbackPositionMs = 0L,
                    playbackDurationMs = 0L,
                    isPlaying = false,
                )
            }
        }
    }

    fun dispose() {
        progressJob?.cancel()
        if (audioRecorder.isRecording()) {
            scope.launch { audioRecorder.stop() }
        }
        audioPlayer.stop()
        textToSpeech.stop()
        scope.cancel()
    }

    private suspend fun preparePlaybackIfNeeded(path: String?) {
        if (path.isNullOrBlank()) {
            stopPlaybackInternal()
            preparedPath = null
            _state.update {
                it.copy(
                    playbackPositionMs = 0L,
                    playbackDurationMs = 0L,
                    isPlaying = false,
                )
            }
            return
        }
        if (preparedPath == path && _state.value.playbackDurationMs > 0L) return
        val result = audioPlayer.prepare(path)
        result.fold(
            onSuccess = { duration ->
                preparedPath = path
                _state.update {
                    it.copy(
                        playbackDurationMs = duration,
                        playbackPositionMs = 0L,
                        isPlaying = false,
                    )
                }
            },
            onFailure = { err ->
                preparedPath = null
                _state.update {
                    it.copy(
                        playbackDurationMs = 0L,
                        playbackPositionMs = 0L,
                        isPlaying = false,
                        errorMessage = err.message,
                    )
                }
            },
        )
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val playing = audioPlayer.isPlaying()
                val position = audioPlayer.getPositionMs()
                val duration = audioPlayer.getDurationMs()
                _state.update {
                    it.copy(
                        isPlaying = playing,
                        playbackPositionMs = position,
                        playbackDurationMs = duration.coerceAtLeast(it.playbackDurationMs),
                    )
                }
                val finished = duration > 0L && position >= duration
                if (!playing || finished) {
                    if (finished) {
                        audioPlayer.stop()
                        _state.update {
                            it.copy(
                                isPlaying = false,
                                playbackPositionMs = duration,
                            )
                        }
                    }
                    break
                }
                delay(200)
            }
        }
    }

    private fun stopPlaybackInternal() {
        progressJob?.cancel()
        audioPlayer.stop()
        _state.update {
            it.copy(
                isPlaying = false,
                playbackPositionMs = 0L,
            )
        }
    }
}
