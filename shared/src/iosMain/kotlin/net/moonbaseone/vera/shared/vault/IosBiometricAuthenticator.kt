package net.moonbaseone.vera.shared.vault

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

/**
 * iOS LocalAuthentication gate (Face ID / Touch ID with device passcode fallback).
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
            val policy = if (
                context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)
            ) {
                LAPolicyDeviceOwnerAuthenticationWithBiometrics
            } else {
                LAPolicyDeviceOwnerAuthentication
            }

            context.evaluatePolicy(policy, localizedReason = reason) { success, error ->
                if (!cont.isActive) return@evaluatePolicy
                when {
                    success -> cont.resume(BiometricAuthResult.Success)
                    error?.code?.toLong() == cancelledCode ->
                        cont.resume(BiometricAuthResult.Cancelled)
                    else -> cont.resume(
                        BiometricAuthResult.Failed(
                            AuthBiometricFailure(error?.localizedDescription ?: "Authenticatie mislukt"),
                        ),
                    )
                }
            }
        }

    private companion object {
        // LAError.userCancel / systemCancel / appCancel
        const val cancelledCode = -2L
    }
}
