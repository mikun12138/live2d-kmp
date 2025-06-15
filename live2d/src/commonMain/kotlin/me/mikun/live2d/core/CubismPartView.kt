package me.mikun.live2d.core

class CubismPartView internal constructor(
    val index: Int,
    val parts: CubismParts,
) {

    val id: String?
        get() = this.parts.ids[this.index]

    var opacity: Float
        get() = this.parts.opacities[this.index]
        set(opacity) {
            this.parts.opacities[this.index] = opacity
        }

    val parentPartIndex: Int
        get() = this.parts.parentPartIndices[this.index]

}
