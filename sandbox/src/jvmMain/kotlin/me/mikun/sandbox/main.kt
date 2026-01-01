package me.mikun.sandbox

import arch
import me.mikun.live2d.opengl.opengl
import me.mikun.live2d.skiko.skiko


val resDir = System.getProperty("compose.application.resources.dir")
val resDirMoc = "$resDir/moc/"
val resDirLib = "$resDir/lib/${arch.value}"
val resMocHaru = "Haru"
val resMocHiyori = "Hiyori"
val resMocMao = "Mao"
val resMocMark = "Mark"
val resMocNatori = "Natori"
val resMocRice = "Rice"
val resMocWanko = "Wanko"

fun main() {

    val moc = resMocMao

    when (backend) {
        Backends.OpenGL -> {
            opengl(
                "$resDirMoc$moc", moc
            )
        }

        Backends.Skiko -> {
            skiko(
                "$resDirMoc$moc", moc
            )
        }
    }
}

enum class Backends {
    OpenGL, Skiko
}

val backend = Backends.Skiko

