@file:Suppress("UNUSED_VARIABLE")
plugins {
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinAndroid) apply false
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.fabric.io/public")
        maven("https://maven.google.com")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath(libs.gradle)
        //noinspection DifferentKotlinGradleVersion
        classpath(kotlin("gradle-plugin", version = "2.2.0"))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.fabric.io/public")
        maven("https://maven.google.com")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}