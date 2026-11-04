package net.moonbaseone.vera.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class JargonEntryTest {
    @Test
    fun glossary_containsTenEntries() {
        assertEquals(10, BuiltInGlossary.size)
    }

    @Test
    fun allEntries_haveRequiredFields() {
        BuiltInGlossary.forEach { entry ->
            assertTrue(entry.term.isNotEmpty())
            assertTrue(entry.dutchExplanation.isNotEmpty())
            assertTrue(entry.actionItem.isNotEmpty())
            assertTrue(entry.category.isNotEmpty())
        }
    }

    @Test
    fun allEntries_haveFhirCodeOrIknlRef() {
        BuiltInGlossary.forEach { entry ->
            assertTrue(entry.fhirCode != null || entry.iknlRef != null)
        }
    }

    @Test
    fun her2_entry_isPresentWithLoinc() {
        val her2 = BuiltInGlossary.first { it.term.contains("HER2-positief") }
        assertEquals("HER2-positief", her2.term)
        assertTrue(her2.dutchExplanation.contains("HER2", ignoreCase = true))
        assertTrue(her2.fhirCode!!.contains("LOINC"))
        assertEquals("biomarker", her2.category)
    }

    @Test
    fun erNegatief_and_ki67_present() {
        assertTrue(BuiltInGlossary.any { it.term.contains("ER-negatief") })
        val ki67 = BuiltInGlossary.first { it.term.contains("Ki-67") }
        assertTrue(
            ki67.dutchExplanation.contains("snel", ignoreCase = true) ||
                ki67.dutchExplanation.contains("cellen", ignoreCase = true),
        )
    }

    @Test
    fun categories_areValidAndDiverse() {
        val valid = setOf(
            "biomarker", "pathologie", "diagnose", "stadiëring",
            "procedure", "radiotherapie", "cardiologie", "respons",
        )
        BuiltInGlossary.forEach { assertTrue(it.category in valid, it.category) }
        assertTrue(BuiltInGlossary.map { it.category }.toSet().size > 1)
    }

    @Test
    fun iknlRefs_pointToIknl() {
        val withIknl = BuiltInGlossary.filter { it.iknlRef != null }
        assertTrue(withIknl.isNotEmpty())
        withIknl.forEach { assertTrue(it.iknlRef!!.contains("iknl.nl")) }
    }

    @Test
    fun dataClass_equality() {
        assertEquals(BuiltInGlossary[0], BuiltInGlossary[0])
        assertNotEquals(BuiltInGlossary[0], BuiltInGlossary[1])
    }

    @Test
    fun carePathway_categoriesCovered() {
        assertTrue(BuiltInGlossary.any { it.category == "diagnose" })
        assertTrue(BuiltInGlossary.any { it.category == "procedure" })
        assertTrue(BuiltInGlossary.any { it.category == "cardiologie" })
    }
}
