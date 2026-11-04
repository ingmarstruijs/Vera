package net.moonbaseone.vera.shared.vault

import com.benasher44.uuid.uuid4
import net.moonbaseone.vera.shared.AppConstants
import net.moonbaseone.vera.shared.domain.MedicalRecord
import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import net.moonbaseone.vera.shared.failure.AuthMaxAttemptsFailure
import net.moonbaseone.vera.shared.failure.Failure
import net.moonbaseone.vera.shared.failure.VaultEncryptionFailure
import net.moonbaseone.vera.shared.failure.VaultKeyNotFoundFailure
import net.moonbaseone.vera.shared.failure.VaultLockedFailure
import net.moonbaseone.vera.shared.failure.VaultSelfDestructFailure
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class VaultOpResult(
    val success: Boolean,
    val failure: Failure? = null,
)

data class VaultRecordsResult(
    val records: List<MedicalRecord> = emptyList(),
    val failure: Failure? = null,
)

/**
 * Zero-knowledge local vault: AES-256-GCM payloads + secure key custody.
 *
 * [unlockWithBiometrics] gates key release via [BiometricAuthenticator]
 * (Android BiometricPrompt / iOS LocalAuthentication).
 */
@OptIn(ExperimentalEncodingApi::class)
class LocalVaultService(
    private val keyStore: SecureKeyStore,
    private val recordStore: MedicalRecordStore,
    private val biometricAuthenticator: BiometricAuthenticator = FakeBiometricAuthenticator(),
    private val maxFailedAttempts: Int = AppConstants.MAX_FAILED_AUTH_ATTEMPTS,
) {
    private var sessionKey: ByteArray? = null

    val isUnlocked: Boolean get() = sessionKey != null

    suspend fun isVaultInitialised(): Boolean =
        keyStore.read(AppConstants.VAULT_KEY_STORAGE_KEY) != null

    /** Creates and persists a new AES-256 vault key (first-run). */
    suspend fun createVault() {
        val key = VaultCrypto.generateKey()
        keyStore.write(AppConstants.VAULT_KEY_STORAGE_KEY, Base64.encode(key))
        keyStore.write(AppConstants.FAILED_ATTEMPTS_KEY, "0")
    }

    /**
     * Biometric / device-credential unlock, then load the master key into the session.
     * Pass a platform [BiometricAuthenticator] from the Activity / UI layer when needed.
     */
    suspend fun unlockWithBiometrics(
        reason: String = "Ontgrendel Vera om je medische gegevens te openen",
        authenticator: BiometricAuthenticator = biometricAuthenticator,
    ): VaultOpResult {
        val remaining = remainingAttempts()
        if (remaining <= 0) {
            wipeVault()
            return VaultOpResult(success = false, failure = VaultSelfDestructFailure())
        }

        if (!authenticator.canAuthenticate()) {
            return VaultOpResult(
                success = false,
                failure = AuthBiometricFailure(
                    "Geen schermbeveiliging op dit apparaat. Stel een PIN of biometrie in via de apparaatinstellingen.",
                ),
            )
        }

        return when (val auth = authenticator.authenticate(reason)) {
            BiometricAuthResult.Success -> unlockWithStoredKey()
            BiometricAuthResult.Cancelled -> VaultOpResult(
                success = false,
                failure = AuthBiometricFailure("Authenticatie geannuleerd."),
            )
            is BiometricAuthResult.Failed -> {
                // Count failed biometric attempts toward self-destruct.
                val tracked = handleFailedAttempt()
                if (tracked.failure is VaultSelfDestructFailure) tracked
                else VaultOpResult(success = false, failure = auth.failure)
            }
        }
    }

    /**
     * Loads the master key from secure storage into the session (no biometric UI).
     * Used by tests and after a successful [unlockWithBiometrics] gate.
     */
    suspend fun unlockWithStoredKey(): VaultOpResult {
        val remaining = remainingAttempts()
        if (remaining <= 0) {
            wipeVault()
            return VaultOpResult(success = false, failure = VaultSelfDestructFailure())
        }
        return try {
            loadSessionKey()
            resetFailedAttempts()
            VaultOpResult(success = true)
        } catch (e: VaultKeyNotFoundFailure) {
            VaultOpResult(success = false, failure = e)
        } catch (e: Exception) {
            handleFailedAttempt()
        }
    }

    /** Test helper: unlock with an explicit key (skips secure storage read). */
    fun unlockWithKey(keyBytes: ByteArray) {
        require(keyBytes.size == AppConstants.AES_KEY_BYTES)
        sessionKey = keyBytes.copyOf()
    }

    fun lock() {
        sessionKey = null
    }

    suspend fun storeRecord(record: MedicalRecord): VaultOpResult {
        val key = sessionKey
            ?: return VaultOpResult(success = false, failure = VaultLockedFailure())
        return try {
            val uuid = record.id.ifEmpty { uuid4().toString() }
            val row = EncryptedMedicalRecordRow(
                uuid = uuid,
                encryptedPayload = VaultCrypto.encryptString(record.anonymisedPayloadJson, key),
                encryptedTitle = VaultCrypto.encryptString(record.title, key),
                resourceType = record.resourceType,
                dateRecorded = record.dateRecorded,
                iknlGuidelineRef = record.iknlGuidelineRef,
                fhirStatus = record.fhirStatus,
                tagsCsv = record.tags.joinToString(","),
            )
            recordStore.upsert(row)
            VaultOpResult(success = true)
        } catch (e: Exception) {
            VaultOpResult(
                success = false,
                failure = VaultEncryptionFailure("Opslaan mislukt: ${e.message}"),
            )
        }
    }

    suspend fun getAllRecords(): VaultRecordsResult {
        val key = sessionKey
            ?: return VaultRecordsResult(failure = VaultLockedFailure())
        return try {
            val records = recordStore.getAllSortedByDate().map { row ->
                MedicalRecord(
                    id = row.uuid,
                    resourceType = row.resourceType,
                    title = VaultCrypto.decryptString(row.encryptedTitle, key),
                    dateRecorded = row.dateRecorded,
                    anonymisedPayloadJson = VaultCrypto.decryptString(row.encryptedPayload, key),
                    iknlGuidelineRef = row.iknlGuidelineRef,
                    fhirStatus = row.fhirStatus,
                    tags = if (row.tagsCsv.isEmpty()) emptyList() else row.tagsCsv.split(","),
                )
            }
            VaultRecordsResult(records = records)
        } catch (e: Exception) {
            VaultRecordsResult(
                failure = VaultEncryptionFailure("Laden mislukt: ${e.message}"),
            )
        }
    }

    suspend fun deleteRecord(uuid: String) {
        recordStore.deleteByUuid(uuid)
    }

    suspend fun wipeVault() {
        // Zero-fill then delete key material.
        keyStore.write(
            AppConstants.VAULT_KEY_STORAGE_KEY,
            Base64.encode(ByteArray(AppConstants.AES_KEY_BYTES)),
        )
        keyStore.delete(AppConstants.VAULT_KEY_STORAGE_KEY)
        keyStore.delete(AppConstants.FAILED_ATTEMPTS_KEY)
        recordStore.clear()
        sessionKey = null
    }

    /** Records a failed unlock (biometric refusal, etc.). */
    suspend fun registerFailedUnlock(): VaultOpResult = handleFailedAttempt()

    private suspend fun loadSessionKey() {
        val encoded = keyStore.read(AppConstants.VAULT_KEY_STORAGE_KEY)
            ?: throw VaultKeyNotFoundFailure()
        sessionKey = Base64.decode(encoded)
    }

    private suspend fun remainingAttempts(): Int {
        val raw = keyStore.read(AppConstants.FAILED_ATTEMPTS_KEY) ?: "0"
        val failed = raw.toIntOrNull() ?: 0
        return maxFailedAttempts - failed
    }

    private suspend fun handleFailedAttempt(): VaultOpResult {
        val raw = keyStore.read(AppConstants.FAILED_ATTEMPTS_KEY) ?: "0"
        val failed = (raw.toIntOrNull() ?: 0) + 1
        keyStore.write(AppConstants.FAILED_ATTEMPTS_KEY, failed.toString())
        if (failed >= maxFailedAttempts) {
            wipeVault()
            return VaultOpResult(success = false, failure = VaultSelfDestructFailure())
        }
        return VaultOpResult(success = false, failure = AuthMaxAttemptsFailure())
    }

    private suspend fun resetFailedAttempts() {
        keyStore.write(AppConstants.FAILED_ATTEMPTS_KEY, "0")
    }
}
