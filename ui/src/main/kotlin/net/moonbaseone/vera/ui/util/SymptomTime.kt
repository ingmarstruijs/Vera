package net.moonbaseone.vera.ui.util

import net.moonbaseone.vera.shared.util.DateDisplay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Formats a symptom [createdAtMillis] as local `22 apr 2025 · 09:00`. */
fun formatSymptomWhen(createdAtMillis: Long): String {
    if (createdAtMillis <= 0L) return ""
    val iso = Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
    return DateDisplay.formatIso(iso)
}
