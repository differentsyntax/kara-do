// Plugins go on one shared classpath here (rather than in a plugins {} block) so that
// the Android Gradle Plugin is only fetched when the Android SDK is present. That keeps
// :core:* buildable on machines without the SDK, while Kotlin and AGP still share
// a classloader when :app is built.
buildscript {
    val kotlinVersion = "2.2.20"
    val agpVersion = "8.13.0"
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
        if (gradle.extra["hasAndroidSdk"] == true) {
            classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:$kotlinVersion")
            classpath("com.android.tools.build:gradle:$agpVersion")
        }
    }
}
