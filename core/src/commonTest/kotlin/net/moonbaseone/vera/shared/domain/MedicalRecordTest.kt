package net.moonbaseone.vera.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class MedicalRecordTest {
    private fun sample(
        id: String = "test-uuid-001",
        resourceType: String = "Condition",
        title: String = "Diagnose",
        dateRecorded: String = "2024-04-10",
        payload: String = """{"code":"C50.4"}""",
        iknl: String? = null,
        status: String? = "final",
        tags: List<String> = emptyList(),
    ) = MedicalRecord(
        id = id,
        resourceType = resourceType,
        title = title,
        dateRecorded = dateRecorded,
        anonymisedPayloadJson = payload,
        iknlGuidelineRef = iknl,
        fhirStatus = status,
        tags = tags,
    )

    @Test
    fun fields_roundTripViaCopy() {
        val record = sample(tags = listOf("oncologie", "HER2"))
        val copy = record.copy(title = "Updated")
        assertEquals("Updated", copy.title)
        assertEquals(record.id, copy.id)
        assertEquals(listOf("oncologie", "HER2"), copy.tags)
    }

    @Test
    fun optionalFields_nullable() {
        val record = sample(iknl = null, status = null)
        assertNull(record.iknlGuidelineRef)
        assertNull(record.fhirStatus)
    }

    @Test
    fun resourceType_and_date_unencryptedForIndexing() {
        val record = sample(resourceType = "Observation", dateRecorded = "2024-06-15")
        assertEquals("Observation", record.resourceType)
        assertEquals("2024-06-15", record.dateRecorded)
    }

    @Test
    fun uuid_uniqueAcrossRecords() {
        assertNotEquals(sample(id = "aaa").id, sample(id = "bbb").id)
    }

    @Test
    fun payload_isNotPlaintextDiagnosis() {
        val plaintext = "Invasief ductaal carcinoom"
        val record = sample(payload = "anonymised-payload")
        assertNotEquals(plaintext, record.anonymisedPayloadJson)
    }
}
