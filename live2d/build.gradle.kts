import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kmp)
    alias(libs.plugins.android.kmp.library)

    kotlin("plugin.serialization") version "2.3.21"

    id("com.vanniktech.maven.publish") version "0.32.0"
}

group = "me.mikun"
version = "0.0.1"

kotlin {

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_25)
                }
            }
        }
    }

    android {
        namespace = "me.mikun.live2d"
        compileSdk = 36
        minSdk = 26

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }
    }


//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach {
//        it.binaries.framework {
//            baseName = "shared"
//            isStatic = true
//        }
//    }

    sourceSets {
        androidMain.dependencies {
            implementation("io.github.vova7878.panama:Core:v0.1.3")
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

}


