package net.moonbaseone.vera.shared.appointments

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.pocket.InMemoryPocketGuideStore
import net.moonbaseone.vera.shared.util.DateDisplay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppointmentsViewModelTest {
    private suspend fun awaitLoaded(vm: AppointmentsViewModel) {
        repeat(40) {
            if (!vm.state.value.isLoading) return
            delay(25)
        }
        error("Timed out: ${vm.state.value}")
    }

    @Test
    fun load_fetchesAppointmentsAsNextAndUpcoming() = runBlocking {
        val vm = AppointmentsViewModel(MockDataService())
        awaitLoaded(vm)
        assertNotNull(vm.state.value.next)
        assertTrue(vm.state.value.upcoming.isNotEmpty() || vm.state.value.next != null)
        vm.dispose()
    }

    @Test
    fun ensureGuide_marksStatusReady() = runBlocking {
        val store = InMemoryPocketGuideStore()
        val mock = MockDataService()
        val vm = AppointmentsViewModel(mock, pocketGuideStore = store)
        awaitLoaded(vm)
        val id = vm.state.value.next!!.appointment.id
        assertEquals(PocketGuideStatus.None, vm.state.value.next!!.guideStatus)
        vm.ensureGuide(id)
        delay(50)
        assertEquals(PocketGuideStatus.Ready, vm.state.value.next!!.guideStatus)
        vm.dispose()
    }

    @Test
    fun dateDisplay_formatsIso() {
        assertEquals(
            "22 apr 2025 · 09:00",
            DateDisplay.formatIso("2025-04-22T09:00:00+02:00"),
        )
    }
}
