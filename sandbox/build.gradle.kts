import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.tasks.AbstractComposeDesktopTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
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
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.preview)

            implementation(compose.components.resources)

            implementation(projects.live2d)
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

            runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
            runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
            runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
            runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
        }
    }

    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

}

dependencies {
    debugImplementation(compose.uiTooling)
}

android {
    namespace = "me.mikun.live2d"
    compileSdk = 36
    defaultConfig {
        applicationId = "me.mikun.live2d"
        minSdk = 23
        versionCode = 1
        versionName = "1.0"
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
                appResourcesRootDir.set(project.layout.buildDirectory.dir("libs/allRes"))
            }
        }
    }
}

tasks.withType<AbstractComposeDesktopTask> {
    dependsOn("downloadLive2DCore")
}

val allResDirWindows = layout.buildDirectory.dir("libs/allRes/windows")
val resDirMoc = allResDirWindows.get().dir("moc")
val resDirLib = allResDirWindows.get().dir("lib")

tasks.register("downloadLive2DCore") {
    group = "build setup"

    val downloadUrl = "https://cubism.live2d.com/sdk-native/bin/CubismSdkForNative-5-r.4.zip"

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
            from(
                zipTree(archiveFile)
            )
            into(
                downloadTempDir
            )
        }

        /*
            windows
         */

        // dll
        copy {
            from(
                downloadTempDir.get().dir("CubismSdkForNative-5-r.4/Core/dll/windows")
            )
            into(
                resDirLib
            )
        }

        // res
        copy {
            from(
                downloadTempDir.get().dir("CubismSdkForNative-5-r.4/Samples/Resources")
            )
            into(
                resDirMoc
            )
        }

    }
}



