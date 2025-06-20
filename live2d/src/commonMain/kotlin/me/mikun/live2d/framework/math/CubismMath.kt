/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.math

import me.mikun.live2d.framework.utils.Live2DLogger
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility class used for numerical calculations, etc.
 */
object CubismMath {
    const val PI: Float = 3.1415926535897932384626433832795f
    const val EPSILON: Float = 0.00001f

    /**
     * Calculate the easing processed sin. Can be used for easing during fade in/out.
     *
     * @param value target value
     * @return eased sin value
     */
    fun getEasingSine(value: Float): Float {
        if (value < 0.0f) {
            return 0.0f
        } else if (value > 1.0f) {
            return 1.0f
        }
        return (0.5f - 0.5f * cos(value * PI))
    }

    /**
     * Convert an angle value to a radian value.
     *
     * @param degrees angle value[degree]
     * @return radian value converted from angle value
     */
    fun degreesToRadian(degrees: Float): Float {
        return (degrees / 180.0f) * PI
    }

    /**
     * Convert a radian value to an angle value
     *
     * @param radian radian value[rad]
     * @return angle value converted from radian value
     */
    fun radianToDegrees(radian: Float): Float {
        return (radian * 180.0f) / PI
    }

    /**
     * Calculate a radian value from two vectors.
     *
     * @param from position vector of a starting point
     * @param to position vector of an end point
     * @return direction vector calculated from radian value
     */
    fun directionToRadian(from: CubismVector2, to: CubismVector2): Float {
        val q1 = atan2(to.y, to.x).toFloat()
        val q2 = atan2(from.y, from.x).toFloat()

        var radian = q1 - q2

        while (radian < -PI) {
            radian += PI * 2.0f
        }

        while (radian > PI) {
            radian -= PI * 2.0f
        }

        return radian
    }

    /**
     * Calculate an angle value from two vectors.
     *
     * @param from position vector of a starting point
     * @param to position vector of an end point
     * @return direction vector calculated from angle value
     */
    fun directionToDegrees(from: CubismVector2, to: CubismVector2): Float {
        val radian = directionToRadian(from, to)
        var degree = radianToDegrees(radian)

        if ((to.x - from.x) > 0.0f) {
            degree = -degree
        }

        return degree
    }

    /**
     * Convert a radian value to a direction vector.
     *
     * @param totalAngle radian value
     * @param result CubismVector2 instance for storing calculation results
     * @return direction vector calculated from radian value
     */
    fun radianToDirection(totalAngle: Float, result: CubismVector2): CubismVector2 {
        result.x = sin(totalAngle)
        result.y = cos(totalAngle)

        return result
    }

    /**
     * Calculate the solution to the quadratic equation.
     *
     * @param a coefficient value of quadratic term
     * @param b coefficient value of the first order term
     * @param c value of constant term
     * @return solution of a quadratic equation
     */
    fun quadraticEquation(a: Float, b: Float, c: Float): Float {
        if (abs(a) < EPSILON) {
            if (abs(b) < EPSILON) {
                return -c
            }
            return -c / b
        }
        return -(b + sqrt(b * b - 4.0f * a * c)) / (2.0f * a)
    }

    /**
     * Calculate the solution of the cubic equation corresponding to the Bezier's t-value by the Cardano's formula.
     * Returns a solution that has a value of 0.0~1.0 when it is a multiple solution.
     *
     * @param a coefficient value of cubic term
     * @param b coefficient value of quadratic term
     * @param c coefficient value of the first order term
     * @param d value of constant term
     * @return solution between 0.0~1.0
     */
    fun cardanoAlgorithmForBezier(a: Float, b: Float, c: Float, d: Float): Float {
        if (abs(a) < EPSILON) {
            return quadraticEquation(b, c, d).coerceIn(0.0f, 1.0f)
        }
        val ba = b / a
        val ca = c / a
        val da = d / a

        val p = (3.0f * ca - ba * ba) / 3.0f
        val p3 = p / 3.0f
        val q = (2.0f * ba * ba * ba - 9.0f * ba * ca + 27.0f * da) / 27.0f
        val q2 = q / 2.0f
        val discriminant = q2 * q2 + p3 * p3 * p3

        val center = 0.5f
        val threshold = center + 0.01f

        if (discriminant < 0.0f) {
            val mp3 = -p / 3.0f
            val mp33 = mp3 * mp3 * mp3
            val r = sqrt(mp33)
            val t = -q / (2.0f * r)
            val cosphi = t.coerceIn(
                -1.0f, 1.0f
            )
            val phi = acos(cosphi.toDouble()).toFloat()
            val crtr = cbrt(r.toDouble()).toFloat()
            val t1 = 2.0f * crtr

            val root1 = t1 * cos(phi / 3.0f) - ba / 3.0f
            if (abs(root1 - center) < threshold) {
                return root1.coerceIn(0.0f, 1.0f)
            }

            val root2 = t1 * cos((phi + 2.0f * PI) / 3.0f) - ba / 3.0f
            if (abs(root2 - center) < threshold) {
                return root2.coerceIn(0.0f, 1.0f)
            }

            val root3 = t1 * cos((phi + 4.0f * PI) / 3.0f) - ba / 3.0f
            return root3.coerceIn(0.0f, 1.0f)
        }

        if (discriminant == 0.0f) {
            val u1: Float
            if (q2 < 0.0f) {
                u1 = cbrt(-q2.toDouble()).toFloat()
            } else {
                u1 = -cbrt(q2.toDouble()).toFloat()
            }

            val root1 = 2.0f * u1 - ba / 3.0f
            if (abs(root1 - center) < threshold) {
                return root1.coerceIn(0.0f, 1.0f)
            }

            val root2 = -u1 - ba / 3.0f
            return root2.coerceIn(0.0f, 1.0f)
        }

        val sd = sqrt(discriminant)
        val u1 = cbrt((sd - q2).toDouble()).toFloat()
        val v1 = cbrt((sd + q2).toDouble()).toFloat()
        val root1 = u1 - v1 - ba / 3.0f
        return root1.coerceIn(0.0f, 1.0f)
    }

    /**
     * 浮動小数点の余りを求める。
     *
     * @param dividend 被除数（割られる値）
     * @param divisor  除数（割る値）
     * @return 余り
     */
    fun modF(dividend: Float, divisor: Float): Float {
        if (dividend.isInfinite()
            || divisor.compareTo(0.0f) == 0 || dividend.isNaN()
            || divisor.isNaN()
        ) {
            Live2DLogger.warning(
                "dividend: $dividend, divisor: $divisor ModF() returns 'NaN'."
            )
            return Float.Companion.NaN
        }

        return dividend % divisor
    }
}
