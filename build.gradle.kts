// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.2.0" apply false
    kotlin("jvm") version "1.6.20" // or kotlin("multiplatform") or any other kotlin plugin
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        val kotlinVersion = "1.6.20"
        classpath(kotlin("gradle-plugin"))
    }
}

