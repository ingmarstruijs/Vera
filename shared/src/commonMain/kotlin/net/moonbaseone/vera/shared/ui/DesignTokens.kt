package net.moonbaseone.vera.shared.ui

/**
 * Shared clinical colour tokens (ARGB hex) — no widgets in commonMain.
 * P11: calmer oncology palette (muted sage / slate) rather than loud Material teal.
 */
object DesignTokens {
    /** Deep sage — primary actions, trustworthy clinical accent. */
    const val PRIMARY_GREEN = 0xFF2F5D50
    const val PRIMARY_CONTAINER = 0xFFE2EEE9
    const val AMBER = 0xFFB86E00
    const val ERROR_RED = 0xFFB3261E
    const val TEXT_DARK = 0xFF1A2421
    /** Cool clinical surface (not flat white, not warm cream). */
    const val SURFACE_LIGHT = 0xFFF3F6F5
    const val SURFACE_ELEVATED = 0xFFFFFFFF
    const val OUTLINE_SUBTLE = 0xFFC5D0CB
    const val SENTINEL_GREEN = 0xFF2A6B4E
    const val SENTINEL_BG = 0xFFE6F2EC

    const val STATUS_COMPLETED = 0xFF1E4D7B
    const val STATUS_IN_PROGRESS = 0xFF2F5D50
    const val STATUS_SCHEDULED = 0xFFB86E00
    const val STATUS_UNKNOWN = 0xFF5C6B66

    /** Minimum contrast-friendly on-surface secondary text. */
    const val TEXT_SECONDARY = 0xFF4A5A55

    fun statusColorArgb(fhirStatus: String?): Long = when (fhirStatus) {
        "active", "in-progress" -> STATUS_IN_PROGRESS
        "completed" -> STATUS_COMPLETED
        "scheduled", "booked" -> STATUS_SCHEDULED
        "cancelled", "entered-in-error" -> ERROR_RED
        else -> STATUS_UNKNOWN
    }
}
