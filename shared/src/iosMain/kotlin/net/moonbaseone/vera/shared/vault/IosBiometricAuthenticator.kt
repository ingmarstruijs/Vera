package net.moonbaseone.vera.shared.vault

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import kotlin.coroutines.resume

/**
 * iOS LocalAuthentication gate.
 *
 * Uses [LAPolicyDeviceOwnerAuthentication] so Face ID / Touch ID work when enrolled,
 * and the device passcode works when biometrics are unavailable — never requires
 * biometric enrollment to unlock.
 */
@OptIn(ExperimentalForeignApi::class)
class IosBiometricAuthenticator : BiometricAuthenticator {
    override suspend fun canAuthenticate(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)
    }

    override suspend fun authenticate(reason: String): BiometricAuthResult =
        suspendCancellableCoroutine { cont ->
            val context = LAContext()
            if (!context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)) {
                cont.resume(biometricUnavailableFailure())
                return@suspendCancellableCoroutine
            }

            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthentication,
                localizedReason = reason,
            ) { success, error ->
                if (!cont.isActive) return@evaluatePolicy
                when {
                    success -> cont.resume(BiometricAuthResult.Success)
                    error?.code?.toLong() == cancelledCode ||
                        error?.code?.toLong() == systemCancelCode ||
                        error?.code?.toLong() == appCancelCode ->
                        cont.resume(BiometricAuthResult.Cancelled)
                    error?.code?.toLong() == passcodeNotSetCode ||
                        error?.code?.toLong() == biometryNotAvailableCode ->
                        cont.resume(
                            BiometricAuthResult.Unavailable(
                                AuthBiometricFailure(
                                    error?.localizedDescription
                                        ?: "Geen schermbeveiliging beschikbaar.",
                                ),
                            ),
                        )
                    else -> cont.resume(
                        BiometricAuthResult.Failed(
                            AuthBiometricFailure(
                                error?.localizedDescription ?: "Authenticatie mislukt",
                            ),
                        ),
                    )
                }
            }
        }

    private companion object {
        // LAError codes
        const val cancelledCode = -2L // userCancel
        const val systemCancelCode = -4L
        const val appCancelCode = -9L
        const val passcodeNotSetCode = -5L
        const val biometryNotAvailableCode = -6L
    }
}
