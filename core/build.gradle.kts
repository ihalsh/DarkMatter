import org.gradle.api.JavaVersion.*

plugins {
    kotlin("jvm")
}

dependencies {
    val gdxVersion: String by project
    val ktxVersion: String by project
    val ashleyVersion: String by project
    val junitVersion: String by project
    val coroutinesVersion : String by project

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.ashley:ashley:$ashleyVersion")

    // LibKTX kotlin extensions, optional but recommended.
    // The complete list of modules is available at https://github.com/libktx/ktx
    implementation("io.github.libktx:ktx-app:$ktxVersion")
    implementation("io.github.libktx:ktx-graphics:$ktxVersion")
    implementation("io.github.libktx:ktx-ashley:$ktxVersion")
    implementation("io.github.libktx:ktx-async:$ktxVersion")
    implementation("io.github.libktx:ktx-assets-async:$ktxVersion")
    implementation("io.github.libktx:ktx-collections:$ktxVersion")
    implementation("io.github.libktx:ktx-math:$ktxVersion")
    implementation("io.github.libktx:ktx-log:$ktxVersion")
    implementation("io.github.libktx:ktx-style:$ktxVersion")

    // If you're using https://github.com/BlueBoxWare/LibGDXPlugin
    // this dependency provides the @GDXAssets annotation.
    compileOnly("com.gmail.blueboxware:libgdxpluginannotations:1.16")
    
    testImplementation("junit:junit:$junitVersion")
}

java {
    sourceCompatibility = VERSION_1_6
    targetCompatibility = VERSION_1_6
}