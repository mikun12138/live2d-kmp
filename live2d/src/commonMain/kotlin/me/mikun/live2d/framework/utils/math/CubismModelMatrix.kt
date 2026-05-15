/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.utils.math

class CubismModelMatrix : CubismMatrix44 {

    private val width: Float

    private val height: Float

    constructor(w: Float, h: Float) : super() {
        width = w
        height = h

        setHeight(2.0f)
    }
    fun setWidth(w: Float) {
        val scaleX = w / width
        scale(scaleX, scaleX)
    }

    fun setHeight(h: Float) {
        val scaleX = h / height
        scale(scaleX, scaleX)
    }

    fun setPosition(x: Float, y: Float) {
        translate(x, y)
    }

    fun setCenterPosition(x: Float, y: Float) {
        centerX(x)
        centerY(y)
    }

    fun top(y: Float) {
        setY(y)
    }

    fun bottom(y: Float) {
        translateY = y - height * scaleY
    }

    fun left(x: Float) {
        setX(x)
    }
    fun right(x: Float) {
        translateX = x - width * scaleX
    }

    fun centerX(x: Float) {
        translateX = x - (width * scaleX / 2.0f)
    }

    fun setX(x: Float) {
        translateX = x
    }

    fun centerY(y: Float) {
        translateY = y - (height * scaleY / 2.0f)
    }

    fun setY(y: Float) {
        translateY = y
    }

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

}
