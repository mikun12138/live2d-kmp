package com.live2d.sdk.cubism.framework.rendering

import java.nio.ByteBuffer

expect fun ALive2DTexture.Companion.create(
    buffer: ByteBuffer
): ALive2DTexture

abstract class ALive2DTexture(
    val id: Int,
    val isPremultipliedAlpha: Boolean,
) {
    companion object
}