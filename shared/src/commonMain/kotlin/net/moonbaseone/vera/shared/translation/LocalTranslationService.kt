package net.moonbaseone.vera.shared.translation

import net.moonbaseone.vera.shared.failure.TranslationFailure
import net.moonbaseone.vera.shared.failure.TranslationModelNotDownloadedFailure
import net.moonbaseone.vera.shared.platformCurrentTimeMillis
import net.moonbaseone.vera.shared.shield.IdentityShieldService

data class TranslationResult(
    val text: String? = null,
    val failure: Exception? = null,
)

/**
 * Privacy-safe translation: Identity Shield → on-device [OnDeviceTranslator].
 * Never sends medical text to a remote API.
 */
class LocalTranslationService(
    private val engine: OnDeviceTranslator,
    private val shield: IdentityShieldService,
    private val sourceLanguage: String = "nl",
) {
    private val cache = mutableMapOf<String, Pair<String, Long>>()
    private val cacheTtlMs = 60L * 60L * 1000L

    suspend fun isAvailable(): Boolean = engine.isAvailable()

    suspend fun isModelDownloaded(languageCode: String): Boolean =
        languageCode == sourceLanguage || engine.isModelDownloaded(languageCode)

    suspend fun downloadModel(
        languageCode: String,
        onProgress: (Float) -> Unit = {},
    ): Result<Unit> = engine.downloadModel(languageCode, onProgress)

    suspend fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String = this.sourceLanguage,
    ): TranslationResult {
        if (text.isBlank()) return TranslationResult(text = text)
        if (sourceLanguage == targetLanguage) return TranslationResult(text = text)

        if (!engine.isAvailable()) {
            return TranslationResult(
                failure = TranslationFailure(
                    "Op-apparaat vertaling is alleen beschikbaar op de mobiele app.",
                ),
            )
        }

        val cacheKey = "$sourceLanguage:$targetLanguage:${text.hashCode()}"
        val cached = cache[cacheKey]
        val now = platformCurrentTimeMillis()
        if (cached != null && now - cached.second < cacheTtlMs) {
            return TranslationResult(text = cached.first)
        }

        if (!engine.isModelDownloaded(targetLanguage)) {
            return TranslationResult(
                failure = TranslationModelNotDownloadedFailure(
                    "Taalmodel voor \"$targetLanguage\" niet gedownload.",
                ),
            )
        }

        return try {
            val shielded = shield.shieldWithNer(text)
            val result = engine.translate(shielded, sourceLanguage, targetLanguage)
            result.fold(
                onSuccess = { translated ->
                    cache[cacheKey] = translated to now
                    if (cache.size > 200) {
                        val cutoff = now - cacheTtlMs
                        cache.entries.removeAll { it.value.second < cutoff }
                    }
                    TranslationResult(text = translated)
                },
                onFailure = { TranslationResult(failure = it as? Exception ?: TranslationFailure(it.message ?: "")) },
            )
        } catch (e: Exception) {
            TranslationResult(failure = TranslationFailure(e.message ?: e.toString()))
        }
    }

    suspend fun translateBatch(
        texts: List<String>,
        targetLanguage: String,
    ): List<String> = texts.map { text ->
        translate(text, targetLanguage).text ?: text
    }
}
