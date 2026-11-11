package net.moonbaseone.vera.shared.symptoms

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SymptomLogViewModelTest {
    @Test
    fun capture_addsNewestFirst() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Misselijkheid")
        val vm = SymptomLogViewModel(speechToText = speech)
        vm.load()
        delay(40)
        assertTrue(vm.state.value.speechAvailable)
        vm.toggleVoiceLogging()
        delay(50)
        assertEquals(1, vm.state.value.notes.size)
        assertEquals("Misselijkheid", vm.state.value.notes.first().text)
        vm.dispose()
    }

    @Test
    fun delete_and_reRecord() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Hoofdpijn")
        val vm = SymptomLogViewModel(speechToText = speech)
        vm.load()
        delay(40)
        vm.toggleVoiceLogging()
        delay(50)
        val id = vm.state.value.notes.first().id
        vm.toggleVoiceLogging(replaceNoteId = id)
        delay(50)
        assertEquals(1, vm.state.value.notes.size)
        assertEquals(id, vm.state.value.notes.first().id)
        vm.deleteNote(id)
        assertTrue(vm.state.value.notes.isEmpty())
        vm.dispose()
    }

    @Test
    fun readAloud_speaksNote() = runBlocking {
        val speech = FakeSpeechToText(transcript = "Pijn")
        val tts = FakeTextToSpeech()
        val vm = SymptomLogViewModel(speechToText = speech, textToSpeech = tts)
        vm.load()
        delay(40)
        vm.toggleVoiceLogging()
        delay(50)
        vm.readAloudNote(vm.state.value.notes.first().id)
        delay(50)
        assertEquals("Pijn", tts.lastSpoken)
        vm.dispose()
    }

    @Test
    fun speechUnavailable_flagged() = runBlocking {
        val vm = SymptomLogViewModel(speechToText = FakeSpeechToText(available = false))
        vm.load()
        delay(40)
        assertFalse(vm.state.value.speechAvailable)
        vm.dispose()
    }
}
