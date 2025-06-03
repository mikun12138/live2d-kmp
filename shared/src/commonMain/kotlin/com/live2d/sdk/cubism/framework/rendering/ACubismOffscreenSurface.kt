package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismVector2

expect fun ACubismOffscreenSurface.Companion.create(): ACubismOffscreenSurface

abstract class ACubismOffscreenSurface {

     abstract fun beginDraw()
     abstract fun endDraw()

    abstract fun createOffscreenSurface(displayBufferSize: CubismVector2)

    abstract fun isSameSize(bufferSize: CubismVector2): Boolean

    var colorBuffer: IntArray = IntArray(1)

    companion object

}