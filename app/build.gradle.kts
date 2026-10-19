plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.differentsyntax.karado"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.differentsyntax.karado"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    // Recipes ship inside the APK as assets/recipes/** (ADR-0005: no downloads).
    sourceSets["main"].assets.srcDir(layout.buildDirectory.dir("generated/recipeAssets"))

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

// Test fixtures stay in the repo, not in the app.
val copyRecipes by tasks.registering(Sync::class) {
    from(rootProject.file("recipes")) { exclude("fixtures/**") }
    into(layout.buildDirectory.dir("generated/recipeAssets/recipes"))
}
tasks.named("preBuild") { dependsOn(copyRecipes) }

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":core:engine"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
}
