@file:Suppress("UNUSED_VARIABLE")

buildscript {
    val appVersionCode by extra(1)
    val appVersion by extra("1.0.0")

    val kotlinVersion by extra("1.3.72")
    val coroutinesVersion by extra("1.3.5")
    val gdxVersion by extra("1.9.10")
    val ktxVersion by extra("1.9.10-b6")
    val junitVersion by extra("4.12")

    repositories {
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.3")
        classpath(kotlin("gradle-plugin", kotlinVersion))
    }
}

plugins {
    base
}

allprojects {
    repositories {
        jcenter()
        google()
    }
}
