/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.math

/**
 * This class provides face orientation control functionality.
 */
class CubismTargetPoint {
    /**
     * Update face orientation control.
     *
     * @param deltaTimeSeconds delta time[s]
     */
    fun update(deltaTimeSeconds: Float) {
        // Add delta time
        userTimeSeconds += deltaTimeSeconds

        // Set the maximum speed considering the average speed when swinging the head from the center to the left or right.
        // The swing of the face is set from the center (0.0) to the left and right (+-1.0).
        val faceParamMaxV = 40.0f / 10.0f
        // Upper limit of the speed that can be changed per frame.
        val maxV = faceParamMaxV / FRAME_RATE

        if (lastTimeSeconds == 0.0f) {
            lastTimeSeconds = userTimeSeconds
            return
        }

        val deltaTimeWeight = (userTimeSeconds - lastTimeSeconds) * FRAME_RATE
        lastTimeSeconds = userTimeSeconds

        // Calculate the time it takes to reach the maximum speed.
        val timeToMaxSpeed = 0.15f
        val frameToMaxSpeed = timeToMaxSpeed * FRAME_RATE // sec * frame/sec
        val maxA = deltaTimeWeight * maxV / frameToMaxSpeed

        val dx = faceTargetX - this.x
        val dy = faceTargetY - this.y

        // In the case of there is no change.
        if (CubismMath.absF(dx) <= EPSILON && CubismMath.absF(dy) <= EPSILON) {
            return
        }

        // If the speed is greater than the maximum speed, reduce the speed.
        val d: Float = CubismMath.sqrtF((dx * dx) + (dy * dy))

        // Maximum velocity vector in the direction of travel.
        val vx = maxV * dx / d
        val vy = maxV * dy / d

        // Calculate the change (acceleration) from the current speed to the new speed.
        var ax = vx - faceVX
        var ay = vy - faceVY

        val a: Float = CubismMath.sqrtF((ax * ax) + (ay * ay))

        // At acceleration.
        if (a < -maxA || a > maxA) {
            ax *= maxA / a
            ay *= maxA / a
        }

        // Add the acceleration to the original speed to obtain the new speed.
        faceVX += ax
        faceVY += ay


        // Processing for smooth deceleration when approaching the desired direction
        // Calculate the maximum speed currently available based on the relationship between distance and speed at which an object can stop at a set acceleration, and slow down if the speed is greater than that.
        // Humans are inherently more flexible because they can adjust force (acceleration) with muscle power, but this is a simple process.
        run {
            // Relational expression between acceleration, velocity, and distance.
            //            2  6           2               3
            //      sqrt(a  t  + 16 a h t  - 8 a h) - a t
            // v = --------------------------------------
            //                    2
            //                 4 t  - 2
            // (t=1)
            // Time t is calculated in advance as 1/60 (frame rate, no units) for acceleration and velocity. Therefore, it can be erased as t = 1 (unverified)
            val maxV2: Float =
                0.5f * (CubismMath.sqrtF((maxA * maxA) + 16.0f * maxA * d - 8.0f * maxA * d) - maxA)
            val curV: Float = CubismMath.sqrtF((faceVX * faceVX) + (faceVY * faceVY))

            // Decelerate to maximum speed when current speed > maximum speed.
            if (curV > maxV2) {
                faceVX *= maxV2 / curV
                faceVY *= maxV2 / curV
            }
        }
        this.x += faceVX
        this.y += faceVY
    }


    /**
     * Set a target value for face orientation.
     *
     * @param x X-axis face orientation value (-1.0 - 1.0)
     * @param y Y-axis face orientation value (-1.0 - 1.0)
     */
    fun set(x: Float, y: Float) {
        faceTargetX = x
        faceTargetY = y
    }

    /**
     * X target value for face orientation (getting closer to this value)
     */
    private var faceTargetX = 0f

    /**
     * Y target value for face orientation (getting closer to this value)
     */
    private var faceTargetY = 0f
    /**
     * Get the face orientation value on the X-axis.
     *
     * @return X-axis face orientation value (-1.0 - 1.0)
     */
    /**
     * face orientation X (-1.0 - 1.0)
     */
    var x: Float = 0f
        private set
    /**
     * Get the face orientation value on the Y-axis.
     *
     * @return Y-axis face orientation value (-1.0 - 1.0)
     */
    /**
     * face orientation Y (-1.0 - 1.0)
     */
    var y: Float = 0f
        private set

    /**
     * speed of change in face orientation X
     */
    private var faceVX = 0f

    /**
     * speed of change in face orientation Y
     */
    private var faceVY = 0f

    /**
     * last executed time[s]
     */
    private var lastTimeSeconds = 0f

    /**
     * total elapsed time[s]
     */
    private var userTimeSeconds = 0f

    companion object {
        /**
         * Framerate per seconds[s]
         */
        private const val FRAME_RATE = 30

        /**
         * Epsilon value
         */
        private const val EPSILON = 0.01f
    }
}
