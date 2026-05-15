/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.math

/**
 * Column-Major Order Matrix
 */
open class CubismMatrix44 {

    constructor() {
        this.tr = FloatArray(16)
        loadIdentity()
    }

    protected constructor(matrix: FloatArray) {
        this.tr = FloatArray(16)
        set(matrix)
    }
    var tr: FloatArray
        protected set

    fun loadIdentity() {
        resetMatrixForCalcToUnitMatrix()
        set(matrixForCalculation)
    }

    fun set(matrix: CubismMatrix44) {
        System.arraycopy(matrix.tr, 0, this.tr, 0, 16)
    }

    fun set(matrix: FloatArray) {
        System.arraycopy(matrix, 0, this.tr, 0, 16)
    }

    var scaleX: Float
        get() = this.tr[0]
        set(value) {
            this.tr[0] = value
        }

    var scaleY: Float
        get() = this.tr[5]
        set(value) {
            this.tr[5] = value
        }

    var translateX: Float
        get() = this.tr[12]
        set(value) {
            this.tr[12] = value
        }

    var translateY: Float
        get() = this.tr[13]
        set(value) {
            this.tr[13] = value
        }

    fun transformX(src: Float): Float {
        return this.tr[0] * src + this.tr[12]
    }

    fun transformY(src: Float): Float {
        return this.tr[5] * src + this.tr[13]
    }

    fun translateRelative(x: Float, y: Float) {
        resetMatrixForCalcToUnitMatrix()
        matrixForCalculation[12] = x
        matrixForCalculation[13] = y

        multiply(
            matrixForCalculation,
            this.tr,
            this.tr
        )
    }

    fun translate(x: Float, y: Float) {
        this.tr[12] = x
        this.tr[13] = y
    }

    fun scaleRelative(x: Float, y: Float) {
        resetMatrixForCalcToUnitMatrix()
        matrixForCalculation[0] = x
        matrixForCalculation[5] = y
        multiply(
            matrixForCalculation,
            this.tr,
            this.tr
        )
    }

    fun scale(x: Float, y: Float) {
        this.tr[0] = x
        this.tr[5] = y
    }


    companion object {
        fun multiply(multiplicand: FloatArray?, multiplier: FloatArray?, dst: FloatArray?) {

            for (i in 0..15) {
                matrixForMultiplication[i] = 0.0f
            }
            for (i in 0..3) {
                for (j in 0..3) {
                    for (k in 0..3) {
                        matrixForMultiplication[j + i * 4] += multiplicand!![k + i * 4] * multiplier!![j + k * 4]
                    }
                }
            }

            System.arraycopy(matrixForMultiplication, 0, dst, 0, 16)
        }

        private fun resetMatrixForCalcToUnitMatrix() {
            for (i in 0..15) {
                matrixForCalculation[i] = 0.0f
            }
            matrixForCalculation[0] = 1.0f
            matrixForCalculation[5] = 1.0f
            matrixForCalculation[10] = 1.0f
            matrixForCalculation[15] = 1.0f
        }

        private val matrixForCalculation = FloatArray(16)

        private val matrixForMultiplication = FloatArray(16)
    }
}
