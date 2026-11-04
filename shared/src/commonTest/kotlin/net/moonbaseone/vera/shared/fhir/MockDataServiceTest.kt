package net.moonbaseone.vera.shared.fhir

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MockDataServiceTest {
    private val service = MockDataService(ioDelayMs = 0)

    private fun bundle(): FhirBundle = runBlocking {
        val result = service.fetchOncologyBundle()
        assertNull(result.failure)
        assertNotNull(result.bundle)
        result.bundle!!
    }

    @Test
    fun fetchOncologyBundle_succeedsWithTimestamp() {
        val b = bundle()
        assertTrue(b.timestamp.contains("T"))
        assertTrue(b.resources.isNotEmpty())
    }

    @Test
    fun bundle_containsExpectedResourceTypes() {
        val types = bundle().resources.map { it.resourceType }.toSet()
        assertTrue(
            types.containsAll(
                listOf("Patient", "Condition", "Observation", "CarePlan", "Appointment"),
            ),
        )
    }

    @Test
    fun patient_hasIdBirthDateAndDutchCity() {
        val patient = bundle().resources.first { it.resourceType == "Patient" }
        assertTrue(patient.id.isNotEmpty())
        assertNotNull(patient.data["birthDate"])
        @Suppress("UNCHECKED_CAST")
        val addresses = patient.data["address"] as List<Map<String, Any?>>
        assertEquals("Utrecht", addresses[0]["city"])
    }

    @Test
    fun condition_hasIcd10C504AndStaging() {
        val condition = bundle().resources.first { it.resourceType == "Condition" }

        @Suppress("UNCHECKED_CAST")
        val coding = ((condition.data["code"] as Map<*, *>)["coding"] as List<*>)[0] as Map<*, *>
        assertEquals("C50.4", coding["code"])
        assertTrue(coding["system"].toString().contains("icd-10"))
        assertNotNull(condition.data["stage"])
        assertNotNull(condition.data["subject"])
        val display = coding["display"].toString().lowercase()
        assertTrue(
            listOf("invasief", "ductaal", "carcinoom", "mamma", "neoplasma")
                .any { display.contains(it) },
        )
    }

    @Test
    fun observations_haveLoincAndKi67Value() {
        val obs = bundle().resources.filter { it.resourceType == "Observation" }
        assertTrue(obs.isNotEmpty())

        @Suppress("UNCHECKED_CAST")
        val firstCoding =
            ((obs.first().data["code"] as Map<*, *>)["coding"] as List<*>)[0] as Map<*, *>
        assertTrue(firstCoding["system"].toString().contains("loinc"))

        val ki67 = obs.first { r ->

            @Suppress("UNCHECKED_CAST")
            val coding = ((r.data["code"] as Map<*, *>)["coding"] as List<*>).first() as Map<*, *>
            coding["display"].toString().contains("Ki-67")
        }

        @Suppress("UNCHECKED_CAST")
        val value = ki67.data["valueQuantity"] as Map<*, *>
        assertEquals(38, value["value"])
    }

    @Test
    fun procedures_and_medications_present() {
        val b = bundle()
        val procs = b.resources.filter { it.resourceType == "Procedure" }
        assertTrue(procs.size >= 3)
        procs.forEach { assertTrue((it.data["status"] as String).isNotEmpty()) }

        val meds = b.resources.filter { it.resourceType == "MedicationRequest" }
        assertTrue(meds.size >= 3)
        assertEquals("order", meds.first().data["intent"])
        assertNotNull(meds.first().data["dosageInstruction"])
    }

    @Test
    fun carePlan_activeWithFiveActivitiesAndIknl() = runBlocking {
        val fromBundle = bundle().resources.first { it.resourceType == "CarePlan" }
        assertEquals("active", fromBundle.data["status"])
        @Suppress("UNCHECKED_CAST")
        assertEquals(5, (fromBundle.data["activity"] as List<*>).size)
        assertTrue((fromBundle.data["description"] as String).contains("IKNL", ignoreCase = true))

        val carePlan = service.fetchCarePlan()
        assertEquals("CarePlan", carePlan.resourceType)
        assertTrue((carePlan.data["activity"] as List<*>).isNotEmpty())
    }

    @Test
    fun appointments_isoStartAndParticipants() = runBlocking {
        val appts = bundle().resources.filter { it.resourceType == "Appointment" }
        assertTrue(appts.size >= 3)
        assertTrue((appts.first().data["start"] as String).contains("T"))
        assertTrue((appts.first().data["participant"] as List<*>).isNotEmpty())

        val fetched = service.fetchAppointments()
        assertTrue(fetched.isNotEmpty())
    }

    @Test
    fun allResources_haveNonEmptyIds() {
        bundle().resources.forEach {
            assertTrue(it.id.isNotEmpty(), "${it.resourceType} should have an id")
        }
    }
}
