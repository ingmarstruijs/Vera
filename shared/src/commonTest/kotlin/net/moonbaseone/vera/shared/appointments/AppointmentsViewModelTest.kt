package net.moonbaseone.vera.shared.appointments

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.symptoms.SymptomLogViewModel
import net.moonbaseone.vera.shared.util.DateDisplay
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
    fun load_fetchesAppointmentsAndContacts() = runBlocking {
        val vm = AppointmentsViewModel(MockDataService())
        vm.load()
        awaitLoaded(vm)
        assertTrue(vm.state.value.appointments.isNotEmpty())
        assertTrue(vm.state.value.contacts.isNotEmpty())
        assertTrue(vm.state.value.appointments.any { it.phone.isNotBlank() })
        assertTrue(vm.state.value.appointments.first().startDisplay.contains("apr"))
        vm.dispose()
    }

    @Test
    fun generatePocketGuide_includesSymptomLog() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Hoofdpijn")
        val symptoms = SymptomLogViewModel(speechToText = speech)
        symptoms.load()
        delay(50)
        symptoms.toggleVoiceLogging()
        delay(50)
        val vm = AppointmentsViewModel(MockDataService(), symptomLog = symptoms)
        vm.load()
        awaitLoaded(vm)
        val id = vm.state.value.appointments.first().id
        vm.generatePocketGuide(id)
        val guide = vm.state.value.pocketGuide
        assertNotNull(guide)
        assertTrue(guide.symptoms.contains("Hoofdpijn"))
        assertFalse(guide.plainText().contains("**"))
        assertFalse(guide.plainText().contains("##"))
        vm.dispose()
        symptoms.dispose()
    }

    @Test
    fun updateExtraNotes_persistsOnGuide() = runBlocking {
        val vm = AppointmentsViewModel(MockDataService())
        vm.load()
        awaitLoaded(vm)
        val id = vm.state.value.appointments.first().id
        vm.generatePocketGuide(id)
        vm.updateExtraNotes("Vragen over vermoeidheid")
        assertEquals("Vragen over vermoeidheid", vm.state.value.pocketGuide?.extraNotes)
        assertTrue(vm.state.value.pocketGuide!!.plainText().contains("Vragen over vermoeidheid"))
        vm.dispose()
    }

    @Test
    fun generatePocketGuide_opensDialog() = runBlocking {
        val vm = AppointmentsViewModel(MockDataService())
        vm.load()
        awaitLoaded(vm)
        val id = vm.state.value.appointments.first().id
        vm.generatePocketGuide(id)
        assertTrue(vm.state.value.guideDialogOpen)
        assertNotNull(vm.state.value.pocketGuide)
        vm.dismissGuideDialog()
        assertFalse(vm.state.value.guideDialogOpen)
        vm.dispose()
    }

    @Test
    fun savePocketGuide_canReopenLater() = runBlocking {
        val store = InMemoryPocketGuideStore()
        val vm = AppointmentsViewModel(MockDataService(), pocketGuideStore = store)
        vm.load()
        awaitLoaded(vm)
        val id = vm.state.value.appointments.first().id
        vm.generatePocketGuide(id)
        vm.updateExtraNotes("Meenemen: vragenlijst")
        vm.savePocketGuide()
        assertTrue(vm.state.value.pocketGuide?.isSaved == true)
        assertEquals(1, vm.state.value.savedGuides.size)
        vm.dismissGuideDialog()
        vm.select(vm.state.value.appointments.last().id)
        vm.openSavedGuide(id)
        assertTrue(vm.state.value.guideDialogOpen)
        assertEquals("Meenemen: vragenlijst", vm.state.value.pocketGuide?.extraNotes)
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
        assertEquals(vm.state.value.pocketGuide?.plainText(), tts.lastSpoken)
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
