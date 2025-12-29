pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Kényszeríti, hogy minden modul ugyanazokat a forrásokat használja
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// A projekt neve a fejlesztői környezetben
rootProject.name = "NarrativeTensorExplorer"

// Csak az app modult vesszük bele, minden mást töröltünk
include(":app")
