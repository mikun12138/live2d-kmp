/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.type

/**
 * This class defines a rectangle form(a coordinate and a length is float value)
 */
data class csmRectF(
    /**
     * Left end x-coordinate
     */
    var x: Float = 0.0f,
    /**
     * Top end y-coordinate
     */
    var y: Float = 0.0f,
    /**
     * Width
     */
    var width: Float = 0.0f,
    /**
     * Height
     */
    var height: Float = 0.0f,
)

 val csmRectF.centerX: Float
     /**
      * Get the x-coordinate at center of this rectangle.
      *
      * @return the x-coord at center of this rect.
      */
     get() = x + 0.5f * width

val csmRectF.centerY: Float
    /**
     * Get the y-coordinate at center of this rectangle.
     *
     * @return the y-coord at center of this rect
     */
    get() = y + 0.5f * height

val csmRectF.right: Float
    /**
     * Get the x-coordinate at right end of this rectangle.
     *
     * @return x-coord at right end
     */
    get() = x + width

val csmRectF.bottom: Float
    /**
     * Get the y-coordinate at bottom of this rectangle.
     *
     * @return y-coord at bottom
     */
    get() = y + height

/**
 * Scale width and height with the center of this rectangle as axis.
 *
 * @param w the amount of scaling into width
 * @param h the amount of scaling into height
 */
fun csmRectF.expand(w: Float, h: Float): csmRectF {
    x -= w
    y -= h
    width += w * 2.0f
    height += h * 2.0f
    return this
}

