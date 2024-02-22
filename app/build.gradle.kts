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
    compileSdkPreview = "VanillaIceCream"
    buildToolsVersion = "35.0.0 rc1"

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "de.dertyp7214.rboardpatcher"
        minSdk = 23
        targetSdk = 34
        versionCode = 3201
        versionName = "3.2"

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = JvmTarget.JVM_21.description
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JvmTarget.JVM_21.description
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
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio)
}
