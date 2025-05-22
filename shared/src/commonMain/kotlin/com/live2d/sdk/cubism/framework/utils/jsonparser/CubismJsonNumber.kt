/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.utils.jsonparser

import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.String

/**
 * This class expresses a number value.
 * It has double type number.
 */
internal class CubismJsonNumber private constructor(
    /**
     * number value
     */
    private val value: Double
) : ACubismJsonValue() {
    override fun getString(defaultValue: String?, indent: String?): String? {
        return stringBuffer
    }


    override fun toInt(): Int {
        return value.toInt()
    }

    override fun toInt(defaultValue: Int): Int {
        return value.toInt()
    }

    override fun toFloat(): Float {
        return value.toFloat()
    }

    override fun toFloat(defaultValue: Float): Float {
        return value.toFloat()
    }

    override val isNumber: Boolean
        get() = true

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as CubismJsonNumber

        return that.value.toFloat().compareTo(value.toFloat()) == 0
    }

    override fun hashCode(): Int {
        val temp = value.toFloat().toBits().toLong()
        return (temp xor (temp ushr 32)).toInt()
    }

    /**
     * private constructor
     *
     * @param value number value
     */
    init {
        stringBuffer = value.toString()
    }

    companion object {
        /**
         * Get an instance of number type.
         *
         * @param value number value
         * @return an instance of number type
         */
        fun valueOf(value: Double): CubismJsonNumber {
            return CubismJsonNumber(value)
        }
    }
}
