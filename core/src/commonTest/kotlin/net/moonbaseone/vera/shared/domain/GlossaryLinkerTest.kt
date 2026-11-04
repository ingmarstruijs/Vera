package net.moonbaseone.vera.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlossaryLinkerTest {
    @Test
    fun findEntries_matchesAliasesInCarePlanText() {
        val text =
            "Adjuvante radiotherapie na BCS volgens hypofractionering. " +
                "LVEF-controle tijdens trastuzumab; SLNB was pN0."
        val entries = GlossaryLinker.findEntries(text)
        val terms = entries.map { it.term }.toSet()
        assertTrue(terms.contains("Borstsparende chirurgie (BCS)"))
        assertTrue(terms.contains("Hypofractionering"))
        assertTrue(terms.contains("LVEF (linker ventrikel ejectiefractie)"))
        assertTrue(terms.contains("Schildwachtklierprocedure (SLNB)"))
        assertTrue(terms.contains("HER2-positief")) // via trastuzumab alias
    }

    @Test
    fun findEntries_matchesPcrAndHer2Positief() {
        val text = "HER2-positief invasief ductaal carcinoom met pCR in de axilla."
        val terms = GlossaryLinker.findEntries(text).map { it.term }.toSet()
        assertTrue(terms.contains("HER2-positief"))
        assertTrue(terms.contains("Invasief ductaal carcinoom"))
        assertTrue(terms.contains("pCR (pathologisch complete respons)"))
    }

    @Test
    fun findSpans_prefersLongerOverlap() {
        val text = "Uw uitslag is HER2-positief volgens pathologie."
        val spans = GlossaryLinker.findSpans(text)
        val her2 = spans.filter { it.entry.term == "HER2-positief" }
        assertEquals(1, her2.size)
        assertEquals("HER2-positief", her2.first().matchedText)
        // Shorter alias "HER2" must not also occupy the same range.
        assertTrue(spans.none { it.matchedText.equals("HER2", ignoreCase = true) })
    }

    @Test
    fun findEntries_emptyForBlank() {
        assertTrue(GlossaryLinker.findEntries("").isEmpty())
        assertTrue(GlossaryLinker.findEntries("   ").isEmpty())
    }

    @Test
    fun findSpans_respectsWordBoundaries() {
        // "ER" alone should not match inside unrelated words; alias is oestrogeenreceptor / ER-negatief
        val spans = GlossaryLinker.findSpans("De server status is online.")
        assertTrue(spans.isEmpty())
    }
}
