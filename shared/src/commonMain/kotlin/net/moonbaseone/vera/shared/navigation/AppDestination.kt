package net.moonbaseone.vera.shared.navigation

/**
 * Typed shell destinations for Android / iOS nav prep (P11).
 *
 * Platforms map these routes to Compose Navigation / SwiftUI TabView.
 * Full Decompose/Circuit adoption remains a follow-up once product needs
 * deeper shared back-stack orchestration.
 */
sealed class AppDestination(val route: String) {
    data object Unlock : AppDestination("unlock")
    data object CarePlan : AppDestination("careplan")
    data object Decoder : AppDestination("decoder")
    data object Appointments : AppDestination("appointments")

    companion object {
        /**
         * Post-unlock shell IA: care plan → decoder → appointments.
         * Computed (not stored) to avoid sealed nested-object init-order nulls.
         */
        val primaryShell: List<AppDestination>
            get() = listOf(CarePlan, Decoder, Appointments)

        fun fromRoute(route: String): AppDestination? = when (route) {
            Unlock.route -> Unlock
            CarePlan.route -> CarePlan
            Decoder.route -> Decoder
            Appointments.route -> Appointments
            // Legacy P5 route alias
            "timeline" -> CarePlan
            else -> null
        }
    }
}

/**
 * Thin navigation contract for shared coordinators.
 * Platforms supply the actual back stack / tab switcher.
 */
interface AppNavigator {
    fun navigate(destination: AppDestination)
    fun current(): AppDestination?
}
