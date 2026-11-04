package net.moonbaseone.vera.shared.shield

import net.moonbaseone.vera.shared.AppConstants

/**
 * Optional on-device NER for personal names / addresses beyond regex rules.
 * Android: ML Kit Entity Extraction; iOS: NLTagger.
 */
interface NerEngine {
    suspend fun initialise(): Result<Unit>

    /**
     * Returns spans (start inclusive, end exclusive) of entities to redact
     * in [text] (already regex-scrubbed).
     */
    suspend fun findRedactionRanges(text: String): List<IntRange>
}

class NoOpNerEngine : NerEngine {
    override suspend fun initialise(): Result<Unit> = Result.success(Unit)
    override suspend fun findRedactionRanges(text: String): List<IntRange> = emptyList()
}

/** Applies [ranges] longest-first so indices stay valid while replacing. */
fun applyRedactionRanges(text: String, ranges: List<IntRange>): String {
    if (ranges.isEmpty()) return text
    val sorted = ranges.sortedByDescending { it.first }
    var result = text
    for (range in sorted) {
        if (range.first < 0 || range.last >= result.length) continue
        result = result.replaceRange(range, AppConstants.REDACTION_TOKEN)
    }
    return result
}
