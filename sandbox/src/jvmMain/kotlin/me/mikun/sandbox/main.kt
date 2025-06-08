package me.mikun.sandbox

import com.live2d.sdk.cubism.core.Live2DCubismCoreFFM
import me.mikun.live2d.live2dMain
import java.util.Locale.getDefault


enum class Arch(
    val value: String,
) {
    X86_64("x86_64"),
    ARM64("arm64")
    ;

    companion object {
        fun byName(name: String): Arch {
            return when (name) {
                "x86_64", "amd64" -> X86_64
                "aarch64", "arm64" -> ARM64
                else -> error("Unknown arch: [$name]")
            }
        }
    }
}

val arch = Arch.byName(
    System.getProperty("os.arch").lowercase(getDefault())
)
val resDir = System.getProperty("compose.application.resources.dir")
val resDirMoc = "$resDir/moc/"
val resDirLib = "$resDir/lib/${arch.value}"
val resMocHiyori = "Hiyori"

fun main() {

    Live2DCubismCoreFFM.load(resDirLib)

    live2dMain(
        "$resDirMoc$resMocHiyori", resMocHiyori
    )
}