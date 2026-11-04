package net.moonbaseone.vera.shared.vault

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import kotlin.coroutines.resume

/**
 * Android [BiometricPrompt] gate. Requires a [FragmentActivity].
 *
 * Supports fingerprint/face when enrolled, and PIN/pattern/password via
 * [DEVICE_CREDENTIAL] when biometrics are absent (common on emulators).
 * Prompt construction always runs on the main thread.
 */
class AndroidBiometricAuthenticator(
    private val activity: FragmentActivity,
) : BiometricAuthenticator {

    override suspend fun canAuthenticate(): Boolean =
        withContext(Dispatchers.Main.immediate) {
            resolveAuthenticators() != null
        }

    override suspend fun authenticate(reason: String): BiometricAuthResult =
        withContext(Dispatchers.Main.immediate) {
            val authenticators = resolveAuthenticators()
                ?: return@withContext biometricUnavailableFailure()

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
                        // Hardware / enrollment gaps are not "wrong PIN" failures.
                        val unavailable = errorCode == BiometricPrompt.ERROR_HW_NOT_PRESENT ||
                            errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE ||
                            errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                            errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
                        cont.resume(
                            when {
                                cancelled -> BiometricAuthResult.Cancelled
                                unavailable ->
                                    BiometricAuthResult.Unavailable(
                                        AuthBiometricFailure(errString.toString()),
                                    )
                                else -> BiometricAuthResult.Failed(
                                    AuthBiometricFailure(errString.toString()),
                                )
                            },
                        )
                    }

                    override fun onAuthenticationFailed() {
                        // Wrong biometric — keep prompt open.
                    }
                }

                try {
                    val prompt = BiometricPrompt(activity, executor, callback)
                    val info = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Vera")
                        .setSubtitle(reason)
                        .setAllowedAuthenticators(authenticators)
                        .build()
                    prompt.authenticate(info)
                } catch (t: Throwable) {
                    if (cont.isActive) {
                        cont.resume(
                            BiometricAuthResult.Unavailable(
                                AuthBiometricFailure(
                                    t.message
                                        ?: "Authenticatie kon niet worden gestart.",
                                ),
                            ),
                        )
                    }
                }
            }
        }

    /**
     * Prefer strong biometrics + device credential, then weak + credential,
     * then credential-only on API 30+ (PIN/pattern/password with no biometrics).
     */
    private fun resolveAuthenticators(): Int? {
        val manager = BiometricManager.from(activity)
        val candidates = buildList {
            add(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            add(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(DEVICE_CREDENTIAL)
            }
        }
        for (candidate in candidates) {
            if (manager.canAuthenticate(candidate) == BiometricManager.BIOMETRIC_SUCCESS) {
                return candidate
            }
        }
        return null
    }
}
