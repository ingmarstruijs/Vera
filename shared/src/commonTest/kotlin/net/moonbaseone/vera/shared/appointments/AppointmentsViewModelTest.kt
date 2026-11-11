package net.moonbaseone.vera.shared.appointments

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
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
        assertTrue(vm.state.value.speechAvailable)
        vm.dispose()
    }

    @Test
    fun generatePocketGuide_isStructuredReadable() = runBlocking {
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
        assertEquals(id, guide.appointmentId)
        assertTrue(guide.symptoms.contains("Hoofdpijn") || guide.title.isNotBlank())
        val plain = guide.plainText()
        assertFalse(plain.contains("**"), "plain text must not include markdown bold")
        assertFalse(plain.contains("##"), "plain text must not include markdown headings")
        assertFalse(plain.startsWith("#"))
        assertTrue(plain.contains(guide.title))
        vm.dispose()
    }

    @Test
    fun symptomNotes_reRecordReplacesSameId() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Misselijkheid")
        val vm = AppointmentsViewModel(MockDataService(), speechToText = speech)
        vm.load()
        awaitLoaded(vm)
        vm.toggleVoiceLogging()
        delay(50)
        assertEquals(1, vm.state.value.symptomNotes.size)
        val noteId = vm.state.value.symptomNotes.first().id
        vm.toggleVoiceLogging(replaceNoteId = noteId)
        delay(50)
        assertEquals(1, vm.state.value.symptomNotes.size)
        assertEquals(noteId, vm.state.value.symptomNotes.first().id)
        assertEquals("Misselijkheid", vm.state.value.symptomNotes.first().text)
        vm.dispose()
    }

    @Test
    fun symptomNotes_deleteRemovesOne() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Hoofdpijn")
        val vm = AppointmentsViewModel(MockDataService(), speechToText = speech)
        vm.load()
        awaitLoaded(vm)
        vm.toggleVoiceLogging()
        delay(50)
        vm.toggleVoiceLogging()
        delay(50)
        assertTrue(vm.state.value.symptomNotes.size >= 1)
        val id = vm.state.value.symptomNotes.first().id
        vm.deleteSymptomNote(id)
        assertTrue(vm.state.value.symptomNotes.none { it.id == id })
        vm.dispose()
    }

    @Test
    fun readAloudSymptom_speaksNote() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Pijn")
        val tts = FakeTextToSpeech()
        val vm = AppointmentsViewModel(MockDataService(), speechToText = speech, textToSpeech = tts)
        vm.load()
        awaitLoaded(vm)
        vm.toggleVoiceLogging()
        delay(50)
        val id = vm.state.value.symptomNotes.first().id
        vm.readAloudSymptom(id)
        delay(50)
        assertEquals("Pijn", tts.lastSpoken)
        vm.dispose()
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

    @Test
    fun dateDisplay_formatsIso() {
        assertEquals(
            "22 apr 2025 · 09:00",
            DateDisplay.formatIso("2025-04-22T09:00:00+02:00"),
        )
    }
}
