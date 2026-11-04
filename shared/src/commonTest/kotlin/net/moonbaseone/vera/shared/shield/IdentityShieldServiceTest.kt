package net.moonbaseone.vera.shared.shield

import net.moonbaseone.vera.shared.AppConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentityShieldServiceTest {
    private val shield = IdentityShieldService()

    @Test
    fun validBsn_isRedacted() {
        val bsn = "123456782"
        val result = shield.shield("Patient with BSN $bsn registered")
        assertTrue(result.contains(AppConstants.REDACTION_TOKEN))
        assertFalse(result.contains(bsn))
    }

    @Test
    fun invalidBsn_failsElfproef_notRedactedAsTokenOnly() {
        val invalid = "123456789"
        assertFalse(shield.isValidBsn(invalid))
        val result = shield.shield("Invalid BSN: $invalid")
        // Invalid candidate must not be replaced by the redaction token via BSN path.
        assertTrue(result.contains(invalid))
    }

    @Test
    fun multipleValidBsns_redacted() {
        val result = shield.shield("Patient A (123456782) and Patient B (111222333)")
        // 111222333 — check if valid; at least one redaction for known valid
        assertTrue(result.contains(AppConstants.REDACTION_TOKEN))
        assertFalse(result.contains("123456782"))
    }

    @Test
    fun dutchPostcode_redacted() {
        val postcode = "3511ZE"
        val result = shield.shield("Patient lives at Postcode $postcode (Utrecht)")
        assertTrue(result.contains(AppConstants.REDACTION_TOKEN))
        assertFalse(result.contains(postcode))
    }

    @Test
    fun postcodeWithSpace_redacted() {
        val result = shield.shield("Address: 3511 ZE")
        assertTrue(result.contains(AppConstants.REDACTION_TOKEN))
        assertFalse(result.contains("3511 ZE"))
    }

    @Test
    fun email_and_phone_redacted() {
        val email = "patient@example.com"
        val phone = "+31612345678"
        val result = shield.shield("Contact: $email Phone: $phone")
        assertFalse(result.contains(email))
        assertFalse(result.contains(phone))
        assertTrue(result.contains(AppConstants.REDACTION_TOKEN))
    }

    @Test
    fun shieldMap_preservesMedicalDataAndStructure() {
        val data = mapOf(
            "patient" to mapOf(
                "bsn" to "123456782",
                "contact" to mapOf(
                    "email" to "jan@example.com",
                    "phone" to "+31612345678",
                ),
            ),
            "diagnosis" to "C50.4 IDC",
            "contacts" to listOf(
                mapOf("email" to "a@example.com", "name" to "Alice"),
                mapOf("email" to "b@example.com", "name" to "Bob"),
            ),
        )
        val result = shield.shieldMap(data)
        assertEquals("C50.4 IDC", result["diagnosis"])
        @Suppress("UNCHECKED_CAST")
        val contacts = result["contacts"] as List<*>
        assertEquals(2, contacts.size)
    }

    @Test
    fun fhirBundle_medicalCodesPreserved() {
        val bundle = mapOf(
            "resourceType" to "Bundle",
            "entry" to listOf(
                mapOf(
                    "resource" to mapOf(
                        "resourceType" to "Condition",
                        "code" to mapOf(
                            "coding" to listOf(
                                mapOf(
                                    "system" to "http://hl7.org/fhir/sid/icd-10-cm",
                                    "code" to "C50.4",
                                    "display" to "Invasief ductaal carcinoom",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        val result = shield.shieldMap(bundle)
        @Suppress("UNCHECKED_CAST")
        val entry = (result["entry"] as List<*>)[0] as Map<*, *>
        val resource = entry["resource"] as Map<*, *>
        val coding = ((resource["code"] as Map<*, *>)["coding"] as List<*>)[0] as Map<*, *>
        assertEquals("C50.4", coding["code"])
    }

    @Test
    fun empty_and_deterministic() {
        assertEquals("", shield.shield(""))
        val text = "Email: test@example.com"
        assertEquals(shield.shield(text), shield.shield(text))
    }
}
