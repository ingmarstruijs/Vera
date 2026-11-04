package net.moonbaseone.vera.shared.vault

/**
 * Abstraction over platform secure storage (Android Keystore / iOS Keychain).
 * P3: interface + in-memory fake for tests. Real platform actuals in P4.
 */
interface SecureKeyStore {
    suspend fun read(key: String): String?
    suspend fun write(key: String, value: String)
    suspend fun delete(key: String)
}

/** Process-local fake used by unit tests and as a temporary DI default. */
class InMemorySecureKeyStore : SecureKeyStore {
    private val values = mutableMapOf<String, String>()

    override suspend fun read(key: String): String? = values[key]

    override suspend fun write(key: String, value: String) {
        values[key] = value
    }

    override suspend fun delete(key: String) {
        values.remove(key)
    }

    fun clear() = values.clear()
}
