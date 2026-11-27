package net.moonbaseone.vera.shared.blueprint

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.careplan.LocalCarePlanRepository
import net.moonbaseone.vera.shared.domain.GlossaryLinker
import net.moonbaseone.vera.shared.domain.JargonEntry
import net.moonbaseone.vera.shared.domain.TimelineEvent
import net.moonbaseone.vera.shared.fhir.ActiveDatasetStore
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.fhir.MockDatasetId
import net.moonbaseone.vera.shared.fhir.MockDatasetInfo
import net.moonbaseone.vera.shared.fhir.MockDatasets
import net.moonbaseone.vera.shared.ui.UiCopy

sealed class BlueprintUiState {
    data object Loading : BlueprintUiState()
    data class Ready(
        val events: List<TimelineEvent>,
        /** Currently visible care-step (timeline + pager stay in sync). */
        val selectedId: String? = null,
        /** false = plain-language patient view (default); true = original clinical text. */
        val showClinicalText: Boolean = false,
    ) : BlueprintUiState()
    data class Error(val message: String) : BlueprintUiState()
}

/**
 * Loads the IKNL-aligned CarePlan into timeline events for the Blueprint UI.
 * Reads via [CarePlanRepository] so FHIR/MedMij transport can swap later.
 */
class BlueprintViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val datasetStore: ActiveDatasetStore = ActiveDatasetStore(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    constructor(
        mockData: MockDataService,
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(LocalCarePlanRepository(mockData), mockData.datasetStore, scope)

    private val _state = MutableStateFlow<BlueprintUiState>(BlueprintUiState.Loading)
    val state: StateFlow<BlueprintUiState> = _state.asStateFlow()

    val selectedDatasetId: StateFlow<MockDatasetId> = datasetStore.id
    val availableDatasets: List<MockDatasetInfo> = MockDatasets.all

    init {
        scope.launch {
            datasetStore.id.collect { load() }
        }
    }

    fun selectDataset(id: MockDatasetId) {
        datasetStore.select(id)
    }

    fun load() {
        scope.launch {
            _state.value = BlueprintUiState.Loading
            try {
                val carePlan = carePlanRepository.fetchCarePlan()

                @Suppress("UNCHECKED_CAST")
                val activities = (carePlan.data["activity"] as? List<*>)
                    ?.mapNotNull { it as? Map<String, Any?> }
                    .orEmpty()
                val events = TimelineEvent.fromCarePlanActivities(activities)
                val currentId = events.firstOrNull {
                    it.phase == net.moonbaseone.vera.shared.domain.TimelinePhase.Current
                }?.id
                _state.value = BlueprintUiState.Ready(
                    events = events,
                    selectedId = currentId ?: events.firstOrNull()?.id,
                )
            } catch (e: Exception) {
                _state.value = BlueprintUiState.Error(e.message ?: UiCopy.BLUEPRINT_NO_DATA)
            }
        }
    }

    /** Select a care-step via the timeline navigator (or pager sync). */
    fun selectEvent(eventId: String) {
        val current = _state.value as? BlueprintUiState.Ready ?: return
        if (current.selectedId == eventId) return
        if (current.events.none { it.id == eventId }) return
        _state.value = current.copy(selectedId = eventId)
    }

    fun setShowClinicalText(showClinical: Boolean) {
        val current = _state.value as? BlueprintUiState.Ready ?: return
        _state.value = current.copy(showClinicalText = showClinical)
    }

    fun jargonMatches(event: TimelineEvent): List<JargonEntry> =
        GlossaryLinker.findEntries(event.glossaryHaystack())

    fun dispose() {
        scope.cancel()
    }
}
