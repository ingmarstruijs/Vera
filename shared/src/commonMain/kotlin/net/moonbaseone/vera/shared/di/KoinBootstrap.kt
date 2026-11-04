package net.moonbaseone.vera.shared.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Lightweight Koin stub for P1. Real modules (vault, shield, repositories) come later.
 */
fun sharedModules(): List<Module> = listOf(
    module {
        single { SharedAppInfo(appId = "net.moonbaseone.vera") }
    },
)

fun initKoin(extraDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        modules(sharedModules())
        extraDeclaration()
    }
}

data class SharedAppInfo(val appId: String)
