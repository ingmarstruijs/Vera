package net.moonbaseone.vera.shared

import net.moonbaseone.vera.shared.navigation.AppDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VeraSharedTest {
    @Test
    fun packageId_matchesProductDomain() {
        assertEquals("net.moonbaseone.vera", VeraShared.PACKAGE_ID)
    }

    @Test
    fun primaryTabs_areCarePlanDecoderAppointmentsContacts() {
        assertEquals(4, VeraShared.primaryTabs.size)
        assertEquals(
            listOf("careplan", "decoder", "appointments", "contacts"),
            VeraShared.primaryTabs.map { it.route },
        )
        assertEquals(AppDestination.primaryShell, VeraShared.primaryDestinations)
    }

    @Test
    fun appDestination_mapsLegacyTimelineAlias() {
        assertEquals(AppDestination.CarePlan, AppDestination.fromRoute("timeline"))
        assertEquals(AppDestination.CarePlan, AppDestination.fromRoute("careplan"))
    }

    @Test
    fun platformName_isNonBlank() {
        assertTrue(platformName().isNotBlank())
    }
}
