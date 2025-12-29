pluginManagement {
    repositories {
        google()       // Kizárólag a hivatalos Google szerver
        mavenCentral() // A legbiztonságosabb globális Java tárhely
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()       // Android 14 csomagok forrása
        mavenCentral() // Minden egyéb stabil könyvtár forrása
        // Itt NINCS kínai vagy indiai repo, így azokat nem fogja használni.
    }
}

rootProject.name = "NarrativeTensorExplorer"
include(":app")
