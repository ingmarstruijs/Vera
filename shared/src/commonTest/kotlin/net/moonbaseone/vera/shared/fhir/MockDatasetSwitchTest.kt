package net.moonbaseone.vera.shared.fhir

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockDatasetSwitchTest {
    @Test
    fun switchingDataset_changesCarePlanAndAppointments() = runBlocking {
        val store = ActiveDatasetStore(MockDatasetId.BreastHer2IIa)
        val service = MockDataService(datasetStore = store, ioDelayMs = 0)

        val breast = service.fetchCarePlan()
        assertEquals("careplan-001", breast.id)
        assertEquals(3, service.fetchAppointments().size)

        store.select(MockDatasetId.ColonStageIii)
        val colon = service.fetchCarePlan()
        assertEquals("careplan-colon-001", colon.id)
        val colonAppts = service.fetchAppointments()
        assertEquals(3, colonAppts.size)
        assertTrue(colonAppts.all { it.id.startsWith("appt-colon-") })

        store.select(MockDatasetId.LungEarlyStart)
        val lung = service.fetchCarePlan()
        assertEquals("careplan-lung-001", lung.id)
        assertTrue(service.fetchAppointments().all { it.id.startsWith("appt-lung-") })
    }

    @Test
    fun blueprintViewModel_reloadsOnDatasetChange() = runBlocking {
        val store = ActiveDatasetStore()
        val service = MockDataService(datasetStore = store, ioDelayMs = 0)
        val vm = net.moonbaseone.vera.shared.blueprint.BlueprintViewModel(
            carePlanRepository = net.moonbaseone.vera.shared.careplan.LocalCarePlanRepository(service),
            datasetStore = store,
        )
        // Allow init collect to load default
        kotlinx.coroutines.delay(50)
        val readyBreast = vm.state.value as net.moonbaseone.vera.shared.blueprint.BlueprintUiState.Ready
        assertTrue(readyBreast.events.isNotEmpty())
        val breastTitles = readyBreast.events.map { it.title }

        vm.selectDataset(MockDatasetId.ColonStageIii)
        kotlinx.coroutines.delay(50)
        val readyColon = vm.state.value as net.moonbaseone.vera.shared.blueprint.BlueprintUiState.Ready
        assertTrue(readyColon.events.isNotEmpty())
        assertTrue(readyColon.events.any { it.title.contains("CAPOX", ignoreCase = true) })
        assertTrue(readyColon.events.map { it.title } != breastTitles)

        vm.dispose()
    }
}
