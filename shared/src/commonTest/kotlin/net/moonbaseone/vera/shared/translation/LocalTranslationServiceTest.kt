package net.moonbaseone.vera.shared.translation

import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.AppConstants
import net.moonbaseone.vera.shared.failure.TranslationModelNotDownloadedFailure
import net.moonbaseone.vera.shared.shield.IdentityShieldService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalTranslationServiceTest {
    private fun service(
        translator: FakeOnDeviceTranslator = FakeOnDeviceTranslator(),
    ) = LocalTranslationService(
        engine = translator,
        shield = IdentityShieldService(),
    )

    @Test
    fun translate_sameLanguage_passthrough() = runBlocking {
        val result = service().translate("Hello", targetLanguage = "nl", sourceLanguage = "nl")
        assertEquals("Hello", result.text)
        assertNull(result.failure)
    }

    @Test
    fun translate_shieldsPiiBeforeEngine() = runBlocking {
        val translator = FakeOnDeviceTranslator()
        val result = service(translator).translate(
            text = "Mail patient@example.com about HER2",
            targetLanguage = "en",
        )
        assertNotNull(result.text)
        assertTrue(result.text!!.contains(AppConstants.REDACTION_TOKEN))
        assertTrue(translator.translateCalls == 1)
        assertTrue(!result.text!!.contains("patient@example.com"))
    }

    @Test
    fun translate_missingModel_fails() = runBlocking {
        val translator = FakeOnDeviceTranslator(downloaded = mutableSetOf())
        val result = service(translator).translate("tekst", targetLanguage = "en")
        assertNull(result.text)
        assertTrue(result.failure is TranslationModelNotDownloadedFailure)
    }

    @Test
    fun downloadModel_thenTranslate() = runBlocking {
        val translator = FakeOnDeviceTranslator(downloaded = mutableSetOf())
        val svc = service(translator)
        val download = svc.downloadModel("en")
        assertTrue(download.isSuccess)
        val result = svc.translate("Ki-67 hoog", targetLanguage = "en")
        assertNotNull(result.text)
        assertTrue(result.text!!.startsWith("[\$]en "))
    }

    @Test
    fun translateBatch_preservesOrder() = runBlocking {
        val out = service().translateBatch(listOf("a", "b"), targetLanguage = "en")
        assertEquals(2, out.size)
        assertTrue(out[0].contains("a"))
        assertTrue(out[1].contains("b"))
    }
}
