package me.mikun.live2d.core

class CubismDrawableView internal constructor(
    val index: Int,
    val drawables: CubismDrawables
) {

    val id: String?
        get() = this.drawables.ids[this.index]

    val constantFlag: Byte
        get() = this.drawables.constantFlags[this.index]

    val dynamicFlag: Byte
        get() = this.drawables.dynamicFlags[this.index]

    val textureIndex: Int
        get() = this.drawables.textureIndices[this.index]

    val drawOrder: Int
        get() = this.drawables.drawOrders[this.index]

    val renderOrder: Int
        get() = this.drawables.renderOrders[this.index]

    val opacity: Float
        get() = this.drawables.opacities[this.index]

    val masks: IntArray
        get() = this.drawables.masks[this.index]

    val vertexCount: Int
        get() = this.drawables.vertexCounts[this.index]

    val vertexPositions: FloatArray
        get() = this.drawables.vertexPositions[this.index]

    val vertexUvs: FloatArray
        get() = this.drawables.vertexUvs[this.index]

    val indices: ShortArray
        get() = this.drawables.indices[this.index]

    val multiplyColors: FloatArray
        get() = this.drawables.multiplyColors[this.index]

    val screenColors: FloatArray
        get() = this.drawables.screenColors[this.index]

    val parentPartIndex: Int
        get() = this.drawables.parentPartIndices[this.index]
}
