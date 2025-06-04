package com.live2d.sdk.cubism.framework.rendering

import java.nio.ByteBuffer

expect fun ALive2DTexture.create(buffer: ByteBuffer): ALive2DTexture

abstract class ALive2DTexture {

    abstract fun load(buffer: ByteBuffer)

    companion object
}