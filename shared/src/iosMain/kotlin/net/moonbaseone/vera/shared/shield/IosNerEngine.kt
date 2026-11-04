package net.moonbaseone.vera.shared.shield

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSMakeRange
import platform.NaturalLanguage.NLTagPersonalName
import platform.NaturalLanguage.NLTagSchemeName
import platform.NaturalLanguage.NLTagger
import platform.NaturalLanguage.NLTaggerOptionsOmitPunctuation
import platform.NaturalLanguage.NLTaggerOptionsOmitWhitespace
import platform.NaturalLanguage.NLTokenUnitWord

/**
 * Apple NLTagger personal-name tagging (no Google ML Kit pod required).
 */
@OptIn(ExperimentalForeignApi::class)
class IosNerEngine : NerEngine {
    override suspend fun initialise(): Result<Unit> = Result.success(Unit)

    override suspend fun findRedactionRanges(text: String): List<IntRange> {
        if (text.isBlank()) return emptyList()
        return try {
            val tagger = NLTagger(listOf(NLTagSchemeName))
            tagger.string = text
            val range = NSMakeRange(0u, text.length.toULong())
            val options = NLTaggerOptionsOmitWhitespace or NLTaggerOptionsOmitPunctuation
            val ranges = mutableListOf<IntRange>()
            tagger.enumerateTagsInRange(
                range,
                NLTokenUnitWord,
                NLTagSchemeName,
                options,
            ) { tag, tokenRange, _ ->
                if (tag == NLTagPersonalName) {
                    val start = tokenRange.location.toInt()
                    val endExclusive = start + tokenRange.length.toInt()
                    if (start >= 0 && endExclusive > start && endExclusive <= text.length) {
                        ranges.add(start until endExclusive)
                    }
                }
                true
            }
            ranges
        } catch (_: Exception) {
            emptyList()
        }
    }
}
