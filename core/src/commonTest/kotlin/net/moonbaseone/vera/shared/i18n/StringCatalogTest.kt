package net.moonbaseone.vera.shared.i18n

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringCatalogTest {
    @AfterTest
    fun resetLocale() {
        StringCatalog.setLocale(AppLocale.NL)
    }

    @Test
    fun defaultLocale_isDutch() {
        assertEquals(AppLocale.NL, StringCatalog.activeLocale)
        assertEquals("Zorgplan", StringCatalog.current.navCarePlan)
    }

    @Test
    fun packs_coverEnNlDeFr() {
        AppLocale.entries.forEach { locale ->
            val pack = StringCatalog.pack(locale)
            assertEquals(locale, pack.locale)
            assertTrue(pack.appName.isNotBlank())
            assertTrue(pack.navCarePlan.isNotBlank())
            assertEquals(5, pack.appointmentsDefaultQuestions.size)
        }
    }

    @Test
    fun setLocale_switchesActivePack() {
        StringCatalog.setLocale(AppLocale.EN)
        assertEquals("Care plan", StringCatalog.current.navCarePlan)
        StringCatalog.setLocale(AppLocale.DE)
        assertEquals("Versorgungsplan", StringCatalog.current.navCarePlan)
        StringCatalog.setLocale(AppLocale.FR)
        assertEquals("Plan de soins", StringCatalog.current.navCarePlan)
    }

    @Test
    fun fromTag_parsesBcp47Prefix() {
        assertEquals(AppLocale.EN, AppLocale.fromTag("en-GB"))
        assertEquals(AppLocale.NL, AppLocale.fromTag("nl-NL"))
        assertEquals(AppLocale.NL, AppLocale.fromTag(null))
    }
}
