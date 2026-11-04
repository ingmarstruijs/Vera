package net.moonbaseone.vera.shared.vault

import net.moonbaseone.vera.shared.AppConstants
import net.moonbaseone.vera.shared.failure.VaultEncryptionFailure
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * AES-256-GCM helpers matching the vault wire format (nonce ‖ ciphertext ‖ tag):
 * `base64( nonce[12] || ciphertext || tag[16] )`.
 */
@OptIn(ExperimentalEncodingApi::class)
object VaultCrypto {
    fun generateKey(): ByteArray = platformSecureRandom(AppConstants.AES_KEY_BYTES)

    fun encryptString(plainText: String, keyBytes: ByteArray): String {
        require(keyBytes.size == AppConstants.AES_KEY_BYTES) {
            "AES key must be ${AppConstants.AES_KEY_BYTES} bytes"
        }
        return try {
            val nonce = platformSecureRandom(AppConstants.GCM_NONCE_BYTES)
            val ciphertextAndTag =
                platformAesGcmEncrypt(keyBytes, nonce, plainText.encodeToByteArray())
            val combined = ByteArray(nonce.size + ciphertextAndTag.size)
            nonce.copyInto(combined, destinationOffset = 0)
            ciphertextAndTag.copyInto(combined, destinationOffset = nonce.size)
            Base64.encode(combined)
        } catch (e: VaultEncryptionFailure) {
            throw e
        } catch (e: Exception) {
            throw VaultEncryptionFailure("Encryptie mislukt: ${e.message}")
        }
    }

    fun decryptString(encoded: String, keyBytes: ByteArray): String {
        require(keyBytes.size == AppConstants.AES_KEY_BYTES) {
            "AES key must be ${AppConstants.AES_KEY_BYTES} bytes"
        }
        return try {
            val combined = Base64.decode(encoded)
            require(combined.size > AppConstants.GCM_NONCE_BYTES) { "Ciphertext too short" }
            val nonce = combined.copyOfRange(0, AppConstants.GCM_NONCE_BYTES)
            val ciphertextAndTag =
                combined.copyOfRange(AppConstants.GCM_NONCE_BYTES, combined.size)
            val plain = platformAesGcmDecrypt(keyBytes, nonce, ciphertextAndTag)
            plain.decodeToString()
        } catch (e: VaultEncryptionFailure) {
            throw e
        } catch (e: Exception) {
            throw VaultEncryptionFailure("Decryptie mislukt: ${e.message}")
        }
    }
}

internal expect fun platformSecureRandom(size: Int): ByteArray

/** Returns ciphertext || tag (16-byte GCM tag). */
internal expect fun platformAesGcmEncrypt(
    key: ByteArray,
    nonce: ByteArray,
    plaintext: ByteArray,
): ByteArray

internal expect fun platformAesGcmDecrypt(
    key: ByteArray,
    nonce: ByteArray,
    ciphertextAndTag: ByteArray,
): ByteArray
