/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.math

/**
 * Class of 4x4 matrices that can be used to reposition the camera.
 */
class CubismViewMatrix : CubismMatrix44() {
    /**
     * Move for the given arguments.
     * The amount of movement is adjusted.
     *
     * @param x amount of X-axis movement
     * @param y amount of Y-axis movement
     */
    fun adjustTranslate(x: Float, y: Float) {
        var x = x
        var y = y
        if (tr[0] * maxLeft + (tr[12] + x) > screenLeft) {
            x = screenLeft - tr[0] * maxLeft - tr[12]
        }

        if (tr[0] * maxRight + (tr[12] + x) < screenRight) {
            x = screenRight - tr[0] * maxRight - tr[12]
        }

        if (tr[5] * maxTop + (tr[13] + y) < screenTop) {
            y = screenTop - tr[5] * maxTop - tr[13]
        }

        if (tr[5] * maxBottom + (tr[13] + y) > screenBottom) {
            y = screenBottom - tr[5] * maxBottom - tr[13]
        }

        val tr = floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            x, y, 0.0f, 1.0f
        )
        multiply(tr, this.tr, this.tr)
    }

    /**
     * Scale with the given arguments.
     * The amount of scaling is adjusted.
     *
     * @param cx center position of X-axis to be scaled
     * @param cy center position of Y-axis to be scaled
     * @param scale scaling rate
     */
    fun adjustScale(cx: Float, cy: Float, scale: Float) {
        var scale = scale
        val maxScale = this.maxScale
        val minScale = this.minScale

        val targetScale: Float = scale * tr[0]

        if (targetScale < minScale) {
            if (tr[0] > 0.0f) {
                scale = minScale / tr[0]
            }
        } else if (targetScale > maxScale) {
            if (tr[0] > 0.0f) {
                scale = maxScale / tr[0]
            }
        }

        val tr1 = floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            cx, cy, 0.0f, 1.0f
        )
        val tr2 = floatArrayOf(
            scale, 0.0f, 0.0f, 0.0f,
            0.0f, scale, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        )
        val tr3 = floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            -cx, -cy, 0.0f, 1.0f
        )

        multiply(tr3, tr, tr)
        multiply(tr2, tr, tr)
        multiply(tr1, tr, tr)
    }

    /**
     * Check to see if the magnification ratio is at its maximum.
     *
     * @return Return true if the scaling factor is at its maximum.
     */
    fun isMaxScale(): Boolean {
        return scaleX >= maxScale
    }

    /**
     * Check to see if the magnification ratio is at its minimum.
     *
     * @return Return true if the scaling factor is at its minimum.
     */
    fun isMinScale(): Boolean {
        return scaleX <= minScale
    }


    /**
     * Set the range on the logical coordinates corresponding to the device.
     *
     * @param left position of X-axis on the left side
     * @param right position of X-axis on the right side
     * @param bottom position of Y-axis on the bottom side
     * @param top position of Y-axis on the top side
     */
    fun setScreenRect(left: Float, right: Float, bottom: Float, top: Float) {
        screenLeft = left
        screenRight = right
        screenBottom = bottom
        screenTop = top
    }

    /**
     * Set the movable range on the logical coordinates corresponding to the device.
     *
     * @param left position of X-axis on the left side
     * @param right position of X-axis on the right side
     * @param bottom position of Y-axis on the bottom side
     * @param top position of Y-axis on the top side
     */
    fun setMaxScreenRect(left: Float, right: Float, bottom: Float, top: Float) {
        maxLeft = left
        maxRight = right
        maxBottom = bottom
        maxTop = top
    }

    /**
     * Get the X-axis position of the left side of the logical coordinate corresponding to the device.
     *
     * @return X-axis position of the left side of the logical coordinate corresponding to the device
     */
    /**
     * range on logical coordinates corresponding to device (left side X-axis position)
     */
    var screenLeft: Float = 0f
        private set
    /**
     * Get the X-axis position of the right side of the logical coordinate corresponding to the device.
     *
     * @return X-axis position of the right side of the logical coordinate corresponding to the device
     */
    /**
     * range on logical coordinates corresponding to device (right side X-axis position)
     */
    var screenRight: Float = 0f
        private set
    /**
     * Get the Y-axis position of the bottom side of the logical coordinate corresponding to the device.
     *
     * @return Y-axis position of the bottom side of the logical coordinate corresponding to the device
     */
    /**
     * range on logical coordinates corresponding to device (top side Y-axis position)
     */
    var screenBottom: Float = 0f
        private set
    /**
     * Get the Y-axis position of the top side of the logical coordinate corresponding to the device.
     *
     * @return Y-axis position of the top side of the logical coordinate corresponding to the device
     */
    /**
     * range on logical coordinates corresponding to device (bottom side Y-axis position)
     */
    var screenTop: Float = 0f
        private set
    /**
     * Get the maximum X-axis position of the left side.
     *
     * @return maximum X-axis position of the left side
     */
    /**
     * Moveable range on logical coordinates (left side X-axis position)
     */
    var maxLeft: Float = 0f
        private set
    /**
     * Get the maximum X-axis position of the right side.
     *
     * @return maximum X-axis position of the right side
     */
    /**
     * Moveable range on logical coordinates (right side X-axis position)
     */
    var maxRight: Float = 0f
        private set
    /**
     * Get the maximum Y-axis position of the bottom side.
     *
     * @return maximum Y-axis position of the bottom side
     */
    /**
     * Moveable range on logical coordinates (top side Y-axis position)
     */
    var maxBottom: Float = 0f
        private set
    /**
     * Get the maximum Y-axis position of the top side.
     *
     * @return maximum Y-axis position of the top side
     */
    /**
     * Moveable range on logical coordinates (bottom side Y-axis position)
     */
    var maxTop: Float = 0f
        private set
    /**
     * Get the maximum scaling.
     *
     * @return maximum scaling
     */
    /**
     * Set the maximum scaling.
     *
     * @param maxScale maximum scaling
     */
    /**
     * maximum value of scaling rate
     */
    var maxScale: Float = 0f
    /**
     * Get the minimum scaling.
     *
     * @return minimum scaling
     */
    /**
     * Set the minimum scaling.
     *
     * @param minScale minimum scaling
     */
    /**
     * minimum value of scaling rate
     */
    var minScale: Float = 0f
}
