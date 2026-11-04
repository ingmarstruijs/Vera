package net.moonbaseone.vera.shared.vault

import kotlinx.coroutines.runBlocking
import net.moonbaseone.vera.shared.AppConstants
import net.moonbaseone.vera.shared.domain.MedicalRecord
import net.moonbaseone.vera.shared.failure.AuthBiometricFailure
import net.moonbaseone.vera.shared.failure.AuthMaxAttemptsFailure
import net.moonbaseone.vera.shared.failure.VaultLockedFailure
import net.moonbaseone.vera.shared.failure.VaultSelfDestructFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalVaultServiceTest {
    private fun newVault(
        keyStore: SecureKeyStore = InMemorySecureKeyStore(),
        recordStore: MedicalRecordStore = InMemoryMedicalRecordStore(),
        biometric: BiometricAuthenticator = FakeBiometricAuthenticator(),
        maxAttempts: Int = AppConstants.MAX_FAILED_AUTH_ATTEMPTS,
    ) = LocalVaultService(
        keyStore = keyStore,
        recordStore = recordStore,
        biometricAuthenticator = biometric,
        maxFailedAttempts = maxAttempts,
    )

    private fun sampleRecord(id: String = "uuid-001") = MedicalRecord(
        id = id,
        resourceType = "Condition",
        title = "Invasief ductaal carcinoom",
        dateRecorded = "2024-04-10",
        anonymisedPayloadJson = """{"code":"C50.4"}""",
        tags = listOf("oncology"),
    )

    @Test
    fun newVault_isLockedAndUninitialised() = runBlocking {
        val vault = newVault()
        assertFalse(vault.isUnlocked)
        assertFalse(vault.isVaultInitialised())
        Unit
    }

    @Test
    fun createAndUnlock_thenStoreAndLoadRoundTrip() = runBlocking {
        val vault = newVault()
        vault.createVault()
        assertTrue(vault.isVaultInitialised())

        val unlock = vault.unlockWithStoredKey()
        assertTrue(unlock.success)
        assertTrue(vault.isUnlocked)

        val store = vault.storeRecord(sampleRecord())
        assertTrue(store.success)

        val loaded = vault.getAllRecords()
        assertNull(loaded.failure)
        assertEquals(1, loaded.records.size)
        assertEquals("""{"code":"C50.4"}""", loaded.records[0].anonymisedPayloadJson)
        assertEquals("Invasief ductaal carcinoom", loaded.records[0].title)
        assertEquals(listOf("oncology"), loaded.records[0].tags)
        Unit
    }

    @Test
    fun unlockWithBiometrics_success_loadsSession() = runBlocking {
        val biometric = FakeBiometricAuthenticator()
        val vault = newVault(biometric = biometric)
        vault.createVault()
        val result = vault.unlockWithBiometrics(authenticator = biometric)
        assertTrue(result.success)
        assertTrue(vault.isUnlocked)
        assertEquals(1, biometric.authenticateCalls)
        Unit
    }

    @Test
    fun unlockWithBiometrics_unavailable_returnsAuthFailure() = runBlocking {
        val biometric = FakeBiometricAuthenticator(canAuth = false)
        val vault = newVault(biometric = biometric)
        vault.createVault()
        val result = vault.unlockWithBiometrics(authenticator = biometric)
        assertFalse(result.success)
        assertTrue(result.failure is AuthBiometricFailure)
        assertFalse(vault.isUnlocked)
        Unit
    }

    @Test
    fun unlockWithBiometrics_failedAttempt_countsTowardSelfDestruct() = runBlocking {
        val biometric = FakeBiometricAuthenticator(
            result = BiometricAuthResult.Failed(AuthBiometricFailure("fail")),
        )
        val keyStore = InMemorySecureKeyStore()
        val vault = newVault(keyStore = keyStore, biometric = biometric, maxAttempts = 2)
        vault.createVault()

        val first = vault.unlockWithBiometrics(authenticator = biometric)
        assertTrue(first.failure is AuthBiometricFailure)

        val second = vault.unlockWithBiometrics(authenticator = biometric)
        assertTrue(second.failure is VaultSelfDestructFailure)
        assertFalse(vault.isVaultInitialised())
        Unit
    }

    @Test
    fun storeWhileLocked_returnsVaultLockedFailure() = runBlocking {
        val vault = newVault()
        vault.createVault()
        val result = vault.storeRecord(sampleRecord())
        assertFalse(result.success)
        assertTrue(result.failure is VaultLockedFailure)
        Unit
    }

    @Test
    fun lock_clearsSession() = runBlocking {
        val vault = newVault()
        vault.createVault()
        vault.unlockWithStoredKey()
        vault.lock()
        assertFalse(vault.isUnlocked)
        assertTrue(vault.getAllRecords().failure is VaultLockedFailure)
        Unit
    }

    @Test
    fun wipeVault_removesKeyAndRecords() = runBlocking {
        val vault = newVault()
        vault.createVault()
        vault.unlockWithStoredKey()
        vault.storeRecord(sampleRecord())
        vault.wipeVault()
        assertFalse(vault.isVaultInitialised())
        assertFalse(vault.isUnlocked)
        Unit
    }

    @Test
    fun failedUnlocks_selfDestructAfterMaxAttempts() = runBlocking {
        val keyStore = InMemorySecureKeyStore()
        val records = InMemoryMedicalRecordStore()
        val vault = newVault(keyStore, records, maxAttempts = 3)
        vault.createVault()
        vault.unlockWithStoredKey()
        vault.storeRecord(sampleRecord())
        vault.lock()

        repeat(2) {
            val r = vault.registerFailedUnlock()
            assertTrue(r.failure is AuthMaxAttemptsFailure)
        }
        val last = vault.registerFailedUnlock()
        assertTrue(last.failure is VaultSelfDestructFailure)
        assertFalse(vault.isVaultInitialised())
        assertTrue(records.getAllSortedByDate().isEmpty())
        Unit
    }

    @Test
    fun failureMessages_nonEmpty() {
        assertFalse(VaultLockedFailure().message.isNullOrEmpty())
        assertFalse(VaultSelfDestructFailure().message.isNullOrEmpty())
        assertFalse(AuthMaxAttemptsFailure().message.isNullOrEmpty())
    }
}
