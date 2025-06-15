/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.math

/**
 * 4x4 matrix class for setting model coordinates.
 */
class CubismModelMatrix : CubismMatrix44 {
    /**
     * Set the width.
     *
     * @param w width
     */
    fun setWidth(w: Float) {
        val scaleX = w / width
        scale(scaleX, scaleX)
    }

    /**
     * Set the height.
     *
     * @param h height
     */
    fun setHeight(h: Float) {
        val scaleX = h / height
        scale(scaleX, scaleX)
    }

    /**
     * Set the position.
     *
     * @param x X-axis position
     * @param y Y-axis position
     */
    fun setPosition(x: Float, y: Float) {
        translate(x, y)
    }

    /**
     * Set the center position.
     * Be sure to set the width or height before using this method.
     *
     * @param x center position of X-axis
     * @param y center position of Y-axis
     */
    fun setCenterPosition(x: Float, y: Float) {
        centerX(x)
        centerY(y)
    }

    /**
     * Set the position of the upper edge.
     *
     * @param y the position of the upper edge
     */
    fun top(y: Float) {
        setY(y)
    }

    /**
     * Set the position of the bottom edge.
     *
     * @param y the position of the bottom edge
     */
    fun bottom(y: Float) {
        val h: Float = height * scaleY
        translateY(y - h)
    }

    /**
     * Set the position of the left edge.
     *
     * @param x the position of the left edge
     */
    fun left(x: Float) {
        setX(x)
    }

    /**
     * Set the position of the right edge.
     *
     * @param x the position of the right edge
     */
    fun right(x: Float) {
        val w: Float = width * scaleX
        translateX(x - w)
    }

    /**
     * Set the center position of X-axis.
     *
     * @param x center position of X-axis
     */
    fun centerX(x: Float) {
        val w: Float = width * scaleX
        translateX(x - (w / 2.0f))
    }

    /**
     * Set the position of X-axis.
     *
     * @param x position of X-axis
     */
    fun setX(x: Float) {
        translateX(x)
    }

    /**
     * Set the center position of Y-axis.
     *
     * @param y center position of Y-axis
     */
    fun centerY(y: Float) {
        val h: Float = height * scaleY
        translateY(y - (h / 2.0f))
    }

    /**
     * Set the position of Y-axis.
     *
     * @param y position of Y-axis
     */
    fun setY(y: Float) {
        translateY(y)
    }

    /**
     * Set position from layout information.
     *
     * @param layout layout information
     */
    fun setupFromLayout(layout: MutableMap<String?, Float?>) {
        val keyWidth = "width"
        val keyHeight = "height"
        val keyX = "x"
        val keyY = "y"
        val keyCenterX = "center_x"
        val keyCenterY = "center_y"
        val keyTop = "top"
        val keyBottom = "bottom"
        val keyLeft = "left"
        val keyRight = "right"

        for (entry in layout.entries) {
            val key: String = entry.key!!
            if (key == keyWidth) {
                setWidth(entry.value!!)
            } else if (key == keyHeight) {
                setHeight(entry.value!!)
            }
        }

        for (entry in layout.entries) {
            val key: String = entry.key!!
            val value: Float = entry.value!!

            if (key == keyX) {
                setX(value)
            } else if (key == keyY) {
                setY(value)
            } else if (key == keyCenterX) {
                centerX(value)
            } else if (key == keyCenterY) {
                centerY(value)
            } else if (key == keyTop) {
                top(value)
            } else if (key == keyBottom) {
                bottom(value)
            } else if (key == keyLeft) {
                left(value)
            } else if (key == keyRight) {
                right(value)
            }
        }
    }

    /**
     * Constructor
     */
    private constructor(w: Float, h: Float) : super() {
        width = w
        height = h

        setHeight(2.0f)
    }

    /**
     * Copy constructor
     *
     * @param modelMatrix model matrix to be copied
     */
    private constructor(modelMatrix: CubismModelMatrix) : super() {
        System.arraycopy(modelMatrix.tr, 0, this.tr, 0, 16)
        width = modelMatrix.width
        height = modelMatrix.height
    }

    /**
     * width
     */
    private val width: Float

    /**
     * height
     */
    private val height: Float

    companion object {
        /**
         * Create the new CubismModelMatrix instance with the width and height passed as arguments.
         *
         * @param w width
         * @param h height
         * @return CubismModelMatrix instance with the width and height
         *
         * @throws IllegalArgumentException if arguments equals 0 or are less than 0
         */
        fun create(w: Float, h: Float): CubismModelMatrix {
            require(!(w <= 0 || h <= 0)) { "width or height equals 0 or is less than 0." }
            return CubismModelMatrix(w, h)
        }


        /**
         * Create the new CubismModelMatrix instance from the CubismModelMatrix instance.
         * It works the same way as a copy constructor.
         *
         * @param modelMatrix CubismModelMatrix instance to be copied
         * @return Copied CubismModelMatrix instance
         */
        fun create(modelMatrix: CubismModelMatrix): CubismModelMatrix {
            return CubismModelMatrix(modelMatrix)
        }
    }
}
