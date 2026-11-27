package net.moonbaseone.vera.shared.ui

/**
 * Shared clinical colour tokens (ARGB hex) — no widgets in commonMain.
 * Soft teal + lavender system inspired by the care-plan / appointment mocks:
 * deep teal for primary trust, muted purple for secondary emphasis, mint washes.
 */
object DesignTokens {
    /** Deep teal — primary actions (mock ≈ #006054). */
    const val PRIMARY_GREEN = 0xFF006054
    /** Soft mint accent on dark surfaces. */
    const val PRIMARY_GREEN_DARK = 0xFF8FCFC2
    const val ON_PRIMARY_DARK = 0xFF003730

    const val PRIMARY_CONTAINER = 0xFFD9F0EB
    const val PRIMARY_CONTAINER_DARK = 0xFF0F3F38
    const val ON_PRIMARY_CONTAINER_DARK = 0xFFD4F2EC

    /** Soft lavender — secondary CTAs / active chips (mock ≈ #6A4EAD). */
    const val ACCENT_PURPLE = 0xFF6A4EAD
    const val ACCENT_PURPLE_CONTAINER = 0xFFEDE7F8
    const val ACCENT_PURPLE_DARK = 0xFFC4B5F0
    const val ACCENT_PURPLE_CONTAINER_DARK = 0xFF2E2450

    const val AMBER = 0xFFB86E00
    const val AMBER_DARK = 0xFFE0B06A
    const val ERROR_RED = 0xFFB3261E
    const val ERROR_RED_DARK = 0xFFF2B8B5

    const val TEXT_DARK = 0xFF12241F
    const val TEXT_ON_DARK = 0xFFE6F2EF
    const val TEXT_SECONDARY = 0xFF4A635C
    const val TEXT_SECONDARY_DARK = 0xFFB5C9C2

    /** Soft cool wash — pairs with subtle gradients in UI. */
    const val SURFACE_LIGHT = 0xFFF5F8FA
    const val SURFACE_ELEVATED = 0xFFFFFFFF
    const val SURFACE_MINT = 0xFFE8F6F2
    const val SURFACE_LAVENDER = 0xFFF3EFFA
    const val SURFACE_DARK = 0xFF0E1715
    const val SURFACE_VARIANT_DARK = 0xFF1A2623
    const val SURFACE_ELEVATED_DARK = 0xFF1A2623

    const val OUTLINE_SUBTLE = 0xFFC9D7D2
    const val OUTLINE_DARK = 0xFF6B7F78
    const val OUTLINE_VARIANT_DARK = 0xFF35433E

    const val SENTINEL_GREEN = 0xFF006054
    const val SENTINEL_GREEN_DARK = 0xFF8FCFC2
    const val SENTINEL_BG = 0xFFE6F5F1
    const val SENTINEL_BG_DARK = 0xFF12302B

    const val BANNER_WARNING_BG = 0xFFFFF3E0
    const val BANNER_WARNING_BG_DARK = 0xFF3A2A12
    const val BANNER_ERROR_BG = 0xFFFFEBEE
    const val BANNER_ERROR_BG_DARK = 0xFF3B1513

    const val STATUS_COMPLETED = 0xFF1E4D7B
    const val STATUS_COMPLETED_DARK = 0xFF8BB4DE
    const val STATUS_IN_PROGRESS = 0xFF006054
    const val STATUS_IN_PROGRESS_DARK = 0xFF8FCFC2
    const val STATUS_SCHEDULED = 0xFFB86E00
    const val STATUS_SCHEDULED_DARK = 0xFFE0B06A
    const val STATUS_UNKNOWN = 0xFF5C6B66
    const val STATUS_UNKNOWN_DARK = 0xFFA8B5B0

    /** Card corner radius used across soft surfaces. */
    const val CARD_RADIUS_DP = 20
    const val CHIP_RADIUS_DP = 999

    fun statusColorArgb(fhirStatus: String?, dark: Boolean = false): Long = when (fhirStatus) {
        "active", "in-progress" -> if (dark) STATUS_IN_PROGRESS_DARK else STATUS_IN_PROGRESS
        "completed" -> if (dark) STATUS_COMPLETED_DARK else STATUS_COMPLETED
        "scheduled", "booked" -> if (dark) STATUS_SCHEDULED_DARK else STATUS_SCHEDULED
        "cancelled", "entered-in-error" -> if (dark) ERROR_RED_DARK else ERROR_RED
        else -> if (dark) STATUS_UNKNOWN_DARK else STATUS_UNKNOWN
    }
}
