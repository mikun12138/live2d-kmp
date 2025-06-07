import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.tasks.AbstractComposeDesktopTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
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
            implementation(compose.runtime)

            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {

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

compose.desktop {
    application {
        mainClass = "me.mikun.sandbox.MainKt"

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "sandbox"
            packageVersion = "1.0.0"
            windows {
                appResourcesRootDir.set(project.layout.buildDirectory.dir("libs/live2DCore"))
            }
        }
    }
}

tasks.register<DefaultTask>("downloadLive2DCore") {
    group = "build setup"

    val downloadUrl = "https://cubism.live2d.com/sdk-native/bin/CubismSdkForNative-5-r.4.zip"
    val outputDir = layout.buildDirectory.dir("libs/live2DCore")

    val downloadTempDir = layout.buildDirectory.dir("libs/live2DCore/temp")
    val archiveFile = downloadTempDir.get().file("CubismSdkForNative-5-r.4.zip").asFile

    doLast {
        if (!archiveFile.exists()) {
            downloadTempDir.get().asFile.mkdirs()

            // download
            logger.lifecycle("downloading \"https://cubism.live2d.com/sdk-native/bin/CubismSdkForNative-5-r.4.zip\"")
            uri(downloadUrl).toURL().openStream().use { input ->
                archiveFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        // unzip
        copy {
            from(zipTree(archiveFile))
            into(downloadTempDir)
        }

        copy {
            from(
                downloadTempDir.get().dir("CubismSdkForNative-5-r.4/Core/dll")
            )
            into(outputDir)
        }

    }
}



