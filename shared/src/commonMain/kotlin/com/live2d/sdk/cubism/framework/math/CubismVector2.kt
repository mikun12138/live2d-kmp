/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.math

import kotlin.math.pow

/**
 * This class offers 2D vector function.
 */
class CubismVector2 {
    /**
     * Constructor.
     */
    constructor()

    /**
     * Constructor.
     *
     * @param x x-value
     * @param y y-value
     */
    constructor(x: Float, y: Float) {
        set(x, y)
    }

    /**
     * Copy constructor.
     *
     * @param vec the vector which is copied
     */
    constructor(vec: CubismVector2) {
        set(vec)
    }

    /**
     * Add the given vector to this vector.
     *
     * @param vec the added vector
     * @return calculated result
     */
    fun add(vec: CubismVector2): CubismVector2 {
        this.x += vec.x
        this.y += vec.y

        return this
    }

    /**
     * Subtract the given vector from this.
     *
     * @param vec the subtracted vector
     * @return the calculated result
     */
    fun subtract(vec: CubismVector2): CubismVector2 {
        this.x -= vec.x
        this.y -= vec.y

        return this
    }

    /**
     * Multiply this vector by the given vector.
     *
     * @param vec multiplier
     * @return the calculated result
     */
    fun multiply(vec: CubismVector2): CubismVector2 {
        x *= vec.x
        y *= vec.y

        return this
    }

    /**
     * Multiply this vector by a scalar value.
     *
     * @param scalar a scalar value
     * @return a calculated value
     */
    fun multiply(scalar: Float): CubismVector2 {
        x *= scalar
        y *= scalar

        return this
    }

    /**
     * Divide this vector by the given vector.
     *
     * @param vec divisor
     * @return the calculated result
     */
    fun divide(vec: CubismVector2): CubismVector2 {
        x /= vec.x
        y /= vec.y

        return this
    }

    /**
     * Divide this vector by a scalar value.
     *
     * @param scalar a scalar value
     * @return a calculated value
     */
    fun divide(scalar: Float): CubismVector2 {
        this.x /= scalar
        this.y /= scalar

        return this
    }

    /**
     * Normalize this vector.
     */
    fun normalize() {
        val length = (((x * x) + (y * y)).toDouble().pow(0.5)).toFloat()

        x /= length
        y /= length
    }

    val length: Float
        /**
         * Get the length of this vector.
         *
         * @return the length of this vector
         */
        get() = CubismMath.sqrtF(x * x + y * y)

    /**
     * Get a distance between vectors.
     *
     * @param vec a position vector
     * @return a distance between vectors
     */
    fun getDistanceWith(vec: CubismVector2): Float {
        return CubismMath.sqrtF(((this.x - vec.x) * (this.x - vec.x)) + ((this.y - vec.y) * (this.y - vec.y)))
    }

    /**
     * Calculate dot product
     *
     * @param vec a vector
     * @return a calculated result
     */
    fun dot(vec: CubismVector2): Float {
        return (this.x * vec.x) + (this.y * vec.y)
    }

    /**
     * 与えられたベクトルのx, yの値をこのベクトルのx, yに設定する。
     *
     * @param vec copied vector
     * @return this vector for chaining
     */
    fun set(vec: CubismVector2): CubismVector2 {
        this.x = vec.x
        this.y = vec.y

        return this
    }

    /**
     * 与えられたx, yの値をこのベクトルのx, yに設定する。
     *
     * @param x x value
     * @param y y value
     * @return this vector for chaining
     */
    fun set(x: Float, y: Float): CubismVector2 {
        this.x = x
        this.y = y

        return this
    }

    /**
     * Sets the components of this vector to 0.
     *
     * @return this vector for chaining
     */
    fun setZero(): CubismVector2 {
        this.x = 0.0f
        this.y = 0.0f

        return this
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as CubismVector2

        if (java.lang.Float.compare(that.x, x) != 0) return false
        return java.lang.Float.compare(that.y, y) == 0
    }

    override fun hashCode(): Int {
        var result = (if (x != 0.0f) java.lang.Float.floatToIntBits(x) else 0)
        result = 31 * result + (if (y != 0.0f) java.lang.Float.floatToIntBits(y) else 0)
        return result
    }

    /**
     * X-value
     */
    var x: Float = 0f

    /**
     * Y-value
     */
    var y: Float = 0f

    companion object {
        /**
         * 第1引数と第2引数のベクトルを加算して、計算結果を第3引数のベクトルにコピーする。
         *
         * @param augend 被加数のベクトル
         * @param addend 加数のベクトル
         * @param dest 代入先のベクトル
         * @return 代入先のベクトル
         */
        fun add(augend: CubismVector2, addend: CubismVector2, dest: CubismVector2): CubismVector2 {
            dest.x = augend.x + addend.x
            dest.y = augend.y + addend.y

            return dest
        }

        /**
         * 第1引数と第2引数のベクトルを引き算して、計算結果を第3引数のベクトルにコピーする。
         *
         * @param minuend 被減数
         * @param subtrahend 減数
         * @param dest 代入先のベクトル
         * @return 代入先のベクトル
         */
        fun subtract(
            minuend: CubismVector2,
            subtrahend: CubismVector2,
            dest: CubismVector2
        ): CubismVector2 {
            dest.x = minuend.x - subtrahend.x
            dest.y = minuend.y - subtrahend.y

            return dest
        }

        /**
         * 第1引数と第2引数のベクトルを掛け算して、計算結果を第3引数のベクトルにコピーする。
         *
         * @param multiplicand 被乗数
         * @param multiplier 乗数
         * @param dest 代入先のベクトル
         * @return 代入先のベクトル
         */
        fun multiply(
            multiplicand: CubismVector2,
            multiplier: CubismVector2,
            dest: CubismVector2
        ): CubismVector2 {
            dest.x = multiplicand.x * multiplier.x
            dest.y = multiplicand.y * multiplier.y

            return dest
        }

        /**
         * 第1引数のベクトルと第2引数のスカラーを掛け算して、計算結果を第3引数のベクトルにコピーする。
         *
         * @param multiplicand 被乗数のベクトル
         * @param multiplier 乗数のスカラー
         * @param dest 代入先のベクトル
         * @return 代入先のベクトル
         */
        fun multiply(
            multiplicand: CubismVector2,
            multiplier: Float,
            dest: CubismVector2
        ): CubismVector2 {
            dest.x = multiplicand.x * multiplier
            dest.y = multiplicand.y * multiplier

            return dest
        }

        /**
         * 第1引数と第2引数のベクトルを割り算して、計算結果を第3引数のベクトルにコピーする。
         *
         * @param dividend 被除数
         * @param divisor 除数
         * @param dest 代入先のベクトル
         * @return 代入先のベクトル
         */
        fun divide(
            dividend: CubismVector2,
            divisor: CubismVector2,
            dest: CubismVector2
        ): CubismVector2 {
            dest.x = dividend.x / divisor.x
            dest.y = dividend.y / divisor.y

            return dest
        }

        /**
         * 第1引数のベクトルと第2引数のスカラーを割り算して、計算結果を第3引数のベクトルにコピーする。
         *
         * @param dividend 被除数のベクトル
         * @param divisor 除数のスカラー
         * @param dest 代入先のベクトル
         * @return 代入先のベクトル
         */
        fun divide(dividend: CubismVector2, divisor: Float, dest: CubismVector2): CubismVector2 {
            dest.x = dividend.x / divisor
            dest.y = dividend.y / divisor

            return dest
        }
    }
}
