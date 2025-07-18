import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "de.dertyp7214.rboardpatcher"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "de.dertyp7214.rboardpatcher"
        minSdk = 26
        targetSdk = 36
        versionCode = 3500
        versionName = "3.5"

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
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs = freeCompilerArgs.get() + listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true",
                "-Xsuppress-version-warnings"
            )
            jvmToolchain(23)
            jvmTarget.set(JvmTarget.JVM_23)
        }
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
