package net.moonbaseone.vera.shared.ui

/** Shared colour tokens (ARGB hex) — no widgets in commonMain. */
object DesignTokens {
    const val PRIMARY_GREEN = 0xFF1B5E20
    const val PRIMARY_CONTAINER = 0xFFD7F2DC
    const val AMBER = 0xFFE07B00
    const val ERROR_RED = 0xFFC62828
    const val TEXT_DARK = 0xFF121212
    const val SURFACE_LIGHT = 0xFFF8FAF8
    const val SENTINEL_GREEN = 0xFF1B7A3E
    const val SENTINEL_BG = 0xFFE8F5EE

    const val STATUS_COMPLETED = 0xFF0D47A1
    const val STATUS_IN_PROGRESS = 0xFF1B5E20
    const val STATUS_SCHEDULED = 0xFFE07B00
    const val STATUS_UNKNOWN = 0xFF616161

    fun statusColorArgb(fhirStatus: String?): Long = when (fhirStatus) {
        "active", "in-progress" -> STATUS_IN_PROGRESS
        "completed" -> STATUS_COMPLETED
        "scheduled", "booked" -> STATUS_SCHEDULED
        "cancelled", "entered-in-error" -> ERROR_RED
        else -> STATUS_UNKNOWN
    }
}
