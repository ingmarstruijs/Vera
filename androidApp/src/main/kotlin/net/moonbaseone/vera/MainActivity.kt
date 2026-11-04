package net.moonbaseone.vera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import net.moonbaseone.vera.ui.VeraApp
import net.moonbaseone.vera.ui.theme.VeraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeraTheme {
                VeraApp()
            }
        }
    }
}
