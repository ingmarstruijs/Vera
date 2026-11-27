package net.moonbaseone.vera.shared.appointments

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
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.careplan.LocalCarePlanRepository
import net.moonbaseone.vera.shared.fhir.ActiveDatasetStore
import net.moonbaseone.vera.shared.fhir.FhirResource
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.i18n.StringCatalog
import net.moonbaseone.vera.shared.pocket.AppointmentAudioPlayer
import net.moonbaseone.vera.shared.pocket.FakeAppointmentAudioPlayer
import net.moonbaseone.vera.shared.pocket.PocketGuideStore
import net.moonbaseone.vera.shared.translation.DatasetContentLocalizer
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.util.DateDisplay

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

data class CareContact(
    val id: String,
    val name: String,
    val role: String,
    val organization: String,
    val phone: String,
)

enum class PocketGuideStatus {
    None,
    Ready,
    RecordingPresent,
}

data class AppointmentListItem(
    val appointment: AppointmentSummary,
    val guideStatus: PocketGuideStatus,
)

data class AppointmentsUiState(
    val isLoading: Boolean = true,
    val next: AppointmentListItem? = null,
    val upcoming: List<AppointmentListItem> = emptyList(),
    val past: List<AppointmentListItem> = emptyList(),
    val pastExpanded: Boolean = false,
    val playingAppointmentId: String? = null,
    val errorMessage: String? = null,
)

class AppointmentsViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val datasetStore: ActiveDatasetStore = ActiveDatasetStore(),
    private val pocketGuideStore: PocketGuideStore? = null,
    private val audioPlayer: AppointmentAudioPlayer = FakeAppointmentAudioPlayer(),
    private val contentLocalizer: DatasetContentLocalizer? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    constructor(
        mockData: MockDataService,
        pocketGuideStore: PocketGuideStore? = null,
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(
        LocalCarePlanRepository(mockData),
        mockData.datasetStore,
        pocketGuideStore,
        FakeAppointmentAudioPlayer(),
        null,
        scope,
    )

    private val _state = MutableStateFlow(AppointmentsUiState())
    val state: StateFlow<AppointmentsUiState> = _state.asStateFlow()

    private var sourceSummaries: List<AppointmentSummary> = emptyList()
    private var localizeJob: Job? = null

    init {
        scope.launch {
            datasetStore.id.collect { load() }
        }
        scope.launch {
            pocketGuideStore?.guides?.collect { refreshGuideStatuses() }
        }
        scope.launch {
            StringCatalog.activeLocaleFlow.collect {
                if (sourceSummaries.isNotEmpty()) publishLocalized()
            }
        }
    }

    fun load() {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val summaries = carePlanRepository.fetchAppointments()
                    .map { it.toSummary() }
                    .sortedBy { it.startIso }
                sourceSummaries = summaries
                publishItems(summaries)
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
    }

    private fun publishItems(summaries: List<AppointmentSummary>) {
        val items = summaries.map { appt ->
            AppointmentListItem(appt, statusFor(appt.id))
        }
        _state.update {
            it.copy(
                isLoading = false,
                next = items.firstOrNull(),
                upcoming = items.drop(1),
                past = emptyList(),
            )
        }
    }

    private fun publishLocalized() {
        val localizer = contentLocalizer ?: return
        val source = sourceSummaries
        if (source.isEmpty()) return
        localizeJob?.cancel()
        localizeJob = scope.launch {
            val target = StringCatalog.activeLocale.tag
            val localized = localizer.localizeAppointments(source, target)
            if (sourceSummaries !== source) return@launch
            publishItems(localized)
        }
    }

    fun ensureGuide(appointmentId: String) {
        pocketGuideStore?.ensure(appointmentId)
        refreshGuideStatuses()
    }

    fun playRecording(appointmentId: String) {
        val path = pocketGuideStore?.get(appointmentId)?.recordingPath ?: return
        scope.launch {
            if (_state.value.playingAppointmentId != null) {
                audioPlayer.stop()
            }
            _state.update { it.copy(playingAppointmentId = appointmentId, errorMessage = null) }
            try {
                audioPlayer.play(path)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            } finally {
                _state.update { state ->
                    if (state.playingAppointmentId == appointmentId) {
                        state.copy(playingAppointmentId = null)
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
        _state.update { it.copy(playingAppointmentId = null) }
    }

    fun togglePastExpanded() {
        _state.update { it.copy(pastExpanded = !it.pastExpanded) }
    }

    fun dispose() {
        audioPlayer.stop()
        scope.cancel()
    }

    private fun statusFor(appointmentId: String): PocketGuideStatus {
        val store = pocketGuideStore ?: return PocketGuideStatus.None
        return when {
            store.hasRecording(appointmentId) -> PocketGuideStatus.RecordingPresent
            store.hasGuide(appointmentId) -> PocketGuideStatus.Ready
            else -> PocketGuideStatus.None
        }
    }

    private fun refreshGuideStatuses() {
        _state.update { state ->
            fun mapItem(item: AppointmentListItem) =
                item.copy(guideStatus = statusFor(item.appointment.id))
            state.copy(
                next = state.next?.let(::mapItem),
                upcoming = state.upcoming.map(::mapItem),
                past = state.past.map(::mapItem),
            )
        }
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
