pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kara-do"

// Pure Kotlin modules: build and test anywhere with a JDK.
include(":core:recipes", ":core:engine")

// The Android app needs the Android SDK. CI always has it; a machine without it
// can still build and test the core modules.
val hasAndroidSdk = System.getenv("ANDROID_HOME") != null ||
    System.getenv("ANDROID_SDK_ROOT") != null ||
    file("local.properties").let { it.exists() && it.readText().contains("sdk.dir") }
gradle.extra["hasAndroidSdk"] = hasAndroidSdk
if (hasAndroidSdk) include(":app")
