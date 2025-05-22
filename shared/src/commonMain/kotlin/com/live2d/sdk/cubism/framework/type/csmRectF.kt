/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.type

/**
 * This class defines a rectangle form(a coordinate and a length is float value)
 */
class csmRectF {
    val centerX: Float
        /**
         * Get the x-coordinate at center of this rectangle.
         *
         * @return the x-coord at center of this rect.
         */
        get() = x + 0.5f * width

    val centerY: Float
        /**
         * Get the y-coordinate at center of this rectangle.
         *
         * @return the y-coord at center of this rect
         */
        get() = y + 0.5f * height

    val right: Float
        /**
         * Get the x-coordinate at right end of this rectangle.
         *
         * @return x-coord at right end
         */
        get() = x + width

    val bottom: Float
        /**
         * Get the y-coordinate at bottom of this rectangle.
         *
         * @return y-coord at bottom
         */
        get() = y + height

    /**
     * Set a value to this rectangle.
     */
    fun setRect(r: csmRectF) {
        x = r.x
        y = r.y
        width = r.width
        height = r.height
    }

    /**
     * Scale width and height with the center of this rectangle as axis.
     *
     * @param w the amount of scaling into width
     * @param h the amount of scaling into height
     */
    fun expand(w: Float, h: Float) {
        x -= w
        y -= h
        width += w * 2.0f
        height += h * 2.0f
    }

    /**
     * Constructor
     */
    private constructor()

    /**
     * Constructor with each value.
     *
     * @param x left end x-coord
     * @param y top end y-coord
     * @param w width
     * @param h height
     */
    private constructor(
        x: Float,
        y: Float,
        w: Float,
        h: Float
    ) {
        this.x = x
        this.y = y
        this.width = w
        this.height = h
    }

    private constructor(r: csmRectF) {
        setRect(r)
    }

    /**
     * Get this x-coordinate
     *
     * @return x-coord
     */
    /**
     * Set x-coordinate to this one.
     *
     * @param x x-coord
     */
    /**
     * Left end x-coordinate
     */
    var x: Float = 0f
    /**
     * Get this y-coordinate
     *
     * @return y-coord
     */
    /**
     * Set y-coordinate to this one.
     *
     * @param y y-coord
     */
    /**
     * Top end y-coordinate
     */
    var y: Float = 0f
    /**
     * Get the width of this.
     *
     * @return width
     */
    /**
     * Set the width to this one.
     *
     * @param width width set to this
     */
    /**
     * Width
     */
    var width: Float = 0f
    /**
     * Get the height of this.
     *
     * @return height
     */
    /**
     * Set the height to this one.
     *
     * @param height height set to this
     */
    /**
     * Height
     */
    var height: Float = 0f

    companion object {
        /**
         * Create the new CubismRectangle instance.
         *
         * @return CubismRectangle instance
         */
        fun create(): csmRectF {
            return csmRectF()
        }

        /**
         * Create the new CubismRectangle instance with each parameter.
         *
         * @param x x-coordinate
         * @param y y-coordinate
         * @param w width
         * @param h height
         * @return CubismRectangle instance
         */
        fun create(
            x: Float,
            y: Float,
            w: Float,
            h: Float
        ): csmRectF {
            return csmRectF(x, y, w, h)
        }

        /**
         * Create the new CubismRectangle instance.
         * This method works the same way as a copy constructor.
         *
         * @param r CubismRectangle instance to be copied
         * @return CubismRectangle instance
         */
        fun create(r: csmRectF): csmRectF {
            return csmRectF(r)
        }
    }
}
