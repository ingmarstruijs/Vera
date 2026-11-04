package net.moonbaseone.vera.shared.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import net.moonbaseone.vera.shared.failure.TranslationFailure
import net.moonbaseone.vera.shared.failure.TranslationModelNotDownloadedFailure

/**
 * Google ML Kit on-device Translate API (Android).
 * Model download may use the network once; inference is fully on-device.
 */
class AndroidOnDeviceTranslator : OnDeviceTranslator {
    private val translators = mutableMapOf<String, Translator>()
    private val modelManager = RemoteModelManager.getInstance()

    override suspend fun isAvailable(): Boolean = true

    override suspend fun isModelDownloaded(languageCode: String): Boolean {
        val lang = toTranslateLanguage(languageCode) ?: return false
        return try {
            val model = TranslateRemoteModel.Builder(lang).build()
            modelManager.isModelDownloaded(model).await()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun downloadModel(
        languageCode: String,
        onProgress: (Float) -> Unit,
    ): Result<Unit> {
        val lang = toTranslateLanguage(languageCode)
            ?: return Result.failure(TranslationFailure("Onbekende taalcode: $languageCode"))
        return try {
            onProgress(0.1f)
            val model = TranslateRemoteModel.Builder(lang).build()
            val conditions = DownloadConditions.Builder().build()
            modelManager.download(model, conditions).await()
            onProgress(1f)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(TranslationFailure("Model download mislukt: ${e.message}"))
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
            // Ensure model ready (no-op if already downloaded).
            translator.downloadModelIfNeeded().await()
            val out = translator.translate(text).await()
            Result.success(out)
        } catch (e: Exception) {
            Result.failure(TranslationFailure(e.message ?: e.toString()))
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
}
