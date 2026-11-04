package net.moonbaseone.vera.shared.di

import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.shield.IdentityShieldService
import net.moonbaseone.vera.shared.vault.InMemoryMedicalRecordStore
import net.moonbaseone.vera.shared.vault.InMemorySecureKeyStore
import net.moonbaseone.vera.shared.vault.LocalVaultService
import net.moonbaseone.vera.shared.vault.MedicalRecordStore
import net.moonbaseone.vera.shared.vault.SecureKeyStore
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Shared Koin modules.
 * Default vault uses in-memory stores; apps override with Keystore/Keychain + SQLDelight.
 */
fun sharedModules(): List<Module> = listOf(
    module {
        single { SharedAppInfo(appId = "net.moonbaseone.vera") }
        single { MockDataService() }
        single { IdentityShieldService() }
        single<SecureKeyStore> { InMemorySecureKeyStore() }
        single<MedicalRecordStore> { InMemoryMedicalRecordStore() }
        single {
            LocalVaultService(
                keyStore = get(),
                recordStore = get(),
            )
        }
    },
)

fun initKoin(extraDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        modules(sharedModules())
        extraDeclaration()
    }
}

data class SharedAppInfo(val appId: String)
