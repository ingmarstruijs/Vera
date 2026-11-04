package net.moonbaseone.vera.shared.di

import net.moonbaseone.vera.shared.fhir.MockDataService
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Shared Koin modules. Vault/shield wiring lands in later phases.
 */
fun sharedModules(): List<Module> = listOf(
    module {
        single { SharedAppInfo(appId = "net.moonbaseone.vera") }
        single { MockDataService() }
    },
)

fun initKoin(extraDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        modules(sharedModules())
        extraDeclaration()
    }
}

data class SharedAppInfo(val appId: String)
