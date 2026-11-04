package net.moonbaseone.vera.shared.vault

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Persists vault secrets in EncryptedSharedPreferences (AES-256 via Android Keystore MasterKey).
 */
class AndroidSecureKeyStore(
    context: Context,
    prefsName: String = "vera_secure_prefs",
) : SecureKeyStore {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences by lazy { createPrefs(prefsName) }

    private fun createPrefs(name: String): SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            appContext,
            name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun read(key: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, null)
    }

    override suspend fun write(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, value).apply()
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).apply()
    }
}
