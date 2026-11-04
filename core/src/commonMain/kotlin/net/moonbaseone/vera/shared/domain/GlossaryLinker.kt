package net.moonbaseone.vera.shared.domain

/**
 * Links hard medical words in free text to [BuiltInGlossary] entries.
 *
 * Matching is case-insensitive against each entry's [JargonEntry.term] and
 * [JargonEntry.aliases], preferring longer needles so "HER2-positief" wins over "HER2".
 */
object GlossaryLinker {
    data class Span(
        val entry: JargonEntry,
        val matchedText: String,
        val startIndex: Int,
        val endIndex: Int,
    )

    /** Unique glossary entries mentioned in [text], longest match first. */
    fun findEntries(
        text: String,
        glossary: List<JargonEntry> = BuiltInGlossary,
    ): List<JargonEntry> {
        return findSpans(text, glossary)
            .map { it.entry }
            .distinctBy { it.term }
    }

    /** Non-overlapping spans of glossary terms inside [text]. */
    fun findSpans(
        text: String,
        glossary: List<JargonEntry> = BuiltInGlossary,
    ): List<Span> {
        if (text.isBlank()) return emptyList()

        data class Candidate(val entry: JargonEntry, val needle: String)

        val candidates = glossary
            .flatMap { entry ->
                (listOf(entry.term) + entry.aliases)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { Candidate(entry, it) }
            }
            .sortedByDescending { it.needle.length }

        val lower = text.lowercase()
        val occupied = BooleanArray(text.length)
        val spans = mutableListOf<Span>()

        for (candidate in candidates) {
            val needle = candidate.needle.lowercase()
            var from = 0
            while (from <= lower.length - needle.length) {
                val idx = lower.indexOf(needle, from)
                if (idx < 0) break
                val end = idx + needle.length
                val free = (idx until end).none { occupied[it] }
                if (free && isBoundaryOk(lower, idx, end)) {
                    for (i in idx until end) occupied[i] = true
                    spans += Span(
                        entry = candidate.entry,
                        matchedText = text.substring(idx, end),
                        startIndex = idx,
                        endIndex = end,
                    )
                }
                from = idx + 1
            }
        }

        return spans.sortedBy { it.startIndex }
    }

    private fun isBoundaryOk(lower: String, start: Int, end: Int): Boolean {
        val beforeOk = start == 0 || !lower[start - 1].isLetterOrDigit()
        val afterOk = end >= lower.length || !lower[end].isLetterOrDigit()
        return beforeOk && afterOk
    }
}
