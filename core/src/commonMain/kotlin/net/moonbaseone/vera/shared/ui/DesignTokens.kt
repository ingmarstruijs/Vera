package net.moonbaseone.vera.shared.ui

/**
 * Shared clinical colour tokens (ARGB hex) — no widgets in commonMain.
 * P11: calmer oncology palette (muted sage / slate) rather than loud Material teal.
 * Dark variants keep the same clinical-green brand under system dark mode.
 */
object DesignTokens {
    /** Deep sage — primary actions, trustworthy clinical accent. */
    const val PRIMARY_GREEN = 0xFF2F5D50
    /** Soft sage for dark surfaces (readable accent, not neon). */
    const val PRIMARY_GREEN_DARK = 0xFF9BC4B5
    const val ON_PRIMARY_DARK = 0xFF0C2A22

    const val PRIMARY_CONTAINER = 0xFFE2EEE9
    const val PRIMARY_CONTAINER_DARK = 0xFF1E3F36
    const val ON_PRIMARY_CONTAINER_DARK = 0xFFD8EDE6

    const val AMBER = 0xFFB86E00
    const val AMBER_DARK = 0xFFE0B06A
    const val ERROR_RED = 0xFFB3261E
    const val ERROR_RED_DARK = 0xFFF2B8B5

    const val TEXT_DARK = 0xFF1A2421
    const val TEXT_ON_DARK = 0xFFE4ECE9
    /** Minimum contrast-friendly on-surface secondary text. */
    const val TEXT_SECONDARY = 0xFF4A5A55
    const val TEXT_SECONDARY_DARK = 0xFFB7C4BF

    /** Cool clinical surface (not flat white, not warm cream). */
    const val SURFACE_LIGHT = 0xFFF3F6F5
    const val SURFACE_ELEVATED = 0xFFFFFFFF
    const val SURFACE_DARK = 0xFF121917
    const val SURFACE_VARIANT_DARK = 0xFF1C2623
    const val SURFACE_ELEVATED_DARK = 0xFF1C2623

    const val OUTLINE_SUBTLE = 0xFFC5D0CB
    const val OUTLINE_DARK = 0xFF6B7A74
    const val OUTLINE_VARIANT_DARK = 0xFF3A4641

    const val SENTINEL_GREEN = 0xFF2A6B4E
    const val SENTINEL_GREEN_DARK = 0xFF7FB89A
    const val SENTINEL_BG = 0xFFE6F2EC
    const val SENTINEL_BG_DARK = 0xFF152820

    const val BANNER_WARNING_BG = 0xFFFFF3E0
    const val BANNER_WARNING_BG_DARK = 0xFF3A2A12
    const val BANNER_ERROR_BG = 0xFFFFEBEE
    const val BANNER_ERROR_BG_DARK = 0xFF3B1513

    const val STATUS_COMPLETED = 0xFF1E4D7B
    const val STATUS_COMPLETED_DARK = 0xFF8BB4DE
    const val STATUS_IN_PROGRESS = 0xFF2F5D50
    const val STATUS_IN_PROGRESS_DARK = 0xFF9BC4B5
    const val STATUS_SCHEDULED = 0xFFB86E00
    const val STATUS_SCHEDULED_DARK = 0xFFE0B06A
    const val STATUS_UNKNOWN = 0xFF5C6B66
    const val STATUS_UNKNOWN_DARK = 0xFFA8B5B0

    fun statusColorArgb(fhirStatus: String?, dark: Boolean = false): Long = when (fhirStatus) {
        "active", "in-progress" -> if (dark) STATUS_IN_PROGRESS_DARK else STATUS_IN_PROGRESS
        "completed" -> if (dark) STATUS_COMPLETED_DARK else STATUS_COMPLETED
        "scheduled", "booked" -> if (dark) STATUS_SCHEDULED_DARK else STATUS_SCHEDULED
        "cancelled", "entered-in-error" -> if (dark) ERROR_RED_DARK else ERROR_RED
        else -> if (dark) STATUS_UNKNOWN_DARK else STATUS_UNKNOWN
    }
}
