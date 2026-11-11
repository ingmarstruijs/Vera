package net.moonbaseone.vera.shared.util

/**
 * Formats FHIR/ISO-8601 instant strings for patient-facing UI without a datetime dependency.
 * Examples: `2025-04-22T09:00:00+02:00` → `22 apr 2025 · 09:00`
 */
object DateDisplay {
    private val monthNl = listOf(
        "jan", "feb", "mrt", "apr", "mei", "jun",
        "jul", "aug", "sep", "okt", "nov", "dec",
    )
    private val monthEn = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    fun formatIso(iso: String, dutch: Boolean = true): String {
        val trimmed = iso.trim()
        if (trimmed.isEmpty()) return ""
        val match = ISO_REGEX.matchEntire(trimmed) ?: return trimmed
        val year = match.groupValues[1]
        val month = match.groupValues[2].toIntOrNull() ?: return trimmed
        val day = match.groupValues[3].toIntOrNull() ?: return trimmed
        val hour = match.groupValues[4]
        val minute = match.groupValues[5]
        if (month !in 1..12) return trimmed
        val monthLabel = (if (dutch) monthNl else monthEn)[month - 1]
        val datePart = if (dutch) {
            "$day $monthLabel $year"
        } else {
            "$day $monthLabel $year"
        }
        return if (hour.isNotEmpty() && minute.isNotEmpty()) {
            "$datePart · $hour:$minute"
        } else {
            datePart
        }
    }

    private val ISO_REGEX = Regex(
        """(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2})(?::\d{2}(?:\.\d+)?)?(?:Z|[+-]\d{2}:?\d{2})?)?""",
    )
}
