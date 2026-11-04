package net.moonbaseone.vera.shared.decoder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DecoderViewModelTest {
    @Test
    fun setQuery_filtersGlossary() {
        val vm = DecoderViewModel()
        vm.setQuery("HER2")
        assertTrue(vm.state.value.entries.all { it.term.contains("HER2", ignoreCase = true) || it.dutchExplanation.contains("HER2") })
        assertTrue(vm.state.value.entries.isNotEmpty())
    }

    @Test
    fun setLanguage_nonNl_setsPendingNotice() {
        val vm = DecoderViewModel()
        vm.setLanguage("en")
        assertEquals("en", vm.state.value.selectedLanguage)
        assertNotNull(vm.state.value.translationNotice)
        vm.setLanguage("nl")
        assertNull(vm.state.value.translationNotice)
    }

    @Test
    fun toggleExpanded_works() {
        val vm = DecoderViewModel()
        val term = vm.state.value.entries.first().term
        vm.toggleExpanded(term)
        assertEquals(term, vm.state.value.expandedTerm)
        vm.toggleExpanded(term)
        assertNull(vm.state.value.expandedTerm)
    }
}
