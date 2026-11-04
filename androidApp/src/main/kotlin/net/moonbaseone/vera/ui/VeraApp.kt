package net.moonbaseone.vera.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.moonbaseone.vera.shared.auth.AuthStep
import net.moonbaseone.vera.shared.auth.AuthViewModel
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.ui.auth.AuthScreen
import net.moonbaseone.vera.ui.shell.AppShell
import org.koin.compose.koinInject

@Composable
fun VeraApp() {
    val authViewModel = koinInject<AuthViewModel>()
    val blueprintViewModel = koinInject<BlueprintViewModel>()
    val authState by authViewModel.state.collectAsState()

    when (authState.step) {
        AuthStep.Unlocked -> AppShell(blueprintViewModel = blueprintViewModel)
        else -> AuthScreen(viewModel = authViewModel)
    }
}
