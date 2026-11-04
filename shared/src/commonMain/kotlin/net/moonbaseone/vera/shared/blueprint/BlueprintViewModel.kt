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
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.ui.UiCopy

sealed class BlueprintUiState {
    data object Loading : BlueprintUiState()
    data class Ready(
        val events: List<TimelineEvent>,
        val expandedId: String? = null,
        /** Term currently expanded inline (tap-to-explain); null = chips only. */
        val expandedJargonTerm: String? = null,
    ) : BlueprintUiState()
    data class Error(val message: String) : BlueprintUiState()
}

/**
 * Loads the IKNL-aligned CarePlan into timeline events for the Blueprint UI.
 * Reads via [CarePlanRepository] so FHIR/MedMij transport can swap later.
 */
class BlueprintViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    constructor(
        mockData: MockDataService,
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(LocalCarePlanRepository(mockData), scope)

    private val _state = MutableStateFlow<BlueprintUiState>(BlueprintUiState.Loading)
    val state: StateFlow<BlueprintUiState> = _state.asStateFlow()

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
                    expandedId = currentId,
                    expandedJargonTerm = null,
                )
            } catch (e: Exception) {
                _state.value = BlueprintUiState.Error(e.message ?: UiCopy.BLUEPRINT_NO_DATA)
            }
        }
    }

    fun toggleExpanded(eventId: String) {
        val current = _state.value as? BlueprintUiState.Ready ?: return
        _state.value = current.copy(
            expandedId = if (current.expandedId == eventId) null else eventId,
            expandedJargonTerm = null,
        )
    }

    /** Tap-to-explain a linked glossary term inside the open timeline item. */
    fun toggleJargon(term: String) {
        val current = _state.value as? BlueprintUiState.Ready ?: return
        _state.value = current.copy(
            expandedJargonTerm = if (current.expandedJargonTerm == term) null else term,
        )
    }

    fun jargonMatches(event: TimelineEvent): List<JargonEntry> =
        GlossaryLinker.findEntries(event.glossaryHaystack())

    fun dispose() {
        scope.cancel()
    }
}
