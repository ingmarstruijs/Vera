package net.moonbaseone.vera.shared

/**
 * Entry point for shared KMP business logic.
 * Feature ports (domain, vault, shield, FHIR mock) land in later phases.
 */
object VeraShared {
    const val APP_NAME: String = "Vera"
    const val PACKAGE_ID: String = "net.moonbaseone.vera"

    /** Placeholder tabs for native shells — Timeline | Decoder | Appointments. */
    val primaryTabs: List<VeraTab> = listOf(
        VeraTab.Timeline,
        VeraTab.Decoder,
        VeraTab.Appointments,
    )
}

enum class VeraTab(val route: String, val titleEn: String) {
    Timeline(route = "timeline", titleEn = "Timeline"),
    Decoder(route = "decoder", titleEn = "Decoder"),
    Appointments(route = "appointments", titleEn = "Appointments"),
}

/** Platform label from expect/actual (stub). */
expect fun platformName(): String
