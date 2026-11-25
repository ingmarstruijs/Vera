package net.moonbaseone.vera.shared.navigation

/**
 * Typed shell destinations for Android / iOS navigation.
 *
 * Platforms map these routes to Compose Navigation / SwiftUI TabView.
 */
sealed class AppDestination(val route: String) {
    data object Unlock : AppDestination("unlock")
    data object CarePlan : AppDestination("careplan")
    data object Decoder : AppDestination("decoder")
    data object Appointments : AppDestination("appointments")
    data object Contacts : AppDestination("contacts")
    data object Symptoms : AppDestination("symptoms")
    data class PocketGuide(val appointmentId: String) :
        AppDestination("pocketGuide/$appointmentId") {
        companion object {
            const val routePattern = "pocketGuide/{appointmentId}"
            fun routeFor(appointmentId: String) = "pocketGuide/$appointmentId"
        }
    }

    companion object {
        /**
         * Post-unlock shell IA: care plan → decoder → appointments → contacts.
         * Computed (not stored) to avoid sealed nested-object init-order nulls.
         */
        val primaryShell: List<AppDestination>
            get() = listOf(CarePlan, Decoder, Appointments, Contacts)

        fun fromRoute(route: String): AppDestination? = when {
            route == Unlock.route -> Unlock
            route == CarePlan.route -> CarePlan
            route == Decoder.route -> Decoder
            route == Appointments.route -> Appointments
            route == Contacts.route -> Contacts
            route == Symptoms.route -> Symptoms
            // Legacy P5 route alias
            route == "timeline" -> CarePlan
            route.startsWith("pocketGuide/") -> {
                val id = route.removePrefix("pocketGuide/")
                if (id.isNotBlank()) PocketGuide(id) else null
            }
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
