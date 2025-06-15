package me.mikun.sandbox

import me.mikun.live2d.core.Live2DCubismCoreFFM
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
val resMocHaru = "Haru"
val resMocHiyori = "Hiyori"
val resMocMao = "Mao"
val resMocMark = "Mark"
val resMocNatori = "Natori"
val resMocRice = "Rice"
val resMocWanko = "Wanko"

fun main() {

    Live2DCubismCoreFFM.load(resDirLib)

    val moc = resMocHiyori

    live2dMain(
        "$resDirMoc$moc", moc
    )
}