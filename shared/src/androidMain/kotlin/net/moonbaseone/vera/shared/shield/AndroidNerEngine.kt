package net.moonbaseone.vera.shared.shield

import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractor
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import kotlinx.coroutines.tasks.await
import net.moonbaseone.vera.shared.failure.NerInitFailure

/**
 * ML Kit Entity Extraction (Dutch) for person / address / phone-style entities.
 */
class AndroidNerEngine : NerEngine {
    private var extractor: EntityExtractor? = null

    override suspend fun initialise(): Result<Unit> {
        if (extractor != null) return Result.success(Unit)
        return try {
            extractor = EntityExtraction.getClient(
                EntityExtractorOptions.Builder(EntityExtractorOptions.DUTCH).build(),
            )
            // Download model if needed (network once).
            extractor!!.downloadModelIfNeeded().await()
            Result.success(Unit)
        } catch (e: Exception) {
            extractor = null
            Result.failure(NerInitFailure(e.message ?: e.toString()))
        }
    }

    override suspend fun findRedactionRanges(text: String): List<IntRange> {
        val client = extractor ?: return emptyList()
        return try {
            val annotations = client.annotate(text).await()
            annotations.mapNotNull { annotation ->
                val shouldRedact = annotation.entities.any { entity ->
                    entity.type == Entity.TYPE_ADDRESS ||
                        entity.type == Entity.TYPE_PHONE ||
                        entity.type == Entity.TYPE_EMAIL ||
                        entity.type == Entity.TYPE_DATE_TIME
                }
                // Person names: ML Kit may surface as TYPE_TRACKING_NUMBER-less;
                // also redact any annotation whose raw text looks like a Dutch name prefix.
                val looksLikeName = annotation.annotatedText.matches(
                    Regex("""(?i)\b(van|de|den|der|ten|ter)\s+[A-ZÁÉÍÓÚÀÈ][\p{L}'\-]+.*"""),
                )
                if (shouldRedact || looksLikeName) {
                    annotation.start until annotation.end
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
