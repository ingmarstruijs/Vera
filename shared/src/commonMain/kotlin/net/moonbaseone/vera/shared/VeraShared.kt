package net.moonbaseone.vera.shared

import net.moonbaseone.vera.shared.navigation.AppDestination

/**
 * Entry point for shared KMP business logic.
 * Feature packages live under vault / shield / decoder / appointments / careplan
 * (Gradle module extract remains a later P11 slice — see docs/migration/module-map.md).
 */
object VeraShared {
    const val APP_NAME: String = "Vera"
    const val PACKAGE_ID: String = "net.moonbaseone.vera"

    /** Post-unlock shell destinations — care plan → decoder → appointments. */
    val primaryTabs: List<VeraTab> = listOf(
        VeraTab.CarePlan,
        VeraTab.Decoder,
        VeraTab.Appointments,
    )

    val primaryDestinations: List<AppDestination>
        get() = AppDestination.primaryShell
}

enum class VeraTab(val route: String, val titleEn: String) {
    CarePlan(route = AppDestination.CarePlan.route, titleEn = "Care plan"),
    Decoder(route = AppDestination.Decoder.route, titleEn = "Decoder"),
    Appointments(route = AppDestination.Appointments.route, titleEn = "Appointments"),
}

/** Platform label from expect/actual. */
expect fun platformName(): String
