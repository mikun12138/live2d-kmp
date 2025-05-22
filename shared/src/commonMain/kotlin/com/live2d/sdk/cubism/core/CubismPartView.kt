package com.live2d.sdk.cubism.core

class CubismPartView internal constructor(index: Int, parts: CubismParts) {
    val index: Int
    private val parts: CubismParts

    init {
        check(index >= 0)

        checkNotNull(parts)

        this.index = index
        this.parts = parts
    }

    val id: String?
        get() = this.parts.ids[this.index]

    var opacity: Float
        get() = this.parts.opacities[this.index]
        set(opacity) {
            this.parts.opacities[this.index] = opacity
        }

    val parentPartIndex: Int
        get() = this.parts.parentPartIndices[this.index]

    fun getParts(): CubismParts {
        return this.parts
    }
}
