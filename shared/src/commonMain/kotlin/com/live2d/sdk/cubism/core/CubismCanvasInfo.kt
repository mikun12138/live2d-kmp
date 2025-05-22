package com.live2d.sdk.cubism.core

class CubismCanvasInfo internal constructor(
    sizeInPixels: FloatArray,
    originInPixels: FloatArray,
    pixelsPerUnit: Float
) {
    val sizeInPixels: FloatArray = FloatArray(2)
    val originInPixels: FloatArray = FloatArray(2)
    var pixelsPerUnit: Float = 0.0f
        private set

    init {
        checkNotNull(sizeInPixels)

        checkNotNull(originInPixels)

        check(sizeInPixels.size == 2)

        check(originInPixels.size == 2)

        this.sizeInPixels[0] = sizeInPixels[0]
        this.sizeInPixels[1] = sizeInPixels[1]
        this.originInPixels[0] = originInPixels[0]
        this.originInPixels[1] = originInPixels[1]
        this.pixelsPerUnit = pixelsPerUnit
    }
}