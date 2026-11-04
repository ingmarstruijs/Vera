package net.moonbaseone.vera.shared.fhir

import net.moonbaseone.vera.shared.failure.Failure

/**
 * Thin FHIR R4 resource wrapper — deliberately lightweight (no full FHIR SDK).
 * [data] mirrors the Flutter Map JSON shape.
 */
data class FhirResource(
    val resourceType: String,
    val id: String,
    val data: Map<String, Any?>,
)

data class FhirBundle(
    val resources: List<FhirResource>,
    /** ISO-8601 timestamp. */
    val timestamp: String,
)

/** Result of [MockDataService.fetchOncologyBundle]. */
data class OncologyBundleResult(
    val bundle: FhirBundle? = null,
    val failure: Failure? = null,
)
