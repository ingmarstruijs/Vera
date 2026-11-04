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
 * AES-GCM uses a minimal CommonCrypto-compatible path via CCCryptorGCM
 * when available; for P3 we keep encrypt/decrypt wired through the same
 * wire format. Full Keychain/biometric unlock is P4.
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

/**
 * Placeholder AES-GCM for iOS Shared framework builds.
 * Production should use CryptoKit AES.GCM (P4 hardening). Until then, this
 * keeps the KMP target compiling; Android unit tests cover crypto correctness.
 */
internal actual fun platformAesGcmEncrypt(
    key: ByteArray,
    nonce: ByteArray,
    plaintext: ByteArray,
): ByteArray = iosAesGcmTransform(encrypt = true, key, nonce, plaintext)

internal actual fun platformAesGcmDecrypt(
    key: ByteArray,
    nonce: ByteArray,
    ciphertextAndTag: ByteArray,
): ByteArray = iosAesGcmTransform(encrypt = false, key, nonce, ciphertextAndTag)

/**
 * Soft dependency: call into a tiny pure-Kotlin AES-GCM shim shared via expect
 * would be ideal; for now throw a clear error if somehow invoked without
 * CryptoKit wiring. Android is the verified crypto path in P3 CI.
 */
private fun iosAesGcmTransform(
    encrypt: Boolean,
    key: ByteArray,
    nonce: ByteArray,
    data: ByteArray,
): ByteArray {
    // Use OpenSSL-free pure implementation via CryptographyProvider if linked;
    // until CryptoKit bridge lands, refuse rather than silently weaken security.
    throw UnsupportedOperationException(
        "iOS AES-GCM platform binding lands with P4 Keychain wiring " +
            "(encrypt=$encrypt, key=${key.size}, nonce=${nonce.size}, data=${data.size})",
    )
}
