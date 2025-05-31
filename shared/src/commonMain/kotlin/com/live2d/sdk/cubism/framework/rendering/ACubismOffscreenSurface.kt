package com.live2d.sdk.cubism.framework.rendering

expect fun ACubismOffscreenSurface.Companion.create(): ACubismOffscreenSurface

abstract class ACubismOffscreenSurface {

    fun draw(drawing: () -> Unit) {
        beginDraw()
        run {
            drawing()
        }
        endDraw()
    }

    protected abstract fun beginDraw()
    protected abstract fun endDraw()

    abstract fun createOffscreenSurface(
        clippingMaskBufferSizeX: Float,
        clippingMaskBufferSizeY: Float,
    )

    companion object

}