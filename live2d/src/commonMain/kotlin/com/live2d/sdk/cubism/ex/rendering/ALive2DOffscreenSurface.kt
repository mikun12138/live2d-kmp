package com.live2d.sdk.cubism.ex.rendering

import com.live2d.sdk.cubism.framework.math.CubismVector2

abstract class ACubismOffscreenSurface {

    fun draw(
        block: () -> Unit
    ) {
        beginDraw().takeIf { it }?.let {

            block()

            endDraw()
        }
    }
    /**
     * return false if failed to bind framebuffer
     */
     abstract fun beginDraw(): Boolean
    abstract fun endDraw()

    abstract fun createOffscreenSurface(displayBufferSize: CubismVector2)

    abstract fun isSameSize(bufferSize: CubismVector2): Boolean

    var colorBuffer: IntArray = IntArray(1)

    companion object

}