package net.moonbaseone.vera.shared.di

import net.moonbaseone.vera.shared.appointments.AppointmentsViewModel
import net.moonbaseone.vera.shared.appointments.ContactsViewModel
import net.moonbaseone.vera.shared.auth.AuthViewModel
import net.moonbaseone.vera.shared.blueprint.BlueprintViewModel
import net.moonbaseone.vera.shared.careplan.CarePlanRepository
import net.moonbaseone.vera.shared.careplan.LocalCarePlanRepository
import net.moonbaseone.vera.shared.decoder.DecoderViewModel
import net.moonbaseone.vera.shared.fhir.ActiveDatasetStore
import net.moonbaseone.vera.shared.fhir.FhirClient
import net.moonbaseone.vera.shared.fhir.MockDataService
import net.moonbaseone.vera.shared.fhir.UnsupportedFhirClient
import net.moonbaseone.vera.shared.navigation.AppNavigator
import net.moonbaseone.vera.shared.navigation.DefaultAppNavigator
import net.moonbaseone.vera.shared.pocket.AppointmentAudioPlayer
import net.moonbaseone.vera.shared.pocket.AppointmentAudioRecorder
import net.moonbaseone.vera.shared.pocket.FakeAppointmentAudioPlayer
import net.moonbaseone.vera.shared.pocket.FakeAppointmentAudioRecorder
import net.moonbaseone.vera.shared.pocket.InMemoryPocketGuideStore
import net.moonbaseone.vera.shared.pocket.PocketGuideStore
import net.moonbaseone.vera.shared.pocket.PocketGuideViewModel
import net.moonbaseone.vera.shared.shield.IdentityShieldService
import net.moonbaseone.vera.shared.shield.NerEngine
import net.moonbaseone.vera.shared.shield.NoOpNerEngine
import net.moonbaseone.vera.shared.speech.FakeSpeechToText
import net.moonbaseone.vera.shared.speech.FakeTextToSpeech
import net.moonbaseone.vera.shared.speech.SpeechToText
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.symptoms.InMemorySymptomStore
import net.moonbaseone.vera.shared.symptoms.SymptomStore
import net.moonbaseone.vera.shared.symptoms.SymptomsViewModel
import net.moonbaseone.vera.shared.translation.FakeOnDeviceTranslator
import net.moonbaseone.vera.shared.translation.LocalTranslationService
import net.moonbaseone.vera.shared.translation.OnDeviceTranslator
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

fun sharedModules(): List<Module> = listOf(
    coreModule(),
    vaultModule(),
    shieldModule(),
    carePlanModule(),
    decoderModule(),
    appointmentsModule(),
)

fun coreModule(): Module = module {
    single { SharedAppInfo(appId = "net.moonbaseone.vera") }
    single { ActiveDatasetStore() }
    single { MockDataService(datasetStore = get()) }
    single<FhirClient> { UnsupportedFhirClient() }
    single { DefaultAppNavigator() }
    single<AppNavigator> { get<DefaultAppNavigator>() }
    single<SymptomStore> { InMemorySymptomStore() }
    single<PocketGuideStore> { InMemoryPocketGuideStore() }
}

fun vaultModule(): Module = module {
    single<SecureKeyStore> { InMemorySecureKeyStore() }
    single<MedicalRecordStore> { InMemoryMedicalRecordStore() }
    single<BiometricAuthenticator> { FakeBiometricAuthenticator() }
    single {
        LocalVaultService(
            keyStore = get(),
            recordStore = get(),
            biometricAuthenticator = get(),
        )
    }
    single { AuthViewModel(vault = get()) }
}

fun shieldModule(): Module = module {
    single<NerEngine> { NoOpNerEngine() }
    single { IdentityShieldService(ner = get()) }
}

fun carePlanModule(): Module = module {
    single<CarePlanRepository> { LocalCarePlanRepository(mockData = get()) }
    single { BlueprintViewModel(carePlanRepository = get(), datasetStore = get()) }
}

fun decoderModule(): Module = module {
    single<OnDeviceTranslator> { FakeOnDeviceTranslator() }
    single { LocalTranslationService(engine = get(), shield = get()) }
    single { DecoderViewModel(translation = get()) }
}

fun appointmentsModule(): Module = module {
    single<SpeechToText> { FakeSpeechToText() }
    single<TextToSpeech> { FakeTextToSpeech() }
    single<AppointmentAudioRecorder> { FakeAppointmentAudioRecorder() }
    single<AppointmentAudioPlayer> { FakeAppointmentAudioPlayer() }
    single {
        AppointmentsViewModel(
            carePlanRepository = get(),
            datasetStore = get(),
            pocketGuideStore = get(),
        )
    }
    single {
        SymptomsViewModel(
            symptomStore = get(),
            speechToText = get(),
            textToSpeech = get(),
        )
    }
    single {
        ContactsViewModel(carePlanRepository = get())
    }
    factory {
        PocketGuideViewModel(
            carePlanRepository = get(),
            pocketGuideStore = get(),
            symptomStore = get(),
            audioRecorder = get(),
            audioPlayer = get(),
            textToSpeech = get(),
        )
    }
}

fun initKoin(extraDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        modules(sharedModules())
        extraDeclaration()
    }
}

data class SharedAppInfo(val appId: String)
