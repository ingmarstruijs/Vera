package net.moonbaseone.vera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import net.moonbaseone.vera.shared.vault.AndroidBiometricAuthenticator
import net.moonbaseone.vera.shared.vault.BiometricAuthenticator
import net.moonbaseone.vera.ui.VeraApp
import net.moonbaseone.vera.ui.theme.VeraTheme
import org.koin.android.ext.android.getKoin
import org.koin.dsl.module

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activity-scoped biometric gate (BiometricPrompt needs FragmentActivity).
        getKoin().loadModules(
            listOf(
                module {
                    single<BiometricAuthenticator> { AndroidBiometricAuthenticator(this@MainActivity) }
                },
            ),
            allowOverride = true,
        )
        enableEdgeToEdge()
        setContent {
            VeraTheme {
                VeraApp()
            }
        }
    }
}
