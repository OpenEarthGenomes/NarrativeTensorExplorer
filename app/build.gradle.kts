android {
    // ... a többi marad ...

    defaultConfig {
        applicationId = "com.meaning.app"
        minSdk = 26
        targetSdk = 34 // Maradjunk a 34-nél, a 35-ös felett már túl szigorú a Samsung
        versionCode = 3 // Emeld meg, hogy a telefon újnak lássa!
        versionName = "1.0.3-A35-STABLE"

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17")
                // Csak ez kell az A35-höz, így kisebb és biztosabb az APK
                abiFilters("arm64-v8a") 
            }
        }
    }

    // Ez segít, hogy a debug verzió is "elfogadhatóbb" legyen a rendszernek
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
        }
    }
}
