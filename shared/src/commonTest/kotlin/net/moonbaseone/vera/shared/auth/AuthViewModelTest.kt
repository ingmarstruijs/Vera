package net.moonbaseone.vera.shared.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.vault.BiometricAuthResult
import net.moonbaseone.vera.shared.vault.FakeBiometricAuthenticator
import net.moonbaseone.vera.shared.vault.InMemoryMedicalRecordStore
import net.moonbaseone.vera.shared.vault.InMemorySecureKeyStore
import net.moonbaseone.vera.shared.vault.LocalVaultService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthViewModelTest {
    private fun vault(
        biometric: FakeBiometricAuthenticator = FakeBiometricAuthenticator(),
    ) = LocalVaultService(
        keyStore = InMemorySecureKeyStore(),
        recordStore = InMemoryMedicalRecordStore(),
        biometricAuthenticator = biometric,
    )

    private suspend fun awaitStep(vm: AuthViewModel, predicate: (AuthUiState) -> Boolean) {
        repeat(40) {
            if (predicate(vm.state.value)) return
            delay(25)
        }
        error("Timed out waiting for auth state: ${vm.state.value}")
    }

    @Test
    fun start_uninitialised_showsNewVault() = runBlocking {
        val vm = AuthViewModel(vault())
        vm.start(autoUnlock = false)
        awaitStep(vm) { it.step == AuthStep.NewVault }
        vm.dispose()
    }

    @Test
    fun createVault_thenUnlock_reachesUnlocked() = runBlocking {
        val biometric = FakeBiometricAuthenticator()
        val vm = AuthViewModel(vault(biometric))
        vm.start(autoUnlock = false)
        awaitStep(vm) { it.step == AuthStep.NewVault }
        vm.createVault()
        awaitStep(vm) { it.step == AuthStep.Unlocked }
        assertEquals(1, biometric.authenticateCalls)
        vm.dispose()
    }

    @Test
    fun unlock_failure_decrementsAttempts() = runBlocking {
        val biometric = FakeBiometricAuthenticator(
            result = BiometricAuthResult.Failed(
                net.moonbaseone.vera.shared.failure.AuthBiometricFailure("nee"),
            ),
        )
        val service = vault(biometric)
        service.createVault()
        val vm = AuthViewModel(service)
        vm.start(autoUnlock = false)
        awaitStep(vm) { it.step == AuthStep.Unlock }
        vm.unlockBiometric()
        awaitStep(vm) { it.errorMessage != null && !it.isBusy }
        assertTrue(vm.state.value.attemptsLeft < 5)
        assertEquals(AuthStep.Unlock, vm.state.value.step)
        vm.dispose()
    }
    @Test
    fun unlock_deviceCredentialOnly_reachesUnlocked() = runBlocking {
        // PIN/pattern present, no enrolled biometrics — authenticator still succeeds.
        val biometric = FakeBiometricAuthenticator(canAuth = true)
        val service = vault(biometric)
        service.createVault()
        val vm = AuthViewModel(service)
        vm.start(autoUnlock = true)
        awaitStep(vm) { it.step == AuthStep.Unlocked }
        assertEquals(1, biometric.authenticateCalls)
        vm.dispose()
    }

    @Test
    fun unlock_noBiometricsOrCredential_staysOnUnlockWithoutCrash() = runBlocking {
        val biometric = FakeBiometricAuthenticator(canAuth = false)
        val service = vault(biometric)
        service.createVault()
        val beforeAttempts = service.remainingAttempts()
        val vm = AuthViewModel(service)
        vm.start(autoUnlock = true)
        awaitStep(vm) { it.errorMessage != null && !it.isBusy }
        assertEquals(AuthStep.Unlock, vm.state.value.step)
        assertEquals(beforeAttempts, vm.state.value.attemptsLeft)
        assertEquals(0, biometric.authenticateCalls)
        vm.dispose()
    }

    @Test
    fun unlock_unavailableResult_doesNotDecrementAttempts() = runBlocking {
        val biometric = FakeBiometricAuthenticator(
            result = BiometricAuthResult.Unavailable(
                net.moonbaseone.vera.shared.failure.AuthBiometricFailure("geen biometrie"),
            ),
        )
        val service = vault(biometric)
        service.createVault()
        val vm = AuthViewModel(service)
        vm.start(autoUnlock = false)
        awaitStep(vm) { it.step == AuthStep.Unlock }
        vm.unlockBiometric()
        awaitStep(vm) { it.errorMessage != null && !it.isBusy }
        assertEquals(5, vm.state.value.attemptsLeft)
        assertEquals(AuthStep.Unlock, vm.state.value.step)
        vm.dispose()
    }
}
