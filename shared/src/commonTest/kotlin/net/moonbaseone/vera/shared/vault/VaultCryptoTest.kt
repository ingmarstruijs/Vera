package net.moonbaseone.vera.shared.vault

import net.moonbaseone.vera.shared.AppConstants
import net.moonbaseone.vera.shared.failure.VaultEncryptionFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class VaultCryptoTest {
    @Test
    fun generateKey_isAes256() {
        val key = VaultCrypto.generateKey()
        assertEquals(AppConstants.AES_KEY_BYTES, key.size)
    }

    @Test
    fun encryptDecrypt_roundTrip() {
        val key = VaultCrypto.generateKey()
        val plain = """{"code":"C50.4","note":"Invasief ductaal carcinoom"}"""
        val encoded = VaultCrypto.encryptString(plain, key)
        assertNotEquals(plain, encoded)
        assertEquals(plain, VaultCrypto.decryptString(encoded, key))
    }

    @Test
    fun encrypt_usesUniqueNonce() {
        val key = VaultCrypto.generateKey()
        val plain = "same-plaintext"
        val a = VaultCrypto.encryptString(plain, key)
        val b = VaultCrypto.encryptString(plain, key)
        assertNotEquals(a, b)
        assertEquals(plain, VaultCrypto.decryptString(a, key))
        assertEquals(plain, VaultCrypto.decryptString(b, key))
    }

    @Test
    fun decrypt_withWrongKey_fails() {
        val encoded = VaultCrypto.encryptString("secret", VaultCrypto.generateKey())
        assertFailsWith<VaultEncryptionFailure> {
            VaultCrypto.decryptString(encoded, VaultCrypto.generateKey())
        }
    }

    @Test
    fun ciphertext_isLongerThanNonce() {
        val encoded = VaultCrypto.encryptString("hi", VaultCrypto.generateKey())
        assertTrue(encoded.length > 16)
    }
}
