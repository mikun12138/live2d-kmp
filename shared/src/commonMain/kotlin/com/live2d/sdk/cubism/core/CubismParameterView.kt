package com.live2d.sdk.cubism.core

import com.live2d.sdk.cubism.core.CubismParameters.ParameterType

class CubismParameterView internal constructor(
    val index: Int,
    val parameters: CubismParameters
) {

    val id: String?
        get() = this.parameters.ids[this.index]

    val type: ParameterType?
        get() = this.parameters.types[this.index]

    val minimumValue: Float
        get() = this.parameters.minimumValues[this.index]

    val maximumValue: Float
        get() = this.parameters.maximumValues[this.index]

    val defaultValue: Float
        get() = this.parameters.defaultValues[this.index]

    var value: Float
        get() = this.parameters.values[this.index]
        set(value) {
            this.parameters.values[this.index] = value
        }

    val keyCount: Int
        get() = this.parameters.keyCounts[this.index]

    val keyValue: FloatArray?
        get() = this.parameters.keyValues[this.index]

}
