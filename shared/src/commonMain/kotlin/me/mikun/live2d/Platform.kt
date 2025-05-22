package me.mikun.live2d

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform