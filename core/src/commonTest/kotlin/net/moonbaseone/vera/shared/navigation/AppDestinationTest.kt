package net.moonbaseone.vera.shared.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppDestinationTest {
    @Test
    fun primaryShell_orderIsCarePlanFirst() {
        assertEquals(
            listOf("careplan", "decoder", "appointments", "contacts"),
            AppDestination.primaryShell.map { it.route },
        )
    }

    @Test
    fun fromRoute_unknownIsNull() {
        assertNull(AppDestination.fromRoute("sync"))
        assertNull(AppDestination.fromRoute("viewer"))
    }
}
