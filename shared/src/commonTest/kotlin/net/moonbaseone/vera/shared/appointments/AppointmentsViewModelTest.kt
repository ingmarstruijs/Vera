package net.moonbaseone.vera.shared.appointments

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun load_fetchesAppointments() = runBlocking {
        val vm = AppointmentsViewModel(MockDataService())
        vm.load()
        awaitLoaded(vm)
        assertTrue(vm.state.value.appointments.isNotEmpty())
        assertTrue(vm.state.value.speechAvailable)
        vm.dispose()
    }

    @Test
    fun generatePocketGuide_includesSymptoms() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Hoofdpijn")
        val vm = AppointmentsViewModel(MockDataService(), speechToText = speech)
        vm.load()
        awaitLoaded(vm)
        val id = vm.state.value.appointments.first().id
        vm.select(id)
        vm.toggleVoiceLogging()
        delay(50)
        vm.generatePocketGuide(id)
        val guide = vm.state.value.pocketGuide
        assertNotNull(guide)
        assertTrue(guide.contains("Hoofdpijn") || guide.contains("Pocket Gids"))
        vm.dispose()
    }

    @Test
    fun readAloud_speaksGuide() = runBlocking {
        val tts = FakeTextToSpeech()
        val vm = AppointmentsViewModel(MockDataService(), textToSpeech = tts)
        vm.load()
        awaitLoaded(vm)
        val id = vm.state.value.appointments.first().id
        vm.select(id)
        vm.generatePocketGuide(id)
        vm.readAloudPocketGuide()
        delay(50)
        assertEquals(vm.state.value.pocketGuide, tts.lastSpoken)
        vm.dispose()
    }

    @Test
    fun speechUnavailable_flagged() = runBlocking {
        val vm = AppointmentsViewModel(
            MockDataService(),
            speechToText = FakeSpeechToText(available = false),
        )
        vm.load()
        awaitLoaded(vm)
        assertFalse(vm.state.value.speechAvailable)
        vm.dispose()
    }
}
