package net.moonbaseone.vera.shared.blueprint

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.domain.TimelinePhase
import net.moonbaseone.vera.shared.fhir.MockDataService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BlueprintViewModelTest {
    private suspend fun awaitReady(vm: BlueprintViewModel): BlueprintUiState.Ready {
        repeat(40) {
            val state = vm.state.value
            if (state is BlueprintUiState.Ready) return state
            if (state is BlueprintUiState.Error) error(state.message)
            delay(25)
        }
        error("Timed out: ${vm.state.value}")
    }

    @Test
    fun load_emitsCarePlanTimeline() = runBlocking {
        val vm = BlueprintViewModel(MockDataService())
        vm.load()
        val ready = awaitReady(vm)
        assertTrue(ready.events.isNotEmpty())
        assertTrue(ready.events.any { it.phase == TimelinePhase.Current })
        assertNotNull(ready.expandedId)
        vm.dispose()
    }

    @Test
    fun toggleExpanded_collapsesCurrent() = runBlocking {
        val vm = BlueprintViewModel(MockDataService())
        vm.load()
        val ready = awaitReady(vm)
        val id = ready.expandedId!!
        vm.toggleExpanded(id)
        assertEquals(null, (vm.state.value as BlueprintUiState.Ready).expandedId)
        vm.toggleExpanded(id)
        assertEquals(id, (vm.state.value as BlueprintUiState.Ready).expandedId)
        vm.dispose()
    }

    @Test
    fun load_emitsRichCarePlanFieldsAndInlineJargon() = runBlocking {
        val vm = BlueprintViewModel(MockDataService())
        vm.load()
        val ready = awaitReady(vm)
        assertTrue(ready.events.size >= 5)
        assertTrue(ready.events.all { it.description.length > 80 })
        assertTrue(ready.events.all { !it.patientInstruction.isNullOrBlank() })
        assertTrue(ready.events.all { !it.nextSteps.isNullOrBlank() })
        assertTrue(ready.events.all { it.actionItems.isNotEmpty() })

        val withJargon = ready.events.count { vm.jargonMatches(it).isNotEmpty() }
        assertTrue(withJargon >= 4, "expected most timeline items to link glossary terms")

        val firstTerm = vm.jargonMatches(ready.events.first()).first().term
        vm.toggleJargon(firstTerm)
        assertEquals(firstTerm, (vm.state.value as BlueprintUiState.Ready).expandedJargonTerm)
        vm.toggleJargon(firstTerm)
        assertEquals(null, (vm.state.value as BlueprintUiState.Ready).expandedJargonTerm)
        vm.dispose()
    }
}
