package net.moonbaseone.vera.shared.domain

/**
 * Pure domain entity — no SQLDelight / UI dependencies.
 * Represents a single medical record destined for the encrypted vault.
 */
data class MedicalRecord(
    val id: String,
    /** FHIR R4 ResourceType, e.g. "Condition", "Procedure". */
    val resourceType: String,
    /** Human-readable title (already decoded/translated). */
    val title: String,
    /** ISO-8601 date when recorded/imported. */
    val dateRecorded: String,
    /**
     * Anonymised JSON payload — PII stripped by IdentityShield before create.
     * Stored encrypted in the vault (crypto lands in later phases).
     */
    val anonymisedPayloadJson: String,
    val iknlGuidelineRef: String? = null,
    val fhirStatus: String? = null,
    val tags: List<String> = emptyList(),
)
