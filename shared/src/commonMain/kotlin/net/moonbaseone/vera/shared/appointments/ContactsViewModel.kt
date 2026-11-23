package net.moonbaseone.vera.shared.appointments

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.careplan.LocalCarePlanRepository
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.ui.UiCopy

data class ContactsUiState(
    val isLoading: Boolean = true,
    val contacts: List<CareContact> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Loads care-team phone contacts (GP, desk, appointment practitioners).
 * Same [CareContact] model and builder as [AppointmentsViewModel].
 */
class ContactsViewModel(
    private val carePlanRepository: CarePlanRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    constructor(
        mockData: MockDataService,
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(LocalCarePlanRepository(mockData), scope)

    private val _state = MutableStateFlow(ContactsUiState())
    val state: StateFlow<ContactsUiState> = _state.asStateFlow()

    fun load() {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val summaries = carePlanRepository.fetchAppointments().map { it.toSummary() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        contacts = buildCareContacts(summaries),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: UiCopy.APPOINTMENTS_NO_DATA,
                    )
                }
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}

/** Shared builder for GP / oncology desk / appointment-derived telecom contacts. */
fun buildCareContacts(appointments: List<AppointmentSummary>): List<CareContact> {
    val fromAppointments = appointments
        .filter { it.practitioner.isNotBlank() || it.phone.isNotBlank() }
        .map {
            CareContact(
                id = "appt-contact-${it.id}",
                name = it.practitioner.ifBlank { it.serviceType },
                role = it.serviceType,
                organization = it.location,
                phone = it.phone,
            )
        }
    val gp = CareContact(
        id = "contact-gp",
        name = "Huisartsenpraktijk Utrecht Noord",
        role = "Huisarts",
        organization = "Eerste lijn",
        phone = "030 123 4567",
    )
    val oncologyDesk = CareContact(
        id = "contact-oncology-desk",
        name = "Polikliniek Oncologie",
        role = "Afdelingstelefoon",
        organization = "UMC Utrecht – Afdeling Oncologie",
        phone = "088 755 6300",
    )
    return (listOf(gp, oncologyDesk) + fromAppointments)
        .distinctBy { "${it.name}|${it.phone}" }
}
