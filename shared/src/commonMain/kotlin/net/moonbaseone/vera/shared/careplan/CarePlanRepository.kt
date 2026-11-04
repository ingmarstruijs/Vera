package net.moonbaseone.vera.shared.careplan

import net.moonbaseone.vera.shared.fhir.FhirResource
import net.moonbaseone.vera.shared.fhir.MockDataService

/**
 * Local care-plan / appointment read surface used by ViewModels.
 *
 * Keeps UI logic off [MockDataService] / future FHIR transport so MedMij
 * can plug in behind [net.moonbaseone.vera.shared.fhir.FhirClient] later
 * without rewriting shell ViewModels.
 */
interface CarePlanRepository {
    suspend fun fetchCarePlan(): FhirResource
    suspend fun fetchAppointments(): List<FhirResource>
}

/** Local mock-backed store — production FHIR/MedMij is out of scope for P11. */
class LocalCarePlanRepository(
    private val mockData: MockDataService,
) : CarePlanRepository {
    override suspend fun fetchCarePlan(): FhirResource = mockData.fetchCarePlan()

    override suspend fun fetchAppointments(): List<FhirResource> = mockData.fetchAppointments()
}
