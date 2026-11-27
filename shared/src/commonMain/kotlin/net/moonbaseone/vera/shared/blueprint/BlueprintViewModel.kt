package net.moonbaseone.vera.shared.blueprint

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import net.moonbaseone.vera.shared.i18n.StringCatalog
import net.moonbaseone.vera.shared.translation.DatasetContentLocalizer
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
 * Dutch source content is localized to [StringCatalog.activeLocale] when a localizer is present.
 */
class BlueprintViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val datasetStore: ActiveDatasetStore = ActiveDatasetStore(),
    private val contentLocalizer: DatasetContentLocalizer? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    constructor(
        mockData: MockDataService,
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(LocalCarePlanRepository(mockData), mockData.datasetStore, null, scope)

    private val _state = MutableStateFlow<BlueprintUiState>(BlueprintUiState.Loading)
    val state: StateFlow<BlueprintUiState> = _state.asStateFlow()

    val selectedDatasetId: StateFlow<MockDatasetId> = datasetStore.id
    val availableDatasets: List<MockDatasetInfo> = MockDatasets.all

    /** Untranslated Dutch source events (re-localized when app language changes). */
    private var sourceEvents: List<TimelineEvent> = emptyList()
    private var localizeJob: Job? = null

    init {
        scope.launch {
            datasetStore.id.collect { load() }
        }
        scope.launch {
            StringCatalog.activeLocaleFlow.collect {
                if (sourceEvents.isNotEmpty()) {
                    publishLocalized(preserveSelection = true)
                }
            }
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
                sourceEvents = events
                val currentId = events.firstOrNull {
                    it.phase == net.moonbaseone.vera.shared.domain.TimelinePhase.Current
                }?.id
                val selected = currentId ?: events.firstOrNull()?.id
                _state.value = BlueprintUiState.Ready(
                    events = events,
                    selectedId = selected,
                )
                publishLocalized(preserveSelection = true)
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
        localizeJob?.cancel()
        scope.cancel()
    }

    private fun publishLocalized(preserveSelection: Boolean) {
        val localizer = contentLocalizer ?: return
        val source = sourceEvents
        if (source.isEmpty()) return
        localizeJob?.cancel()
        localizeJob = scope.launch {
            val target = StringCatalog.activeLocale.tag
            val previous = _state.value as? BlueprintUiState.Ready
            val localized = localizer.localizeEvents(source, target)
            if (sourceEvents !== source) return@launch
            val selected = when {
                preserveSelection && previous?.selectedId != null &&
                    localized.any { it.id == previous.selectedId } -> previous.selectedId
                else -> localized.firstOrNull {
                    it.phase == net.moonbaseone.vera.shared.domain.TimelinePhase.Current
                }?.id ?: localized.firstOrNull()?.id
            }
            _state.value = BlueprintUiState.Ready(
                events = localized,
                selectedId = selected,
                showClinicalText = previous?.showClinicalText == true,
            )
        }
    }
}
