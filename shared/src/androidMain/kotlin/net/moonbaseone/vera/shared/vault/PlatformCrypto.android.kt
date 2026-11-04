package net.moonbaseone.vera.shared.vault

import net.moonbaseone.vera.shared.AppConstants
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal actual fun platformSecureRandom(size: Int): ByteArray {
    val bytes = ByteArray(size)
    SecureRandom().nextBytes(bytes)
    return bytes
}

internal actual fun platformAesGcmEncrypt(
    key: ByteArray,
    nonce: ByteArray,
    plaintext: ByteArray,
): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
        Cipher.ENCRYPT_MODE,
        SecretKeySpec(key, "AES"),
        GCMParameterSpec(AppConstants.GCM_TAG_BITS, nonce),
    )
    return cipher.doFinal(plaintext)
}

internal actual fun platformAesGcmDecrypt(
    key: ByteArray,
    nonce: ByteArray,
    ciphertextAndTag: ByteArray,
): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
        Cipher.DECRYPT_MODE,
        SecretKeySpec(key, "AES"),
        GCMParameterSpec(AppConstants.GCM_TAG_BITS, nonce),
    )
    return cipher.doFinal(ciphertextAndTag)
}
