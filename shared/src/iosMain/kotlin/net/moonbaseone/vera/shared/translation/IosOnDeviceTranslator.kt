package net.moonbaseone.vera.shared.translation

import net.moonbaseone.vera.shared.failure.TranslationFailure
import net.moonbaseone.vera.shared.failure.TranslationModelNotDownloadedFailure

/**
 * iOS Shared.framework stub until GoogleMLKit/Translate is linked via CocoaPods.
 * Decoder UI can still show NL glossary; download/translate surface a clear failure.
 */
class IosOnDeviceTranslator : OnDeviceTranslator {
    override suspend fun isAvailable(): Boolean = false

    override suspend fun isModelDownloaded(languageCode: String): Boolean = false

    override suspend fun downloadModel(
        languageCode: String,
        onProgress: (Float) -> Unit,
    ): Result<Unit> = Result.failure(
        TranslationFailure(
            "ML Kit Translate op iOS vereist GoogleMLKit/Translate (CocoaPods). " +
                "Koppel de pod en vervang IosOnDeviceTranslator.",
        ),
    )

    override suspend fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
    ): Result<String> {
        if (sourceLanguage == targetLanguage) return Result.success(text)
        return Result.failure(
            TranslationModelNotDownloadedFailure(
                "Op-apparaat vertaling op iOS is nog niet gekoppeld (CocoaPods ML Kit).",
            ),
        )
    }
}
