package net.moonbaseone.vera.shared.decoder

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.shield.IdentityShieldService
import net.moonbaseone.vera.shared.translation.FakeOnDeviceTranslator
import net.moonbaseone.vera.shared.translation.LocalTranslationService
import net.moonbaseone.vera.shared.ui.UiCopy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DecoderViewModelTest {
    private fun translation(
        translator: FakeOnDeviceTranslator = FakeOnDeviceTranslator(),
    ) = LocalTranslationService(
        engine = translator,
        shield = IdentityShieldService(),
    )

    private suspend fun awaitStep(vm: DecoderViewModel, predicate: (DecoderUiState) -> Boolean) {
        repeat(80) {
            if (predicate(vm.state.value)) return
            delay(25)
        }
        error("Timed out waiting for decoder state: ${vm.state.value}")
    }

    @Test
    fun setQuery_filtersGlossary() {
        val vm = DecoderViewModel(translation())
        vm.setQuery("HER2")
        assertTrue(
            vm.state.value.entries.all {
                it.term.contains("HER2", ignoreCase = true) ||
                    it.dutchExplanation.contains("HER2")
            },
        )
        assertTrue(vm.state.value.entries.isNotEmpty())
        vm.dispose()
    }

    @Test
    fun setLanguage_en_downloadsAndTranslates() = runBlocking {
        val translator = FakeOnDeviceTranslator(downloaded = mutableSetOf())
        val vm = DecoderViewModel(translation(translator))
        vm.setLanguage("en")
        awaitStep(vm) {
            !it.isTranslating &&
                it.translatedEntries.isNotEmpty() &&
                it.translationError == null
        }
        assertEquals("en", vm.state.value.selectedLanguage)
        assertTrue(translator.downloadCalls >= 1)
        assertTrue(translator.translateCalls > 0)
        val sample = vm.state.value.translatedEntries.values.first()
        assertTrue(sample.explanation.startsWith("[\$]en "))
        assertNull(vm.state.value.translationNotice)
        vm.dispose()
    }

    @Test
    fun setLanguage_unavailable_showsNotice() = runBlocking {
        val translator = FakeOnDeviceTranslator(available = false)
        val vm = DecoderViewModel(translation(translator))
        vm.setLanguage("en")
        awaitStep(vm) { it.translationNotice != null }
        assertEquals(UiCopy.DECODER_TRANSLATION_UNAVAILABLE, vm.state.value.translationNotice)
        assertTrue(vm.state.value.translatedEntries.isEmpty())
        assertEquals(0, translator.translateCalls)
        vm.dispose()
    }

    @Test
    fun setLanguage_backToNl_clearsTranslations() = runBlocking {
        val vm = DecoderViewModel(translation())
        vm.setLanguage("de")
        awaitStep(vm) { it.translatedEntries.isNotEmpty() }
        vm.setLanguage("nl")
        assertEquals("nl", vm.state.value.selectedLanguage)
        assertTrue(vm.state.value.translatedEntries.isEmpty())
        assertNull(vm.state.value.translationNotice)
        assertFalse(vm.state.value.isTranslating)
        vm.dispose()
    }

    @Test
    fun toggleExpanded_works() {
        val vm = DecoderViewModel(translation())
        val term = vm.state.value.entries.first().term
        vm.toggleExpanded(term)
        assertEquals(term, vm.state.value.expandedTerm)
        vm.toggleExpanded(term)
        assertNull(vm.state.value.expandedTerm)
        vm.dispose()
    }

    @Test
    fun translatedEntry_coversKnownTerm() = runBlocking {
        val vm = DecoderViewModel(translation())
        val term = vm.state.value.entries.first().term
        vm.setLanguage("fr")
        awaitStep(vm) { it.translatedEntries.containsKey(term) }
        assertNotNull(vm.state.value.translatedEntries[term])
        vm.dispose()
    }
}
