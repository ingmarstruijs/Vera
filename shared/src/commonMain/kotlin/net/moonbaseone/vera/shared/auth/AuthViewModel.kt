package net.moonbaseone.vera.shared.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.moonbaseone.vera.shared.AppConstants
import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import net.moonbaseone.vera.shared.failure.AuthMaxAttemptsFailure
import net.moonbaseone.vera.shared.failure.VaultSelfDestructFailure
import net.moonbaseone.vera.shared.ui.UiCopy
import net.moonbaseone.vera.shared.vault.LocalVaultService

enum class AuthStep {
    Checking,
    NewVault,
    Unlock,
    Unlocked,
}

data class AuthUiState(
    val step: AuthStep = AuthStep.Checking,
    val isBusy: Boolean = false,
    val attemptsLeft: Int = AppConstants.MAX_FAILED_AUTH_ATTEMPTS,
    val errorMessage: String? = null,
    val showSelfDestructDialog: Boolean = false,
)

/**
 * Shared auth coordinator: vault bootstrap → biometric unlock → unlocked shell.
 * Platforms collect [state] and call the public actions; no UI widgets here.
 */
class AuthViewModel(
    private val vault: LocalVaultService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun start(autoUnlock: Boolean = true) {
        scope.launch {
            _state.update { it.copy(step = AuthStep.Checking, errorMessage = null) }
            val initialised = vault.isVaultInitialised()
            if (!initialised) {
                _state.update { it.copy(step = AuthStep.NewVault, isBusy = false) }
                return@launch
            }
            val remaining = vault.remainingAttempts()
            _state.update {
                it.copy(
                    step = AuthStep.Unlock,
                    attemptsLeft = remaining,
                    isBusy = false,
                )
            }
            if (autoUnlock && remaining > 0) {
                unlockBiometric()
            }
        }
    }

    fun createVault() {
        scope.launch {
            _state.update { it.copy(isBusy = true, errorMessage = null) }
            vault.createVault()
            _state.update {
                it.copy(
                    step = AuthStep.Unlock,
                    isBusy = false,
                    attemptsLeft = AppConstants.MAX_FAILED_AUTH_ATTEMPTS,
                )
            }
            unlockBiometric()
        }
    }

    fun unlockBiometric() {
        scope.launch {
            val current = _state.value
            if (current.isBusy || current.attemptsLeft <= 0) return@launch
            _state.update { it.copy(isBusy = true, errorMessage = null) }

            val result = vault.unlockWithBiometrics()
            if (result.success) {
                _state.update {
                    it.copy(
                        step = AuthStep.Unlocked,
                        isBusy = false,
                        errorMessage = null,
                        attemptsLeft = AppConstants.MAX_FAILED_AUTH_ATTEMPTS,
                    )
                }
                return@launch
            }

            val remaining = vault.remainingAttempts()
            when (val failure = result.failure) {
                is VaultSelfDestructFailure -> {
                    _state.update {
                        it.copy(
                            isBusy = false,
                            attemptsLeft = 0,
                            showSelfDestructDialog = true,
                            errorMessage = UiCopy.AUTH_SELF_DESTRUCT_DONE,
                        )
                    }
                }
                is AuthMaxAttemptsFailure -> {
                    _state.update {
                        it.copy(
                            isBusy = false,
                            attemptsLeft = remaining,
                            errorMessage = if (remaining <= 1) {
                                UiCopy.AUTH_SELF_DESTRUCT_WARNING
                            } else {
                                "${UiCopy.AUTH_FAILED_PREFIX}$remaining"
                            },
                        )
                    }
                }
                is AuthBiometricFailure -> {
                    _state.update {
                        it.copy(
                            isBusy = false,
                            attemptsLeft = remaining,
                            errorMessage = "${UiCopy.AUTH_FAILED_PREFIX}$remaining. ${failure.message}",
                        )
                    }
                }
                else -> {
                    _state.update {
                        it.copy(
                            isBusy = false,
                            attemptsLeft = remaining,
                            errorMessage = failure?.message ?: UiCopy.AUTH_FAILED_PREFIX,
                        )
                    }
                }
            }
        }
    }

    fun acknowledgeSelfDestruct() {
        _state.update {
            it.copy(
                showSelfDestructDialog = false,
                step = AuthStep.NewVault,
                attemptsLeft = AppConstants.MAX_FAILED_AUTH_ATTEMPTS,
                errorMessage = null,
            )
        }
    }

    fun lock() {
        vault.lock()
        _state.update {
            AuthUiState(step = AuthStep.Unlock)
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
