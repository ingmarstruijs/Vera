package net.moonbaseone.vera.shared.appointments

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.careplan.LocalCarePlanRepository
import net.moonbaseone.vera.shared.fhir.FhirResource
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.speech.SpeechToText
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.ui.UiCopy

data class AppointmentSummary(
    val id: String,
    val serviceType: String,
    val startIso: String,
    val location: String,
    val practitioner: String,
    val reason: String,
    val comment: String,
)

/**
 * Structured pocket guide shown as readable sections (no markdown syntax in UI).
 * [plainText] is used for TTS / share-style export.
 */
data class PocketGuide(
    val appointmentId: String,
    val title: String,
    val date: String,
    val serviceType: String,
    val location: String,
    val reason: String,
    val symptoms: String,
    val questions: List<String>,
    val memo: String,
    val footer: String,
    val extraNotes: String = "",
) {
    fun plainText(): String = buildString {
        appendLine(title)
        appendLine("${UiCopy.APPOINTMENTS_META_DATE}: $date")
        appendLine("${UiCopy.APPOINTMENTS_META_TYPE}: $serviceType")
        appendLine("${UiCopy.APPOINTMENTS_META_LOCATION}: $location")
        appendLine()
        appendLine(UiCopy.APPOINTMENTS_REASON)
        appendLine(reason)
        appendLine()
        appendLine(UiCopy.APPOINTMENTS_SYMPTOMS_HEADER)
        appendLine(symptoms)
        appendLine()
        appendLine(UiCopy.APPOINTMENTS_SUGGESTED_QUESTIONS)
        questions.forEach { appendLine("• $it") }
        appendLine()
        appendLine(UiCopy.APPOINTMENTS_MEMO)
        appendLine(memo)
        if (extraNotes.isNotBlank()) {
            appendLine()
            appendLine(UiCopy.APPOINTMENTS_EXTRA_NOTES)
            appendLine(extraNotes)
        }
        appendLine()
        appendLine(footer)
    }.trim()
}

data class AppointmentsUiState(
    val isLoading: Boolean = true,
    val appointments: List<AppointmentSummary> = emptyList(),
    val selectedId: String? = null,
    val symptomNotes: String = "",
    val isListening: Boolean = false,
    val speechAvailable: Boolean = false,
    val pocketGuide: PocketGuide? = null,
    val errorMessage: String? = null,
)

class AppointmentsViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val speechToText: SpeechToText = FakeSpeechToText(),
    private val textToSpeech: TextToSpeech = FakeTextToSpeech(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    constructor(
        mockData: MockDataService,
        speechToText: SpeechToText = FakeSpeechToText(),
        textToSpeech: TextToSpeech = FakeTextToSpeech(),
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(LocalCarePlanRepository(mockData), speechToText, textToSpeech, scope)

    private val _state = MutableStateFlow(AppointmentsUiState())
    val state: StateFlow<AppointmentsUiState> = _state.asStateFlow()

    fun load() {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val available = speechToText.isAvailable()
                val summaries = carePlanRepository.fetchAppointments().map { it.toSummary() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        appointments = summaries,
                        speechAvailable = available,
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
    }

    fun select(id: String) {
        _state.update { it.copy(selectedId = id, pocketGuide = null) }
    }

    fun generatePocketGuide(id: String? = _state.value.selectedId) {
        val targetId = id ?: return
        val appt = _state.value.appointments.firstOrNull { it.id == targetId } ?: return
        val notes = _state.value.symptomNotes
        val previousNotes = _state.value.pocketGuide
            ?.takeIf { it.appointmentId == targetId }
            ?.extraNotes
            .orEmpty()
        val guide = PocketGuide(
            appointmentId = appt.id,
            title = UiCopy.APPOINTMENTS_POCKET_TITLE,
            date = appt.startIso,
            serviceType = appt.serviceType,
            location = appt.location,
            reason = appt.reason.ifBlank { "—" },
            symptoms = if (notes.isBlank()) UiCopy.APPOINTMENTS_SYMPTOMS_EMPTY else notes,
            questions = UiCopy.APPOINTMENTS_DEFAULT_QUESTIONS,
            memo = appt.comment.ifBlank { "—" },
            footer = UiCopy.APPOINTMENTS_POCKET_FOOTER,
            extraNotes = previousNotes,
        )
        _state.update { it.copy(selectedId = targetId, pocketGuide = guide) }
    }

    fun updateExtraNotes(text: String) {
        val guide = _state.value.pocketGuide ?: return
        _state.update { it.copy(pocketGuide = guide.copy(extraNotes = text)) }
    }

    fun toggleVoiceLogging() {
        scope.launch {
            val current = _state.value
            if (!current.speechAvailable) return@launch
            if (current.isListening) {
                speechToText.stop()
                _state.update { it.copy(isListening = false) }
                return@launch
            }
            _state.update { it.copy(isListening = true, errorMessage = null) }
            val result = speechToText.listen(
                localeTag = "nl-NL",
                onPartial = { partial ->
                    _state.update { state ->
                        state.copy(symptomNotes = mergeNotes(state.symptomNotes, partial, replaceLast = true))
                    }
                },
            )
            result.fold(
                onSuccess = { text ->
                    _state.update {
                        it.copy(
                            isListening = false,
                            symptomNotes = mergeNotes(it.symptomNotes, text, replaceLast = false),
                        )
                    }
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(
                            isListening = false,
                            errorMessage = err.message ?: UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE,
                        )
                    }
                },
            )
        }
    }

    fun clearSymptomNotes() {
        _state.update { it.copy(symptomNotes = "") }
    }

    fun readAloudPocketGuide() {
        val guide = _state.value.pocketGuide ?: return
        scope.launch {
            textToSpeech.speak(guide.plainText(), localeTag = "nl-NL")
        }
    }

    fun dispose() {
        speechToText.cancel()
        textToSpeech.stop()
        scope.cancel()
    }

    private fun mergeNotes(existing: String, incoming: String, replaceLast: Boolean): String {
        val trimmed = incoming.trim()
        if (trimmed.isEmpty()) return existing
        if (existing.isBlank()) return trimmed
        if (replaceLast) {
            val lines = existing.lines().toMutableList()
            if (lines.isNotEmpty()) {
                lines[lines.lastIndex] = trimmed
            } else {
                lines.add(trimmed)
            }
            return lines.joinToString("\n")
        }
        return "$existing\n$trimmed".trim()
    }
}

@Suppress("UNCHECKED_CAST")
fun FhirResource.toSummary(): AppointmentSummary {
    val serviceType = ((data["serviceType"] as? List<*>)?.firstOrNull() as? Map<*, *>)
        ?.get("text") as? String
        ?: UiCopy.NAV_APPOINTMENTS
    val reason = ((data["reasonCode"] as? List<*>)?.firstOrNull() as? Map<*, *>)
        ?.get("text") as? String
        ?: ""
    val location = (data["location"] as? Map<*, *>)?.get("display") as? String ?: ""
    val practitioner = ((data["participant"] as? List<*>)?.firstOrNull() as? Map<*, *>)
        ?.let { it["actor"] as? Map<*, *> }
        ?.get("display") as? String
        ?: ""
    val comment = data["comment"] as? String ?: ""
    val start = data["start"] as? String ?: ""
    return AppointmentSummary(
        id = id,
        serviceType = serviceType,
        startIso = start,
        location = location,
        practitioner = practitioner,
        reason = reason,
        comment = comment,
    )
}
