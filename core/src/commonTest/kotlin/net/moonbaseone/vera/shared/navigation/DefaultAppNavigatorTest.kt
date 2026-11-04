package net.moonbaseone.vera.shared.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAppNavigatorTest {
    @Test
    fun startsAtUnlockAndNavigatesToShellDestinations() {
        val navigator = DefaultAppNavigator()
        assertEquals(AppDestination.Unlock, navigator.current())

        navigator.navigate(AppDestination.CarePlan)
        assertEquals(AppDestination.CarePlan, navigator.current())
        assertEquals(AppDestination.CarePlan, navigator.destination.value)

        navigator.navigate(AppDestination.Decoder)
        assertEquals(AppDestination.Decoder, navigator.current())
    }
}
