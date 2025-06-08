package me.mikun.sandbox

import com.live2d.sdk.cubism.core.Live2DCubismCoreFFM
import me.mikun.live2d.live2dMain


val resDir = System.getProperty("compose.application.resources.dir")
val resDirMoc = "$resDir/moc/"
val resDirLib = "$resDir/lib/x86_64"
val resMocHiyori = "Hiyori"

fun main() {

    Live2DCubismCoreFFM.load(resDirLib)

    live2dMain(
        "$resDirMoc$resMocHiyori", resMocHiyori
    )
}