package net.moonbaseone.vera.shared.shield

import net.moonbaseone.vera.shared.AppConstants

/**
 * On-device PII redaction before vault storage / guideline lookups.
 *
 * P3: deterministic regex rules (BSN elfproef, postcode, email, phone).
 * ML Kit NER actuals land in a later phase; regex-only matches Flutter web fallback.
 */
class IdentityShieldService {
    /**
     * Strips detected PII from [input]. Empty/blank input is returned unchanged.
     */
    fun shield(input: String): String {
        if (input.trim().isEmpty()) return input
        var result = input
        result = redactBsn(result)
        result = redactPostcode(result)
        result = redactEmail(result)
        result = redactPhone(result)
        return result
    }

    /** Recursively shields string values in nested maps/lists. */
    fun shieldMap(map: Map<String, Any?>): Map<String, Any?> {
        val result = LinkedHashMap<String, Any?>()
        for ((key, value) in map) {
            result[key] = shieldValue(value)
        }
        return result
    }

    private fun shieldValue(value: Any?): Any? = when (value) {
        null -> null
        is String -> shield(value)
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            shieldMap(value as Map<String, Any?>)
        }
        is List<*> -> value.map { shieldValue(it) }
        else -> value
    }

    private fun redactBsn(text: String): String =
        AppConstants.BSN_PATTERN.replace(text) { match ->
            val candidate = match.value
            if (isValidBsn(candidate)) AppConstants.REDACTION_TOKEN else candidate
        }

    private fun redactPostcode(text: String): String =
        text.replace(Regex("""\b\d{4}\s?[A-Z]{2}\b"""), AppConstants.REDACTION_TOKEN)

    private fun redactEmail(text: String): String =
        text.replace(
            Regex("""\b[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}\b"""),
            AppConstants.REDACTION_TOKEN,
        )

    private fun redactPhone(text: String): String =
        text.replace(
            Regex("""(\+31|0031|0)\s?-?(\d[\s\-]?){8,9}\b"""),
            AppConstants.REDACTION_TOKEN,
        )

    /** Dutch BSN 11-proof (elfproef). */
    fun isValidBsn(digits: String): Boolean {
        if (digits.length !in 8..9) return false
        val padded = digits.padStart(9, '0')
        val weights = intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, -1)
        val sum = padded.mapIndexed { i, c -> c.digitToInt() * weights[i] }.sum()
        return sum % 11 == 0
    }
}
