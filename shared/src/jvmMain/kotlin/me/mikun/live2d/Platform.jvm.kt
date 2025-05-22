package me.mikun.live2d

class JvmPlatform : Platform {
    override val name: String = "jvm"
}

actual fun getPlatform(): Platform = JvmPlatform()
