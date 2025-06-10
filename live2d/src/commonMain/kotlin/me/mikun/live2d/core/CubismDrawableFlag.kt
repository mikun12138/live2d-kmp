package me.mikun.live2d.core

object CubismDrawableFlag {
    enum class ConstantFlag(
        val value: Byte
    ) {
        BLEND_ADDITIVE(1),
        BLEND_MULTIPLICATIVE(2),
        IS_DOUBLE_SIDED(4),
        IS_INVERTED_MASK(8)
    }

    enum class DynamicFlag(
        val value: Byte
    ) {
         IS_VISIBLE(1),
         VISIBILITY_DID_CHANGE(2),
         OPACITY_DID_CHANGE(4),
         DRAW_ORDER_DID_CHANGE(8),
         RENDER_ORDER_DID_CHANGE(16),
         VERTEX_POSITIONS_DID_CHANGE(32),
         BLEND_COLOR_DID_CHANGE(64)
    }
}
