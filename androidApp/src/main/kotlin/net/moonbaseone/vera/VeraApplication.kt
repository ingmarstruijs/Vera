package net.moonbaseone.vera

import android.app.Application
import net.moonbaseone.vera.shared.di.initKoin
import net.moonbaseone.vera.shared.speech.AndroidSpeechToText
import net.moonbaseone.vera.shared.speech.AndroidTextToSpeech
import net.moonbaseone.vera.shared.speech.SpeechToText
import net.moonbaseone.vera.shared.speech.TextToSpeech
import net.moonbaseone.vera.shared.vault.AndroidSecureKeyStore
import net.moonbaseone.vera.shared.vault.MedicalRecordStore
import net.moonbaseone.vera.shared.vault.SecureKeyStore
import net.moonbaseone.vera.shared.vault.SqlDelightMedicalRecordStore
import net.moonbaseone.vera.shared.vault.createAndroidVeraDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class VeraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@VeraApplication)
            allowOverride(true)
            modules(
                module {
                    single<SecureKeyStore> { AndroidSecureKeyStore(get()) }
                    single<MedicalRecordStore> {
                        SqlDelightMedicalRecordStore(createAndroidVeraDatabase(get()))
                    }
                    single<SpeechToText> { AndroidSpeechToText(get()) }
                    single<TextToSpeech> { AndroidTextToSpeech(get()) }
                },
            )
        }
    }
}
