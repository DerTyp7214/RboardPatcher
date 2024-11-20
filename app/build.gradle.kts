import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "de.dertyp7214.rboardpatcher"
    compileSdkPreview = "Baklava"
    buildToolsVersion = "36.0.0-rc1"

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "de.dertyp7214.rboardpatcher"
        minSdk = 23
        targetSdk = 35
        versionCode = 3300
        versionName = "3.3"

        testInstrumentationRunner  = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.current()
        targetCompatibility = JavaVersion.current()
    }

    kotlinOptions {
        jvmTarget = JavaVersion.current().toString()
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
        )
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(project(":colorutilsc"))
    implementation(project(":rboardcomponents"))

    implementation(libs.browser)
    implementation(libs.storage)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.preference.ktx)
    implementation(libs.activity)
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
}
