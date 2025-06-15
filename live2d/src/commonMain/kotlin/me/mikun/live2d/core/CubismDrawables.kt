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
    val masks: Array<IntArray>
    val vertexCounts: IntArray
    val vertexPositions: Array<FloatArray>
    val vertexUvs: Array<FloatArray>
    val indexCounts: IntArray
    val indices: Array<ShortArray>
    val multiplyColors: Array<FloatArray>
    val screenColors: Array<FloatArray>
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
        this.masks = Array<IntArray>(count) {
            IntArray(0)
        }

        this.vertexCounts = IntArray(count)
        this.vertexPositions = Array<FloatArray>(count) {
            FloatArray(0)
        }
        this.vertexUvs = Array<FloatArray>(count) {
            FloatArray(0)
        }

        this.indexCounts = IntArray(count)
        this.indices = Array<ShortArray>(count) {
            ShortArray(0)
        }

        this.multiplyColors = Array<FloatArray>(count) {
            FloatArray(0)
        }
        this.screenColors = Array<FloatArray>(count) {
            FloatArray(0)
        }

        this.parentPartIndices = IntArray(count)
    }

    companion object {
        private const val COLOR_UNIT = 4
    }
}
