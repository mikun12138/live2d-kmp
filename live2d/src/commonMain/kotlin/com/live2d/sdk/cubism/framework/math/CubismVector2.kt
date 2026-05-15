/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.math

import kotlin.math.sqrt

class CubismVector2 {
    constructor()

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    constructor(vec: CubismVector2) {
        this.x = vec.x
        this.y = vec.y
    }

    fun normalize() {
        val length = sqrt((x * x) + (y * y))

        x /= length
        y /= length
    }

    fun set(vec: CubismVector2): CubismVector2 {
        this.x = vec.x
        this.y = vec.y

        return this
    }

    fun set(x: Float, y: Float): CubismVector2 {
        this.x = x
        this.y = y

        return this
    }

    fun setZero(): CubismVector2 {
        this.x = 0.0f
        this.y = 0.0f

        return this
    }

    infix fun plus(other: CubismVector2): CubismVector2 {
        return CubismVector2(this.x + other.x, this.y + other.y)
    }

    infix fun minus(other: CubismVector2): CubismVector2 {
        return CubismVector2(this.x - other.x, this.y - other.y)
    }

    infix fun times(n: Float): CubismVector2 {
        return CubismVector2(this.x * n, this.y * n)
    }

    infix fun div(n: Float): CubismVector2 {
        return CubismVector2(this.x / n, this.y / n)
    }

    operator fun plusAssign(vec: CubismVector2) {
        this.x += vec.x
        this.y += vec.y
    }

    operator fun plusAssign(n: Float) {
        this.x += n
        this.y += n
    }

    operator fun minusAssign(vec: CubismVector2) {
        this.x -= vec.x
        this.y -= vec.y
    }

    operator fun minusAssign(n: Float) {
        this.x -= n
        this.y -= n
    }

    operator fun timesAssign(n: Float) {
        this.x *= n
        this.y *= n
    }

    operator fun divAssign(n: Float) {
        this.x /= n
        this.y /= n
    }

    var x: Float = 0f

    var y: Float = 0f
}
