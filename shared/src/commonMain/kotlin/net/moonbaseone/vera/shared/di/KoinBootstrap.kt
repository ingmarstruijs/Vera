package net.moonbaseone.vera.shared.di

import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.auth.AuthViewModel
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.decoder.DecoderViewModel
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.shield.IdentityShieldService
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.speech.SpeechToText
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.vault.BiometricAuthenticator
import net.moonbaseone.vera.shared.vault.FakeBiometricAuthenticator
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
 * Android/iOS apps override [SecureKeyStore] / [MedicalRecordStore] / speech with platform actuals.
 */
fun sharedModules(): List<Module> = listOf(
    module {
        single { SharedAppInfo(appId = "net.moonbaseone.vera") }
        single { MockDataService() }
        single { IdentityShieldService() }
        single<SecureKeyStore> { InMemorySecureKeyStore() }
        single<MedicalRecordStore> { InMemoryMedicalRecordStore() }
        single<BiometricAuthenticator> { FakeBiometricAuthenticator() }
        single<SpeechToText> { FakeSpeechToText() }
        single<TextToSpeech> { FakeTextToSpeech() }
        single {
            LocalVaultService(
                keyStore = get(),
                recordStore = get(),
                biometricAuthenticator = get(),
            )
        }
        single { AuthViewModel(vault = get()) }
        single { BlueprintViewModel(mockData = get()) }
        single { DecoderViewModel() }
        single {
            AppointmentsViewModel(
                mockData = get(),
                speechToText = get(),
                textToSpeech = get(),
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
