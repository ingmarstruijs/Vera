package net.moonbaseone.vera.shared.translation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.moonbaseone.vera.shared.appointments.AppointmentSummary
import net.moonbaseone.vera.shared.appointments.CareContact
import net.moonbaseone.vera.shared.domain.TimelineEvent
import net.moonbaseone.vera.shared.i18n.StringCatalog

data class ContentLocalizationStatus(
    /** Number of in-flight localize jobs (care plan / appointments / contacts / …). */
    val activeCount: Int = 0,
    /** Combined 0f..1f progress for the busiest job; null when idle. */
    val progress: Float? = null,
) {
    val isActive: Boolean get() = activeCount > 0
}

/**
 * Translates Dutch mock/clinical dataset strings into the active app UI language.
 * Source of truth stays NL (as a care team would write); patients see the active locale.
 */
class DatasetContentLocalizer(
    private val translation: LocalTranslationService,
) {
    private val statusMutex = Mutex()
    private val _status = MutableStateFlow(ContentLocalizationStatus())
    val status: StateFlow<ContentLocalizationStatus> = _status.asStateFlow()

    fun targetLanguage(): String = StringCatalog.activeLocale.tag

    /** Ensures the on-device model is ready; false if translation cannot run. */
    suspend fun ensureReady(
        targetLanguage: String,
        onProgress: (Float) -> Unit = {},
    ): Boolean {
        if (targetLanguage == "nl") return true
        if (!translation.isAvailable()) return false
        if (translation.isModelDownloaded(targetLanguage)) return true
        return translation.downloadModel(targetLanguage, onProgress).isSuccess
    }

    suspend fun text(value: String, targetLanguage: String): String {
        if (targetLanguage == "nl" || value.isBlank()) return value
        return translation.translate(value, targetLanguage).text ?: value
    }

    suspend fun texts(values: List<String>, targetLanguage: String): List<String> =
        values.map { text(it, targetLanguage) }

    suspend fun localizeEvent(
        event: TimelineEvent,
        targetLanguage: String,
    ): TimelineEvent {
        if (targetLanguage == "nl") return event
        return event.copy(
            title = text(event.title, targetLanguage),
            shortTitle = event.shortTitle?.let { text(it, targetLanguage) },
            description = text(event.description, targetLanguage),
            patientInstruction = event.patientInstruction?.let { text(it, targetLanguage) },
            nextSteps = event.nextSteps?.let { text(it, targetLanguage) },
            actionItems = texts(event.actionItems, targetLanguage),
        )
    }

    suspend fun localizeEvents(
        events: List<TimelineEvent>,
        targetLanguage: String,
    ): List<TimelineEvent> {
        if (targetLanguage == "nl") return events
        return tracked { report ->
            if (!ensureReady(targetLanguage) { report(it * 0.2f) }) return@tracked events
            if (events.isEmpty()) {
                report(1f)
                return@tracked events
            }
            events.mapIndexed { index, event ->
                val localized = localizeEvent(event, targetLanguage)
                report(0.2f + 0.8f * (index + 1).toFloat() / events.size.toFloat())
                localized
            }
        }
    }

    suspend fun localizeAppointment(
        appointment: AppointmentSummary,
        targetLanguage: String,
    ): AppointmentSummary {
        if (targetLanguage == "nl") return appointment
        return appointment.copy(
            serviceType = text(appointment.serviceType, targetLanguage),
            location = text(appointment.location, targetLanguage),
            reason = text(appointment.reason, targetLanguage),
            comment = text(appointment.comment, targetLanguage),
            // Keep practitioner display names untranslated.
        )
    }

    suspend fun localizeAppointments(
        appointments: List<AppointmentSummary>,
        targetLanguage: String,
    ): List<AppointmentSummary> {
        if (targetLanguage == "nl") return appointments
        return tracked { report ->
            if (!ensureReady(targetLanguage) { report(it * 0.2f) }) return@tracked appointments
            if (appointments.isEmpty()) {
                report(1f)
                return@tracked appointments
            }
            appointments.mapIndexed { index, appointment ->
                val localized = localizeAppointment(appointment, targetLanguage)
                report(0.2f + 0.8f * (index + 1).toFloat() / appointments.size.toFloat())
                localized
            }
        }
    }

    suspend fun localizeContact(
        contact: CareContact,
        targetLanguage: String,
    ): CareContact {
        if (targetLanguage == "nl") return contact
        return contact.copy(
            role = text(contact.role, targetLanguage),
            organization = text(contact.organization, targetLanguage),
            // Translate institutional names that double as "name" for desk/GP entries.
            name = when {
                contact.id.startsWith("appt-contact-") -> contact.name
                else -> text(contact.name, targetLanguage)
            },
        )
    }

    suspend fun localizeContacts(
        contacts: List<CareContact>,
        targetLanguage: String,
    ): List<CareContact> {
        if (targetLanguage == "nl") return contacts
        return tracked { report ->
            if (!ensureReady(targetLanguage) { report(it * 0.2f) }) return@tracked contacts
            if (contacts.isEmpty()) {
                report(1f)
                return@tracked contacts
            }
            contacts.mapIndexed { index, contact ->
                val localized = localizeContact(contact, targetLanguage)
                report(0.2f + 0.8f * (index + 1).toFloat() / contacts.size.toFloat())
                localized
            }
        }
    }

    private suspend fun <T> tracked(block: suspend (report: (Float) -> Unit) -> T): T {
        statusMutex.withLock {
            _status.update { it.copy(activeCount = it.activeCount + 1, progress = it.progress ?: 0f) }
        }
        try {
            return block { progress ->
                _status.update { current ->
                    current.copy(progress = progress.coerceIn(0f, 1f))
                }
            }
        } finally {
            statusMutex.withLock {
                _status.update { current ->
                    val next = (current.activeCount - 1).coerceAtLeast(0)
                    ContentLocalizationStatus(
                        activeCount = next,
                        progress = if (next == 0) null else current.progress,
                    )
                }
            }
        }
    }
}
