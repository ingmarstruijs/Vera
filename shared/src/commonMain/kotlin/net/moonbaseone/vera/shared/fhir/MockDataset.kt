package net.moonbaseone.vera.shared.fhir

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.moonbaseone.vera.shared.domain.IknlGuidelines

/**
 * Demo clinical scenarios the app can load from [MockDataService].
 * Not production FHIR — local fixtures for UX / care-plan exploration.
 */
enum class MockDatasetId {
    BreastHer2IIa,
    ColonStageIii,
    LungEarlyStart,
}

data class MockDatasetInfo(
    val id: MockDatasetId,
    /** Short chip label. */
    val title: String,
    /** One-line clinical context under the picker. */
    val subtitle: String,
    /** Canonical Richtlijnendatabase URL for this scenario. */
    val guidelineUrl: String,
)

object MockDatasets {
    val all: List<MockDatasetInfo> = listOf(
        MockDatasetInfo(
            id = MockDatasetId.BreastHer2IIa,
            title = "HER2+ mamma IIA",
            subtitle = "Radiotherapie + trastuzumab actief (UMC Utrecht)",
            guidelineUrl = IknlGuidelines.BREAST,
        ),
        MockDatasetInfo(
            id = MockDatasetId.ColonStageIii,
            title = "Colon stadium III",
            subtitle = "Adjuvante chemotherapie na resectie (AMC)",
            guidelineUrl = IknlGuidelines.COLON,
        ),
        MockDatasetInfo(
            id = MockDatasetId.LungEarlyStart,
            title = "Long – start",
            subtitle = "Net gediagnosticeerd NSCLC, MDO gepland (AVL)",
            guidelineUrl = IknlGuidelines.LUNG_NSCLC,
        ),
    )

    fun info(id: MockDatasetId): MockDatasetInfo =
        all.first { it.id == id }
}

/** In-memory selection of the active mock dataset; ViewModels reload on change. */
class ActiveDatasetStore(
    initial: MockDatasetId = MockDatasetId.BreastHer2IIa,
) {
    private val _id = MutableStateFlow(initial)
    val id: StateFlow<MockDatasetId> = _id.asStateFlow()

    fun select(id: MockDatasetId) {
        if (_id.value != id) {
            _id.value = id
        }
    }
}
