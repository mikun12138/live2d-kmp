/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.utils.math

data class csmRectF(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var width: Float = 0.0f,
    var height: Float = 0.0f,
)

 val csmRectF.centerX: Float
     get() = x + 0.5f * width

val csmRectF.centerY: Float
    get() = y + 0.5f * height

val csmRectF.right: Float
    get() = x + width

val csmRectF.bottom: Float
    get() = y + height

fun csmRectF.expand(w: Float, h: Float): csmRectF {
    x -= w
    y -= h
    width += w * 2.0f
    height += h * 2.0f
    return this
}

