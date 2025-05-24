package com.live2d.sdk.cubism.util

interface Aliveable {
    var isAlive: Boolean

    fun close() {
        isAlive = false
    }
}