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
import net.moonbaseone.vera.shared.util.DateDisplay
import kotlin.random.Random

data class AppointmentSummary(
    val id: String,
    val serviceType: String,
    val startIso: String,
    val startDisplay: String,
    val location: String,
    val practitioner: String,
    val phone: String,
    val reason: String,
    val comment: String,
)

data class SymptomNote(
    val id: String,
    val text: String,
)

data class CareContact(
    val id: String,
    val name: String,
    val role: String,
    val organization: String,
    val phone: String,
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
    val isSaved: Boolean = false,
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
    val contacts: List<CareContact> = emptyList(),
    val savedGuides: List<PocketGuide> = emptyList(),
    val selectedId: String? = null,
    val symptomNotes: List<SymptomNote> = emptyList(),
    val draftSymptomText: String = "",
    val reRecordNoteId: String? = null,
    val isListening: Boolean = false,
    val speechAvailable: Boolean = false,
    val pocketGuide: PocketGuide? = null,
    val guideDialogOpen: Boolean = false,
    val guideJustSaved: Boolean = false,
    val errorMessage: String? = null,
) {
    val symptomNotesText: String
        get() = symptomNotes.joinToString("\n") { it.text }

    fun savedGuideFor(appointmentId: String): PocketGuide? =
        savedGuides.firstOrNull { it.appointmentId == appointmentId }
}

class AppointmentsViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val speechToText: SpeechToText = FakeSpeechToText(),
    private val textToSpeech: TextToSpeech = FakeTextToSpeech(),
    private val pocketGuideStore: PocketGuideStore = InMemoryPocketGuideStore(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    constructor(
        mockData: MockDataService,
        speechToText: SpeechToText = FakeSpeechToText(),
        textToSpeech: TextToSpeech = FakeTextToSpeech(),
        pocketGuideStore: PocketGuideStore = InMemoryPocketGuideStore(),
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(LocalCarePlanRepository(mockData), speechToText, textToSpeech, pocketGuideStore, scope)

    private val _state = MutableStateFlow(AppointmentsUiState())
    val state: StateFlow<AppointmentsUiState> = _state.asStateFlow()

    fun load() {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val available = speechToText.isAvailable()
                val summaries = carePlanRepository.fetchAppointments().map { it.toSummary() }
                val saved = pocketGuideStore.list().map { it.copy(isSaved = true) }
                _state.update {
                    it.copy(
                        isLoading = false,
                        appointments = summaries,
                        contacts = buildContacts(summaries),
                        savedGuides = saved,
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
        _state.update {
            it.copy(
                selectedId = id,
                pocketGuide = null,
                guideDialogOpen = false,
                guideJustSaved = false,
            )
        }
    }

    fun generatePocketGuide(id: String? = _state.value.selectedId) {
        val targetId = id ?: return
        val appt = _state.value.appointments.firstOrNull { it.id == targetId } ?: return
        val notes = _state.value.symptomNotesText
        val previousNotes = pocketGuideStore.get(targetId)?.extraNotes
            ?: _state.value.pocketGuide?.takeIf { it.appointmentId == targetId }?.extraNotes
            ?: ""
        val guide = PocketGuide(
            appointmentId = appt.id,
            title = UiCopy.APPOINTMENTS_POCKET_TITLE,
            date = appt.startDisplay.ifBlank { appt.startIso },
            serviceType = appt.serviceType,
            location = appt.location,
            reason = appt.reason.ifBlank { "—" },
            symptoms = if (notes.isBlank()) UiCopy.APPOINTMENTS_SYMPTOMS_EMPTY else notes,
            questions = UiCopy.APPOINTMENTS_DEFAULT_QUESTIONS,
            memo = appt.comment.ifBlank { "—" },
            footer = UiCopy.APPOINTMENTS_POCKET_FOOTER,
            extraNotes = previousNotes,
            isSaved = false,
        )
        _state.update {
            it.copy(
                selectedId = targetId,
                pocketGuide = guide,
                guideDialogOpen = true,
                guideJustSaved = false,
            )
        }
    }

    fun openSavedGuide(appointmentId: String) {
        val guide = pocketGuideStore.get(appointmentId)?.copy(isSaved = true) ?: return
        _state.update {
            it.copy(
                selectedId = appointmentId,
                pocketGuide = guide,
                guideDialogOpen = true,
                guideJustSaved = false,
            )
        }
    }

    fun dismissGuideDialog() {
        _state.update { it.copy(guideDialogOpen = false, guideJustSaved = false) }
    }

    fun savePocketGuide() {
        val guide = _state.value.pocketGuide ?: return
        val saved = guide.copy(isSaved = true)
        pocketGuideStore.save(saved)
        _state.update {
            it.copy(
                pocketGuide = saved,
                savedGuides = pocketGuideStore.list().map { g -> g.copy(isSaved = true) },
                guideJustSaved = true,
                guideDialogOpen = true,
            )
        }
    }

    fun updateExtraNotes(text: String) {
        val guide = _state.value.pocketGuide ?: return
        _state.update {
            it.copy(
                pocketGuide = guide.copy(extraNotes = text, isSaved = false),
                guideJustSaved = false,
            )
        }
    }

    fun toggleVoiceLogging(replaceNoteId: String? = null) {
        scope.launch {
            val current = _state.value
            if (!current.speechAvailable) return@launch
            if (current.isListening) {
                speechToText.stop()
                _state.update {
                    it.copy(isListening = false, draftSymptomText = "", reRecordNoteId = null)
                }
                return@launch
            }
            _state.update {
                it.copy(
                    isListening = true,
                    errorMessage = null,
                    reRecordNoteId = replaceNoteId,
                    draftSymptomText = "",
                )
            }
            val result = speechToText.listen(
                localeTag = "nl-NL",
                onPartial = { partial ->
                    _state.update { state -> state.copy(draftSymptomText = partial.trim()) }
                },
            )
            result.fold(
                onSuccess = { text ->
                    val trimmed = text.trim()
                    _state.update { state ->
                        val notes = if (trimmed.isEmpty()) {
                            state.symptomNotes
                        } else {
                            applyUtterance(state.symptomNotes, state.reRecordNoteId, trimmed)
                        }
                        state.copy(
                            isListening = false,
                            draftSymptomText = "",
                            reRecordNoteId = null,
                            symptomNotes = notes,
                        )
                    }
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(
                            isListening = false,
                            draftSymptomText = "",
                            reRecordNoteId = null,
                            errorMessage = err.message ?: UiCopy.APPOINTMENTS_SPEECH_UNAVAILABLE,
                        )
                    }
                },
            )
        }
    }

    fun deleteSymptomNote(id: String) {
        _state.update { state ->
            state.copy(symptomNotes = state.symptomNotes.filterNot { it.id == id })
        }
    }

    fun clearSymptomNotes() {
        _state.update { it.copy(symptomNotes = emptyList(), draftSymptomText = "") }
    }

    fun readAloudSymptom(id: String) {
        val note = _state.value.symptomNotes.firstOrNull { it.id == id } ?: return
        scope.launch {
            textToSpeech.speak(note.text, localeTag = "nl-NL")
        }
    }

    fun readAloudSymptoms() {
        val text = _state.value.symptomNotesText
        if (text.isBlank()) return
        scope.launch {
            textToSpeech.speak(text, localeTag = "nl-NL")
        }
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
        return notes + SymptomNote(id = newNoteId(), text = text)
    }

    private fun newNoteId(): String = "sym-${Random.nextLong().toString(16)}"

    private fun buildContacts(appointments: List<AppointmentSummary>): List<CareContact> {
        val fromAppointments = appointments
            .filter { it.practitioner.isNotBlank() || it.phone.isNotBlank() }
            .map {
                CareContact(
                    id = "appt-contact-${it.id}",
                    name = it.practitioner.ifBlank { it.serviceType },
                    role = it.serviceType,
                    organization = it.location,
                    phone = it.phone,
                )
            }
        val gp = CareContact(
            id = "contact-gp",
            name = "Huisartsenpraktijk Utrecht Noord",
            role = "Huisarts",
            organization = "Eerste lijn",
            phone = "030 123 4567",
        )
        val oncologyDesk = CareContact(
            id = "contact-oncology-desk",
            name = "Polikliniek Oncologie",
            role = "Afdelingstelefoon",
            organization = "UMC Utrecht – Afdeling Oncologie",
            phone = "088 755 6300",
        )
        return (listOf(gp, oncologyDesk) + fromAppointments)
            .distinctBy { "${it.name}|${it.phone}" }
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
    val locationPhone = firstTelecomPhone(data["location"] as? Map<*, *>)
    val practitionerActor = ((data["participant"] as? List<*>)?.firstOrNull() as? Map<*, *>)
        ?.let { it["actor"] as? Map<*, *> }
    val practitioner = practitionerActor?.get("display") as? String ?: ""
    val practitionerPhone = firstTelecomPhone(practitionerActor)
    val comment = data["comment"] as? String ?: ""
    val start = data["start"] as? String ?: ""
    return AppointmentSummary(
        id = id,
        serviceType = serviceType,
        startIso = start,
        startDisplay = DateDisplay.formatIso(start),
        location = location,
        practitioner = practitioner,
        phone = practitionerPhone.ifBlank { locationPhone },
        reason = reason,
        comment = comment,
    )
}

@Suppress("UNCHECKED_CAST")
private fun firstTelecomPhone(map: Map<*, *>?): String {
    if (map == null) return ""
    val telecom = map["telecom"] as? List<*> ?: return ""
    for (entry in telecom) {
        val item = entry as? Map<*, *> ?: continue
        val system = item["system"] as? String
        val value = item["value"] as? String
        if (system == "phone" && !value.isNullOrBlank()) return value
    }
    return ""
}
