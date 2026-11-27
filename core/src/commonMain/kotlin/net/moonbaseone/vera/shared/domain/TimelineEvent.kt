package net.moonbaseone.vera.shared.domain

enum class TimelinePhase {
    Completed,
    Current,
    Future,
}

/** Domain entity for a single event on the Vera Blueprint timeline. */
data class TimelineEvent(
    val id: String,
    val title: String,
    val description: String,
    /** ISO-8601 date or period start date. */
    val dateIso: String,
    val phase: TimelinePhase,
    val fhirResourceType: String,
    val iknlGuidelineRef: String? = null,
    /** Plain-language action items derived from the CarePlan / Decoder. */
    val actionItems: List<String> = emptyList(),
    val fhirStatus: String? = null,
    /** What the patient should know / do around this step. */
    val patientInstruction: String? = null,
    /** Concrete next clinical step after this activity. */
    val nextSteps: String? = null,
    /**
     * Compact label for timeline chips (care-step name, not Afgerond/Huidig).
     * Always set when mapped from CarePlan activities ([deriveShortTitle] if absent).
     */
    val shortTitle: String? = null,
) {
    /** Label shown on the care-plan timeline chip. */
    fun chipLabel(): String = chipLabelFromTitle(shortTitle, title)

    /** Haystack used for inline glossary linking. */
    fun glossaryHaystack(): String = listOfNotNull(
        title,
        description,
        patientInstruction,
        nextSteps,
        actionItems.joinToString(" "),
    ).joinToString(" ")

    companion object {
        /**
         * Maps CarePlan.activity entries (FHIR-ish maps) into timeline events.
         *
         * Recognises Vera mock extensions on `detail`:
         * `shortTitle`, `patientInstruction`, `nextStep`, `actionItem` (list), `iknlGuidelineRef`.
         * Missing `shortTitle` is derived from the activity title so every dataset gets chip labels.
         */
        @Suppress("UNCHECKED_CAST")
        fun fromCarePlanActivities(
            activities: List<Map<String, Any?>>,
        ): List<TimelineEvent> {
            return activities.map { activity ->
                val detail = (activity["detail"] as? Map<String, Any?>).orEmpty()
                val codeMap = detail["code"] as? Map<String, Any?>
                val code = codeMap?.get("text") as? String ?: ""
                val status = detail["status"] as? String ?: "unknown"
                val period = detail["scheduledPeriod"] as? Map<String, Any?>
                val startDate = period?.get("start") as? String ?: ""
                val title = code.split("–").first().trim()

                val phase = when (status) {
                    "completed" -> TimelinePhase.Completed
                    "in-progress", "active" -> TimelinePhase.Current
                    else -> TimelinePhase.Future
                }

                val actionItems = (detail["actionItem"] as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    .orEmpty()

                val explicitShort = (detail["shortTitle"] as? String)?.trim()
                    ?.takeIf { it.isNotEmpty() }

                TimelineEvent(
                    id = code.hashCode().toString(),
                    title = title,
                    description = (detail["description"] as? String) ?: code,
                    dateIso = startDate,
                    phase = phase,
                    fhirResourceType = detail["kind"] as? String ?: "ServiceRequest",
                    fhirStatus = status,
                    patientInstruction = (detail["patientInstruction"] as? String)?.trim()
                        ?.takeIf { it.isNotEmpty() },
                    nextSteps = (detail["nextStep"] as? String)?.trim()
                        ?.takeIf { it.isNotEmpty() },
                    actionItems = actionItems,
                    iknlGuidelineRef = (detail["iknlGuidelineRef"] as? String)?.trim()
                        ?.takeIf { it.isNotEmpty() },
                    shortTitle = explicitShort ?: deriveShortTitle(title),
                )
            }
        }

        /** Prefer explicit short title; otherwise derive a compact care-step label. */
        fun chipLabelFromTitle(shortTitle: String?, title: String): String {
            shortTitle?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            return deriveShortTitle(title)
        }

        /**
         * Builds a chip-sized care-step name from a long activity title (NL/EN keywords).
         * Used for datasets without a Vera `shortTitle` extension (e.g. future MedMij feeds).
         */
        fun deriveShortTitle(title: String): String {
            val cleaned = title
                .substringBefore("(")
                .substringBefore("+")
                .trim()
            if (cleaned.isEmpty()) return title.ifBlank { "—" }
            val t = cleaned.lowercase()

            return when {
                containsAny(t, "her2", "trastuzumab", "pertuzumab", "herceptin", "perjeta") ->
                    "HER2-therapie"
                containsAny(t, "immuno", "immunotherapy", "immunotherapie", "checkpoint") ->
                    "Immunotherapie"
                containsAny(t, "radio", "bestraling", "radiation", "radiotherapy") ->
                    "Radiotherapie"
                containsAny(
                    t,
                    "chemo",
                    "chemotherapie",
                    "chemotherapy",
                    "tchp",
                    "capox",
                    "folfox",
                    "oxaliplatine",
                    "oxaliplatin",
                ) -> "Chemotherapie"
                containsAny(
                    t,
                    "chirurgie",
                    "surgery",
                    "operatie",
                    "resectie",
                    "resection",
                    "colectomie",
                    "hemicolectomie",
                    "lumpectomie",
                    "mastectomie",
                    "borstsparend",
                ) || t.split(Regex("\\s+")).any { it == "bcs" } -> "Chirurgie"
                t.startsWith("mdo") || containsAny(t, "multidisciplinair", "multidisciplinary") ->
                    "MDO"
                containsAny(
                    t,
                    "nacontrole",
                    "follow-up",
                    "follow up",
                    "survivorship",
                    "controle-plan",
                    "controleplan",
                ) || (containsAny(t, "mammografie", "mammography", "cea") &&
                    containsAny(t, "controle", "control", "jaarlijks", "annual")) ->
                    "Nacontrole"
                containsAny(
                    t,
                    "diagnostiek",
                    "diagnostics",
                    "diagnostic",
                    "biopsie",
                    "biopsy",
                    "pet-ct",
                    "pet/ct",
                ) -> "Diagnostiek"
                containsAny(t, "behandeling", "treatment", "therapy", "therapie") &&
                    !containsAny(t, "her2", "immuno", "chemo", "radio") ->
                    "Behandeling"
                else -> {
                    val words = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
                    words.take(2).joinToString(" ").ifBlank { cleaned }
                }
            }
        }

        private fun containsAny(haystack: String, vararg needles: String): Boolean =
            needles.any { haystack.contains(it) }
    }
}
