package net.moonbaseone.vera.shared

import net.moonbaseone.vera.shared.navigation.AppDestination

/**
 * Entry point for shared KMP business logic.
 * Stable types (domain, failure, i18n, navigation, tokens) live in Gradle `:core`.
 * Feature packages (vault / shield / decoder / appointments / careplan) remain in `:shared`
 * until further Gradle extracts — see docs/migration/module-map.md.
 */
object VeraShared {
    const val APP_NAME: String = "Vera"
    const val PACKAGE_ID: String = "net.moonbaseone.vera"

    /** Post-unlock shell destinations — care plan → dictionary → appointments → contacts. */
    val primaryTabs: List<VeraTab> = listOf(
        VeraTab.CarePlan,
        VeraTab.Decoder,
        VeraTab.Appointments,
        VeraTab.Contacts,
    )

    val primaryDestinations: List<AppDestination>
        get() = AppDestination.primaryShell
}

enum class VeraTab(val route: String, val titleEn: String) {
    CarePlan(route = AppDestination.CarePlan.route, titleEn = "Care plan"),
    Decoder(route = AppDestination.Decoder.route, titleEn = "Dictionary"),
    Appointments(route = AppDestination.Appointments.route, titleEn = "Appointments"),
    Contacts(route = AppDestination.Contacts.route, titleEn = "Contact"),
}

/** Platform label from expect/actual. */
expect fun platformName(): String
