package net.moonbaseone.vera.shared.symptoms

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SymptomsViewModelTest {
    @Test
    fun listening_addsEntry() = runBlocking {
        val store = InMemorySymptomStore()
        val vm = SymptomsViewModel(
            symptomStore = store,
            speechToText = FakeSpeechToText(transcript = "Misselijk sinds gisteren"),
            textToSpeech = FakeTextToSpeech(),
        )
        delay(30)
        vm.startListening()
        delay(80)
        assertTrue(vm.state.value.entries.any { it.text.contains("Misselijk") })
        vm.dispose()
    }

    @Test
    fun delete_removesEntry() = runBlocking {
        val store = InMemorySymptomStore()
        val entry = store.add("Koorts")
        val vm = SymptomsViewModel(store)
        delay(30)
        assertEquals(1, vm.state.value.entries.size)
        vm.delete(entry.id)
        delay(30)
        assertTrue(vm.state.value.entries.isEmpty())
        vm.dispose()
    }
}
