package com.meaning.app

import android.app.Application
import com.meaning.app.kernel.QuantizationEngine
import timber.log.Timber

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. C++ Motor (NEON Kernel) előmelegítése
        // Így az app indulásakor azonnal betöltődik a memória
        try {
            System.loadLibrary("meaning-kernel")
            android.util.Log.i("MainApplication", "Native Kernel loaded successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainApplication", "Native Kernel loading failed", e)
        }

        // 2. Naplózó rendszer (Timber) inicializálása
        // Csak Debug módban logolunk, Release-ben nem szivárog adat
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
