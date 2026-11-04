package net.moonbaseone.vera.shared.vault

import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import net.moonbaseone.vera.shared.failure.Failure

/** Platform biometric / device-credential gate (Face ID, fingerprint, PIN). */
interface BiometricAuthenticator {
    /** True when biometrics or device credentials can be used. */
    suspend fun canAuthenticate(): Boolean

    /**
     * Presents the system auth UI.
     * [reason] is shown to the user (localized by the app layer).
     */
    suspend fun authenticate(reason: String): BiometricAuthResult
}

sealed class BiometricAuthResult {
    data object Success : BiometricAuthResult()
    data object Cancelled : BiometricAuthResult()
    /** No biometric hardware/enrollment and no usable device credential. */
    data class Unavailable(val failure: Failure) : BiometricAuthResult()
    data class Failed(val failure: Failure) : BiometricAuthResult()
}

/**
 * Test double for biometric / device-credential gates.
 *
 * [canAuth] = false models a device with neither biometrics nor a PIN/pattern/password.
 * Keep [canAuth] = true with [result] = Success to model PIN-only (device-credential) unlock.
 */
class FakeBiometricAuthenticator(
    private val canAuth: Boolean = true,
    private val result: BiometricAuthResult = BiometricAuthResult.Success,
) : BiometricAuthenticator {
    var authenticateCalls: Int = 0
        private set

    override suspend fun canAuthenticate(): Boolean = canAuth

    override suspend fun authenticate(reason: String): BiometricAuthResult {
        authenticateCalls++
        return result
    }
}

fun biometricUnavailableFailure(
    message: String = "Geen schermbeveiliging op dit apparaat. Stel een PIN of biometrie in via de apparaatinstellingen.",
): BiometricAuthResult = BiometricAuthResult.Unavailable(AuthBiometricFailure(message))
