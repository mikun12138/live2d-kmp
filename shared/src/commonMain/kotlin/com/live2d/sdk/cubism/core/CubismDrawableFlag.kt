package com.live2d.sdk.cubism.core

class CubismDrawableFlag {
    object ConstantFlag {
        const val BLEND_ADDITIVE: Byte = 1
        const val BLEND_MULTIPLICATIVE: Byte = 2
        const val IS_DOUBLE_SIDED: Byte = 4
        const val IS_INVERTED_MASK: Byte = 8
    }

    object DynamicFlag {
        const val IS_VISIBLE: Byte = 1
        const val VISIBILITY_DID_CHANGE: Byte = 2
        const val OPACITY_DID_CHANGE: Byte = 4
        const val DRAW_ORDER_DID_CHANGE: Byte = 8
        const val RENDER_ORDER_DID_CHANGE: Byte = 16
        const val VERTEX_POSITIONS_DID_CHANGE: Byte = 32
        const val BLEND_COLOR_DID_CHANGE: Byte = 64
    }
}
