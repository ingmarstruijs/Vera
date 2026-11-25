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

class InMemorySymptomStore : SymptomStore {
    private val _entries = MutableStateFlow<List<SymptomEntry>>(emptyList())
    override val entries: StateFlow<List<SymptomEntry>> = _entries.asStateFlow()

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
}
