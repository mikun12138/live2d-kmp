package com.live2d.sdk.cubism.core

class CubismParts internal constructor(count: Int) {
    val count: Int
    val ids: Array<String?>
    val parentPartIndices: IntArray
    val opacities: FloatArray

    init {
        check(count >= 0)

        this.count = count
        this.ids = arrayOfNulls<String>(count)
        this.opacities = FloatArray(count)
        this.parentPartIndices = IntArray(count)
    }
}
