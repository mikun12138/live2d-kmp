package com.live2d.sdk.cubism.framework.rendering

expect fun ALive2DTexture.Companion.create(
    bytes: ByteArray
): ALive2DTexture

abstract class ALive2DTexture(
    val id: Int,
    val isPremultipliedAlpha: Boolean,
) {
    companion object
}