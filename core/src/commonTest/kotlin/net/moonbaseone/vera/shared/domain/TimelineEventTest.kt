package net.moonbaseone.vera.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimelineEventTest {
    @Test
    fun fromCarePlanActivities_mapsPhases() {
        val activities = listOf(
            mapOf(
                "detail" to mapOf(
                    "kind" to "ServiceRequest",
                    "code" to mapOf("text" to "Chemo – VOLTOOID"),
                    "status" to "completed",
                    "scheduledPeriod" to mapOf("start" to "2024-12-20"),
                    "description" to "Done",
                ),
            ),
            mapOf(
                "detail" to mapOf(
                    "kind" to "ServiceRequest",
                    "code" to mapOf("text" to "Radio – ACTIEF"),
                    "status" to "in-progress",
                    "scheduledPeriod" to mapOf("start" to "2025-03-01"),
                ),
            ),
            mapOf(
                "detail" to mapOf(
                    "kind" to "ServiceRequest",
                    "code" to mapOf("text" to "Follow-up – GEPLAND"),
                    "status" to "scheduled",
                    "scheduledPeriod" to mapOf("start" to "2026-06-01"),
                ),
            ),
        )

        val events = TimelineEvent.fromCarePlanActivities(activities)
        assertEquals(3, events.size)
        assertEquals(TimelinePhase.Completed, events[0].phase)
        assertEquals(TimelinePhase.Current, events[1].phase)
        assertEquals(TimelinePhase.Future, events[2].phase)
        assertEquals("Chemo", events[0].title)
        assertEquals("2024-12-20", events[0].dateIso)
    }

    @Test
    fun fromCarePlanActivities_mapsRichFields() {
        val activities = listOf(
            mapOf(
                "detail" to mapOf(
                    "kind" to "ServiceRequest",
                    "code" to mapOf("text" to "Radio – ACTIEF"),
                    "status" to "in-progress",
                    "scheduledPeriod" to mapOf("start" to "2025-03-01"),
                    "description" to "Hypofractionering na BCS.",
                    "patientInstruction" to "Noteer huidreacties.",
                    "nextStep" to "Nacontrole na laatste fractie.",
                    "actionItem" to listOf("Vraag hoeveel fracties u nog heeft."),
                    "iknlGuidelineRef" to "https://www.iknl.nl/richtlijnen/borstkanker",
                ),
            ),
        )

        val event = TimelineEvent.fromCarePlanActivities(activities).single()
        assertEquals("Hypofractionering na BCS.", event.description)
        assertEquals("Noteer huidreacties.", event.patientInstruction)
        assertEquals("Nacontrole na laatste fractie.", event.nextSteps)
        assertEquals(listOf("Vraag hoeveel fracties u nog heeft."), event.actionItems)
        assertEquals("https://www.iknl.nl/richtlijnen/borstkanker", event.iknlGuidelineRef)
        assertTrue(event.glossaryHaystack().contains("Hypofractionering"))
        assertTrue(event.glossaryHaystack().contains("huidreacties"))
    }
}
