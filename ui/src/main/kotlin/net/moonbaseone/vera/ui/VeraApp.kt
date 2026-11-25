package net.moonbaseone.vera.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.appointments.ContactsViewModel
import net.moonbaseone.vera.shared.auth.AuthStep
import net.moonbaseone.vera.shared.auth.AuthViewModel
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.decoder.DecoderViewModel
import net.moonbaseone.vera.shared.symptoms.SymptomsViewModel
import net.moonbaseone.vera.ui.auth.AuthScreen
import net.moonbaseone.vera.ui.shell.AppShell
import org.koin.compose.koinInject

/**
 * @param isDebug enables debug-only tools (e.g. demo dataset picker via long-press on the
 * care-plan title). The Android shell passes `BuildConfig.DEBUG`.
 */
@Composable
fun VeraApp(isDebug: Boolean = false) {
    val authViewModel = koinInject<AuthViewModel>()
    val blueprintViewModel = koinInject<BlueprintViewModel>()
    val decoderViewModel = koinInject<DecoderViewModel>()
    val appointmentsViewModel = koinInject<AppointmentsViewModel>()
    val contactsViewModel = koinInject<ContactsViewModel>()
    val symptomsViewModel = koinInject<SymptomsViewModel>()
    val authState by authViewModel.state.collectAsState()

    when (authState.step) {
        AuthStep.Unlocked -> AppShell(
            blueprintViewModel = blueprintViewModel,
            decoderViewModel = decoderViewModel,
            appointmentsViewModel = appointmentsViewModel,
            contactsViewModel = contactsViewModel,
            symptomsViewModel = symptomsViewModel,
            isDebug = isDebug,
        )
        else -> AuthScreen(viewModel = authViewModel)
    }
}
