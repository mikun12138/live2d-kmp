package me.mikun.sandbox

val resDir = "/home/mikun/workspace/live2d-kmp/sandbox/build/libs/live2DCore/temp/CubismSdkForNative-5-r.4/Samples/Resources/"

val resMocHaru = "Haru"
val resMocHiyori = "Hiyori"
val resMocMao = "Mao"
val resMocMark = "Mark"
val resMocNatori = "Natori"
val resMocRice = "Rice"
val resMocWanko = "Wanko"

fun main() {
    val moc = resMocMao

    live2dMain(
        "$resDir$moc", moc
    )
}