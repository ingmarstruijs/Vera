package net.moonbaseone.vera.shared.vault

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import kotlin.coroutines.resume

/**
 * Android [BiometricPrompt] gate. Requires a [FragmentActivity].
 * Allows PIN/pattern fallback via [DEVICE_CREDENTIAL].
 */
class AndroidBiometricAuthenticator(
    private val activity: FragmentActivity,
) : BiometricAuthenticator {
    private val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    override suspend fun canAuthenticate(): Boolean {
        val result = BiometricManager.from(activity).canAuthenticate(authenticators)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    override suspend fun authenticate(reason: String): BiometricAuthResult =
        suspendCancellableCoroutine { cont ->
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (cont.isActive) cont.resume(BiometricAuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (!cont.isActive) return
                    val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    cont.resume(
                        if (cancelled) {
                            BiometricAuthResult.Cancelled
                        } else {
                            BiometricAuthResult.Failed(AuthBiometricFailure(errString.toString()))
                        },
                    )
                }

                override fun onAuthenticationFailed() {
                    // Wrong biometric — keep prompt open.
                }
            }

            val prompt = BiometricPrompt(activity, executor, callback)
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Vera")
                .setSubtitle(reason)
                .setAllowedAuthenticators(authenticators)
                .build()
            prompt.authenticate(info)
        }
}
