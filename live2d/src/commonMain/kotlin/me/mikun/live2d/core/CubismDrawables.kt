package me.mikun.live2d.core

class CubismDrawables(count: Int) {
    val count: Int
    val ids: Array<String?>
    val constantFlags: ByteArray
    val dynamicFlags: ByteArray
    val textureIndices: IntArray
    val drawOrders: IntArray
    val renderOrders: IntArray
    val opacities: FloatArray
    val maskCounts: IntArray
    val masks: Array<IntArray?>
    val vertexCounts: IntArray
    val vertexPositions: Array<FloatArray?>
    val vertexUvs: Array<FloatArray?>
    val indexCounts: IntArray
    val indices: Array<ShortArray?>
    val multiplyColors: Array<FloatArray?>
    val screenColors: Array<FloatArray?>
    val parentPartIndices: IntArray

    init {
        check(count >= 0)

        this.count = count
        this.ids = arrayOfNulls<String>(count)
        this.constantFlags = ByteArray(count)
        this.dynamicFlags = ByteArray(count)
        this.textureIndices = IntArray(count)
        this.drawOrders = IntArray(count)
        this.renderOrders = IntArray(count)
        this.opacities = FloatArray(count)
        this.maskCounts = IntArray(count)
        this.masks = arrayOfNulls<IntArray>(count)

        for (i in 0..<count) {
            this.masks[i] = IntArray(0)
        }

        this.vertexCounts = IntArray(count)
        this.vertexPositions = arrayOfNulls<FloatArray>(count)
        this.vertexUvs = arrayOfNulls<FloatArray>(count)

        for (i in 0..<count) {
            this.vertexPositions[i] = FloatArray(0)
            this.vertexUvs[i] = FloatArray(0)
        }

        this.indexCounts = IntArray(count)
        this.indices = arrayOfNulls<ShortArray>(count)

        for (i in 0..<count) {
            this.indices[i] = ShortArray(0)
        }

        this.multiplyColors = arrayOfNulls<FloatArray>(count)
        this.screenColors = arrayOfNulls<FloatArray>(count)

        for (i in 0..<count) {
            this.multiplyColors[i] = FloatArray(0)
            this.screenColors[i] = FloatArray(0)
        }

        this.parentPartIndices = IntArray(count)
    }

    companion object {
        private const val COLOR_UNIT = 4
    }
}
