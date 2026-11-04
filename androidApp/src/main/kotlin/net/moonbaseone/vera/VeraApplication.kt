package net.moonbaseone.vera

import android.app.Application
import net.moonbaseone.vera.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class VeraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@VeraApplication)
        }
    }
}
