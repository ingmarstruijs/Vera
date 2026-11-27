package net.moonbaseone.vera.shared.pocket

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.moonbaseone.vera.shared.platformCurrentTimeMillis

data class PocketGuide(
    val appointmentId: String,
    val updatedAtMillis: Long,
    val symptomEntryIds: List<String> = emptyList(),
    val linkedActionItemIds: List<String> = emptyList(),
    /** Human-readable linked action texts (denormalized for display). */
    val linkedActionTexts: List<String> = emptyList(),
    val notes: String = "",
    val recordingPath: String? = null,
)

interface PocketGuideStore {
    val guides: StateFlow<Map<String, PocketGuide>>
    fun get(appointmentId: String): PocketGuide?
    fun upsert(guide: PocketGuide)
    fun ensure(appointmentId: String): PocketGuide
    fun linkActionItem(appointmentId: String, actionId: String, actionText: String)
    fun unlinkActionItem(appointmentId: String, actionId: String)
    fun addManualQuestion(appointmentId: String, text: String)
    fun setRecordingPath(appointmentId: String, path: String?)
    fun hasGuide(appointmentId: String): Boolean
    fun hasRecording(appointmentId: String): Boolean
}

class InMemoryPocketGuideStore : PocketGuideStore {
    private val _guides = MutableStateFlow<Map<String, PocketGuide>>(emptyMap())
    override val guides: StateFlow<Map<String, PocketGuide>> = _guides.asStateFlow()

    override fun get(appointmentId: String): PocketGuide? = _guides.value[appointmentId]

    override fun upsert(guide: PocketGuide) {
        _guides.update { it + (guide.appointmentId to guide) }
    }

    override fun ensure(appointmentId: String): PocketGuide {
        get(appointmentId)?.let { return it }
        val created = PocketGuide(
            appointmentId = appointmentId,
            updatedAtMillis = platformCurrentTimeMillis(),
        )
        upsert(created)
        return created
    }

    override fun linkActionItem(appointmentId: String, actionId: String, actionText: String) {
        val current = ensure(appointmentId)
        val trimmed = actionText.trim()
        if (trimmed.isEmpty()) return
        if (actionId in current.linkedActionItemIds) return
        upsert(
            current.copy(
                linkedActionItemIds = current.linkedActionItemIds + actionId,
                linkedActionTexts = current.linkedActionTexts + trimmed,
                updatedAtMillis = platformCurrentTimeMillis(),
            ),
        )
    }

    override fun unlinkActionItem(appointmentId: String, actionId: String) {
        val current = get(appointmentId) ?: return
        val idx = current.linkedActionItemIds.indexOf(actionId)
        if (idx < 0) return
        upsert(
            current.copy(
                linkedActionItemIds = current.linkedActionItemIds.filterIndexed { i, _ -> i != idx },
                linkedActionTexts = current.linkedActionTexts.filterIndexed { i, _ -> i != idx },
                updatedAtMillis = platformCurrentTimeMillis(),
            ),
        )
    }

    override fun addManualQuestion(appointmentId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        linkActionItem(
            appointmentId = appointmentId,
            actionId = actionItemId("manual", "${platformCurrentTimeMillis()}:$trimmed"),
            actionText = trimmed,
        )
    }

    override fun setRecordingPath(appointmentId: String, path: String?) {
        val current = ensure(appointmentId)
        upsert(
            current.copy(
                recordingPath = path,
                updatedAtMillis = platformCurrentTimeMillis(),
            ),
        )
    }

    override fun hasGuide(appointmentId: String): Boolean = _guides.value.containsKey(appointmentId)

    override fun hasRecording(appointmentId: String): Boolean =
        !_guides.value[appointmentId]?.recordingPath.isNullOrBlank()
}

/** Stable id for a care-plan / decoder action item linked into a pocket guide. */
fun actionItemId(source: String, text: String): String =
    "$source:${text.hashCode()}"
