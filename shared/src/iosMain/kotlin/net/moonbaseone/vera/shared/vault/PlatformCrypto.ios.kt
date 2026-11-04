package net.moonbaseone.vera.shared.vault

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

/**
 * iOS secure random via Security.framework.
 *
 * AES-GCM: still deferred to a CryptoKit/Swift bridge (P4 note). Android path
 * remains the CI-verified crypto implementation. Keychain + biometrics are live.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun platformSecureRandom(size: Int): ByteArray {
    val bytes = ByteArray(size)
    bytes.usePinned { pinned ->
        val status = SecRandomCopyBytes(kSecRandomDefault, size.convert(), pinned.addressOf(0))
        check(status == errSecSuccess) { "SecRandomCopyBytes failed: $status" }
    }
    return bytes
}

internal actual fun platformAesGcmEncrypt(
    key: ByteArray,
    nonce: ByteArray,
    plaintext: ByteArray,
): ByteArray = throw UnsupportedOperationException(
    "iOS AES-GCM requires CryptoKit bridge (follow-up). Use Android for encrypt round-trips.",
)

internal actual fun platformAesGcmDecrypt(
    key: ByteArray,
    nonce: ByteArray,
    ciphertextAndTag: ByteArray,
): ByteArray = throw UnsupportedOperationException(
    "iOS AES-GCM requires CryptoKit bridge (follow-up). Use Android for decrypt round-trips.",
)
