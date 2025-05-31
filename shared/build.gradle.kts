import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "2.1.20"
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
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
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val lwjglVersion = "3.3.6"
        val lwjglNatives = "natives-windows"

        jvmMain.dependencies {
            implementation(project.dependencies.platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

            implementation("org.lwjgl:lwjgl:${lwjglVersion}")
            implementation("org.lwjgl:lwjgl-glfw:${lwjglVersion}")
            implementation("org.lwjgl:lwjgl-opengl:${lwjglVersion}")
            implementation("org.lwjgl:lwjgl-stb:${lwjglVersion}")

            // 添加对应平台的原生库（根据你的操作系统选择）
            runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
            runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
            runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
            runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
        }
    }

    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

}

android {
    namespace = "me.mikun.live2d"
    compileSdk = 36
    defaultConfig {
        minSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

