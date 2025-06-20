/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.math

import kotlin.math.pow
import kotlin.math.sqrt

class CubismVector2 {

    constructor(x: Float = 0.0f, y: Float = 0.0f) {
        set(x, y)
    }

    constructor(vec: CubismVector2) {
        set(vec)
    }

    fun add(vec: CubismVector2): CubismVector2 {
        this.x += vec.x
        this.y += vec.y

        return this
    }

    fun subtract(vec: CubismVector2): CubismVector2 {
        this.x -= vec.x
        this.y -= vec.y

        return this
    }

    fun multiply(vec: CubismVector2): CubismVector2 {
        x *= vec.x
        y *= vec.y

        return this
    }

    fun multiply(scalar: Float): CubismVector2 {
        x *= scalar
        y *= scalar

        return this
    }

    fun divide(vec: CubismVector2): CubismVector2 {
        x /= vec.x
        y /= vec.y

        return this
    }

    fun divide(scalar: Float): CubismVector2 {
        this.x /= scalar
        this.y /= scalar

        return this
    }

    fun normalize() {
        val length = (((x * x) + (y * y)).toDouble().pow(0.5)).toFloat()

        x /= length
        y /= length
    }

    val length: Float
        get() = sqrt(x * x + y * y)

    fun getDistanceWith(vec: CubismVector2): Float {
        return sqrt(((this.x - vec.x) * (this.x - vec.x)) + ((this.y - vec.y) * (this.y - vec.y)))
    }

    fun dot(vec: CubismVector2): Float {
        return (this.x * vec.x) + (this.y * vec.y)
    }

    private fun set(vec: CubismVector2): CubismVector2 {
        this.x = vec.x
        this.y = vec.y

        return this
    }

    private fun set(x: Float, y: Float): CubismVector2 {
        this.x = x
        this.y = y

        return this
    }

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

    var x: Float = 0f

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
