package me.mikun.live2d.core

class CubismParameters(count: Int) {
    val count: Int
    val ids: Array<String?>
    val types: Array<ParameterType?>
    val minimumValues: FloatArray
    val maximumValues: FloatArray
    val defaultValues: FloatArray
    val keyCounts: IntArray
    val keyValues: Array<FloatArray?>
    val values: FloatArray

    init {
        check(count >= 0)

        this.count = count
        this.ids = arrayOfNulls<String>(count)
        this.types = arrayOfNulls<ParameterType>(count)
        this.minimumValues = FloatArray(count)
        this.maximumValues = FloatArray(count)
        this.defaultValues = FloatArray(count)
        this.values = FloatArray(count)
        this.keyCounts = IntArray(count)
        this.keyValues = arrayOfNulls<FloatArray>(count)

        for (i in 0..<count) {
            this.keyValues[i] = FloatArray(0)
        }
    }

    enum class ParameterType(val number: Int) {
        NORMAL(0),
        BLEND_SHAPE(1);

        companion object {
            fun toType(parameterType: Int): ParameterType {
                for (value in ParameterType.entries) {
                    if (parameterType == value.number) {
                        return value
                    }
                }

                throw IllegalArgumentException(
                    "Invalid number that does not exist in the ParameterType: $parameterType",
                )
            }
        }
    }
}
