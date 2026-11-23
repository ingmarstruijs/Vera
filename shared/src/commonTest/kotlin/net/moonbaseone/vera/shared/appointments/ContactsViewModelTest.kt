package net.moonbaseone.vera.shared.appointments

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.fhir.MockDataService
import kotlin.test.Test
import kotlin.test.assertTrue

class ContactsViewModelTest {
    private suspend fun awaitLoaded(vm: ContactsViewModel) {
        repeat(40) {
            if (!vm.state.value.isLoading) return
            delay(25)
        }
        error("Timed out: ${vm.state.value}")
    }

    @Test
    fun load_fetchesCareContacts() = runBlocking {
        val vm = ContactsViewModel(MockDataService())
        vm.load()
        awaitLoaded(vm)
        assertTrue(vm.state.value.contacts.isNotEmpty())
        assertTrue(vm.state.value.contacts.any { it.phone.isNotBlank() })
        assertTrue(vm.state.value.contacts.any { it.id == "contact-gp" })
        vm.dispose()
    }
}
