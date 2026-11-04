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
) {
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
         * `patientInstruction`, `nextStep`, `actionItem` (list), `iknlGuidelineRef`.
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

                TimelineEvent(
                    id = code.hashCode().toString(),
                    title = code.split("–").first().trim(),
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
                )
            }
        }
    }
}
