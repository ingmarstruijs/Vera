package net.moonbaseone.vera.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VeraSharedTest {
    @Test
    fun packageId_matchesProductDomain() {
        assertEquals("net.moonbaseone.vera", VeraShared.PACKAGE_ID)
    }

    @Test
    fun primaryTabs_areTimelineDecoderAppointments() {
        assertEquals(3, VeraShared.primaryTabs.size)
        assertEquals(
            listOf("timeline", "decoder", "appointments"),
            VeraShared.primaryTabs.map { it.route },
        )
    }

    @Test
    fun platformName_isNonBlank() {
        assertTrue(platformName().isNotBlank())
    }
}
