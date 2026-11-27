package net.moonbaseone.vera.shared.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import net.moonbaseone.vera.shared.failure.TranslationFailure
import net.moonbaseone.vera.shared.failure.TranslationModelNotDownloadedFailure

/**
 * Google ML Kit on-device Translate API (Android).
 * Downloads the **Dutch → target** model pair (what Vera actually uses).
 * Model download may use the network once; inference is fully on-device.
 */
class AndroidOnDeviceTranslator : OnDeviceTranslator {
    private val translators = mutableMapOf<String, Translator>()
    private val modelManager = RemoteModelManager.getInstance()

    override suspend fun isAvailable(): Boolean = true

    override suspend fun isModelDownloaded(languageCode: String): Boolean {
        if (languageCode.equals("nl", ignoreCase = true)) return true
        val target = toTranslateLanguage(languageCode) ?: return false
        return try {
            val sourceModel = TranslateRemoteModel.Builder(TranslateLanguage.DUTCH).build()
            val targetModel = TranslateRemoteModel.Builder(target).build()
            modelManager.isModelDownloaded(sourceModel).await() &&
                modelManager.isModelDownloaded(targetModel).await()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun downloadModel(
        languageCode: String,
        onProgress: (Float) -> Unit,
    ): Result<Unit> {
        if (languageCode.equals("nl", ignoreCase = true)) {
            onProgress(1f)
            return Result.success(Unit)
        }
        val target = toTranslateLanguage(languageCode)
            ?: return Result.failure(TranslationFailure("Onbekende taalcode: $languageCode"))
        return try {
            // ML Kit has no granular download progress for translate models — pulse while waiting.
            val pulse = startProgressPulse(onProgress)
            try {
                withTimeout(DOWNLOAD_TIMEOUT_MS) {
                    val translator = translatorFor(TranslateLanguage.DUTCH, target)
                    val conditions = DownloadConditions.Builder().build()
                    // Downloads both source (nl) and target packs for this pair.
                    translator.downloadModelIfNeeded(conditions).await()
                }
            } finally {
                pulse.cancel()
            }
            onProgress(1f)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(
                TranslationFailure(
                    e.message?.takeIf { it.isNotBlank() } ?: "Model download mislukt",
                ),
            )
        }
    }

    override suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
    ): Result<String> {
        if (sourceLanguage == targetLanguage) return Result.success(text)
        val source = toTranslateLanguage(sourceLanguage)
            ?: return Result.failure(TranslationFailure("Onbekende brontaal: $sourceLanguage"))
        val target = toTranslateLanguage(targetLanguage)
            ?: return Result.failure(TranslationFailure("Onbekende doeltaal: $targetLanguage"))

        if (!isModelDownloaded(targetLanguage)) {
            return Result.failure(
                TranslationModelNotDownloadedFailure(
                    "Taalmodel voor \"$targetLanguage\" niet gedownload.",
                ),
            )
        }

        return try {
            val translator = translatorFor(source, target)
            val out = translator.translate(text).await()
            Result.success(out)
        } catch (e: Exception) {
            Result.failure(TranslationFailure(e.message ?: e.toString()))
        }
    }

    private fun startProgressPulse(onProgress: (Float) -> Unit): Job {
        // Indeterminate-ish pulse 0.05 → 0.9 while ML Kit downloads (no real bytes callback).
        return CoroutineScope(Dispatchers.Default).launch {
            var p = 0.05f
            onProgress(p)
            while (isActive) {
                delay(400)
                p = (p + 0.04f).coerceAtMost(0.9f)
                onProgress(p)
            }
        }
    }

    private fun translatorFor(source: String, target: String): Translator {
        val key = "$source→$target"
        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(source)
                .setTargetLanguage(target)
                .build()
            Translation.getClient(options)
        }
    }

    private fun toTranslateLanguage(code: String): String? = when (code.lowercase()) {
        "nl" -> TranslateLanguage.DUTCH
        "en" -> TranslateLanguage.ENGLISH
        "de" -> TranslateLanguage.GERMAN
        "fr" -> TranslateLanguage.FRENCH
        "ar" -> TranslateLanguage.ARABIC
        "tr" -> TranslateLanguage.TURKISH
        "pl" -> TranslateLanguage.POLISH
        "es" -> TranslateLanguage.SPANISH
        "pt" -> TranslateLanguage.PORTUGUESE
        "ru" -> TranslateLanguage.RUSSIAN
        "zh" -> TranslateLanguage.CHINESE
        else -> null
    }

    companion object {
        private const val DOWNLOAD_TIMEOUT_MS = 180_000L
    }
}
