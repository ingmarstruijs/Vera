package net.moonbaseone.vera.shared.symptoms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.moonbaseone.vera.shared.platformCurrentTimeMillis
import kotlin.random.Random

data class SymptomEntry(
    val id: String,
    val text: String,
    val createdAtMillis: Long,
    val audioPath: String? = null,
)

interface SymptomStore {
    val entries: StateFlow<List<SymptomEntry>>
    fun add(
        text: String,
        createdAtMillis: Long = platformCurrentTimeMillis(),
        audioPath: String? = null,
    ): SymptomEntry
    fun delete(id: String)
    fun recent(limit: Int = 10): List<SymptomEntry>
}

class InMemorySymptomStore(
    seedDemoEntries: Boolean = false,
) : SymptomStore {
    private val _entries = MutableStateFlow<List<SymptomEntry>>(emptyList())
    override val entries: StateFlow<List<SymptomEntry>> = _entries.asStateFlow()

    init {
        if (seedDemoEntries) seedDemoEntries()
    }

    override fun add(
        text: String,
        createdAtMillis: Long,
        audioPath: String?,
    ): SymptomEntry {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Symptom text must not be blank" }
        val entry = SymptomEntry(
            id = "sym-${createdAtMillis}-${Random.nextInt(100000, 999999)}",
            text = trimmed,
            createdAtMillis = createdAtMillis,
            audioPath = audioPath,
        )
        _entries.update { listOf(entry) + it }
        return entry
    }

    override fun delete(id: String) {
        _entries.update { list -> list.filterNot { it.id == id } }
    }

    override fun recent(limit: Int): List<SymptomEntry> =
        _entries.value.take(limit.coerceAtLeast(0))

    /** Demo utterances for emulator / datasets where on-device STT is unavailable. */
    private fun seedDemoEntries() {
        val now = platformCurrentTimeMillis()
        val dayMs = 24L * 60L * 60L * 1000L
        val hourMs = 60L * 60L * 1000L
        val samples = listOf(
            Triple(
                "Sinds gisteren voel ik me duidelijk vermoeider dan normaal. " +
                    "’s Ochtends gaat het nog redelijk, maar na de lunch zakt de energie weg " +
                    "en moet ik vaak een uurtje liggen. Traplopen kost meer moeite en ik " +
                    "raak sneller kortademig bij boodschappen. Geen koorts gemeten.",
                0L,
                10L,
            ),
            Triple(
                "Na de laatste trastuzumab-infuus had ik lichte misselijkheid tot de avond. " +
                    "Geen braken, wel weinig trek en een metalige smaak. Ik heb water en " +
                    "beschuit genomen; om ongeveer 21:00 was het grotendeels weg. " +
                    "Geen huiduitslag of jeuk gezien.",
                1L,
                18L,
            ),
            Triple(
                "In de vingertoppen van beide handen voel ik tintelingen, vooral ’s nachts " +
                    "en als ik iets kouds uit de koelkast pak. Fijne knoopjes dichtmaken " +
                    "gaat lastiger. De tenen doen soms mee, maar minder heftig. " +
                    "Geen krachtsverlies in de armen gemerkt.",
                2L,
                22L,
            ),
            Triple(
                "Eetlust is sinds vorige week minder: ik sla het ontbijt vaak over en " +
                    "eet ’s avonds kleinere porties. Gewicht lijkt iets gedaald (nog niet " +
                    "gewogen). Geen aanhoudende buikpijn, wel af en toe een vol gevoel. " +
                    "Ik drink wel genoeg water; cafeïne beperkt tot één kop thee.",
                3L,
                8L,
            ),
        )
        // Oldest first via add-prepend → final order is newest-first.
        samples.asReversed().forEach { (text, daysAgo, hourOfDay) ->
            // Anchor roughly at local midday then shift to a stable demo hour offset.
            val created = now - daysAgo * dayMs - (12L - hourOfDay) * hourMs
            add(text = text, createdAtMillis = created)
        }
    }
}
