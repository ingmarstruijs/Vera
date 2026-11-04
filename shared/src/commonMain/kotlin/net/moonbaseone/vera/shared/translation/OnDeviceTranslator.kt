package net.moonbaseone.vera.shared.translation

import net.moonbaseone.vera.shared.failure.TranslationFailure
import net.moonbaseone.vera.shared.failure.TranslationModelNotDownloadedFailure

/**
 * Platform on-device translation engine (ML Kit on Android; stub / Apple bridge on iOS).
 * Medical text must be PII-shielded by [LocalTranslationService] before calling [translate].
 */
interface OnDeviceTranslator {
    /** True when this platform can run on-device translation. */
    suspend fun isAvailable(): Boolean

    suspend fun isModelDownloaded(languageCode: String): Boolean

    /**
     * Downloads the remote language model. [onProgress] receives 0f..1f.
     * May require network once per language; translated medical text never leaves the device.
     */
    suspend fun downloadModel(
        languageCode: String,
        onProgress: (Float) -> Unit = {},
    ): Result<Unit>

    suspend fun translate(
        text: String,
        sourceLanguage: String = "nl",
        targetLanguage: String,
    ): Result<String>
}

/**
 * Test double — optional instant "models" and passthrough/fixed translations.
 */
class FakeOnDeviceTranslator(
    private val available: Boolean = true,
    private val downloaded: MutableSet<String> = mutableSetOf("en", "de", "fr"),
    private val translationPrefix: String = "[$]",
) : OnDeviceTranslator {
    var translateCalls: Int = 0
        private set
    var downloadCalls: Int = 0
        private set

    override suspend fun isAvailable(): Boolean = available

    override suspend fun isModelDownloaded(languageCode: String): Boolean =
        languageCode in downloaded

    override suspend fun downloadModel(
        languageCode: String,
        onProgress: (Float) -> Unit,
    ): Result<Unit> {
        downloadCalls++
        onProgress(0.5f)
        downloaded.add(languageCode)
        onProgress(1f)
        return Result.success(Unit)
    }

    override suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
    ): Result<String> {
        translateCalls++
        if (!available) {
            return Result.failure(TranslationFailure("Vertaling niet beschikbaar op dit platform."))
        }
        if (targetLanguage !in downloaded) {
            return Result.failure(
                TranslationModelNotDownloadedFailure(
                    "Taalmodel voor \"$targetLanguage\" niet gedownload.",
                ),
            )
        }
        if (sourceLanguage == targetLanguage) return Result.success(text)
        return Result.success("$translationPrefix$targetLanguage $text")
    }
}
