package net.moonbaseone.vera.shared.fhir

/**
 * Future production FHIR / MedMij transport boundary.
 *
 * P11 only defines the contract. Do **not** add live MedMij credentials,
 * network clients, or hospital EPD adapters here — that is a later
 * compliance track. ViewModels read care-plan data via
 * [net.moonbaseone.vera.shared.careplan.CarePlanRepository] today.
 */
interface FhirClient {
    suspend fun fetchOncologyBundle(): OncologyBundleResult
}

/**
 * Placeholder so DI can bind a non-production client without implying
 * MedMij readiness. Always fails closed.
 */
class UnsupportedFhirClient : FhirClient {
    override suspend fun fetchOncologyBundle(): OncologyBundleResult =
        OncologyBundleResult(
            failure = net.moonbaseone.vera.shared.failure.FhirParseFailure(
                "Production FHIR/MedMij client is not configured (P11 boundary prep only).",
            ),
        )
}
