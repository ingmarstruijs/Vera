package net.moonbaseone.vera.shared.careplan

import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.fhir.UnsupportedFhirClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CarePlanRepositoryTest {
    @Test
    fun localRepository_returnsCarePlanAndAppointments() = runBlocking {
        val repo = LocalCarePlanRepository(MockDataService())
        val carePlan = repo.fetchCarePlan()
        assertEquals("CarePlan", carePlan.resourceType)
        assertTrue(repo.fetchAppointments().isNotEmpty())
    }

    @Test
    fun unsupportedFhirClient_failsClosed() {
        runBlocking {
            val result = UnsupportedFhirClient().fetchOncologyBundle()
            assertNotNull(result.failure)
        }
    }
}
