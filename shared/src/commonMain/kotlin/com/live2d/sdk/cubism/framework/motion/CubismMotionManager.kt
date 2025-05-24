/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.model.CubismModel

/**
 * Class for managing motion playback, used to play ACubismMotion subclasses such as CubismMotion motion.
 * * If another motion do startMotion() during playback, it will smoothly change to the new motion and the old motion will be suspended.
 * * Use multiple CubismMotionManager instances to play multiple motions at the same time, such as when motions for facial expressions and body motions are made separately.
 */
class CubismMotionManager : CubismMotionQueueManager() {
    /**
     * Set a priority and start the motion.
     *
     * @param motion motion instance
     * @param priority priority of motion
     * @return Returns the identification number(OptionalInt) of the motion that has started. Used as an argument for isFinished(), which judges whether an individual motion has been completed. When it cannot be started, it returns an empty OptionalInt.
     */
    fun startMotionPriority(motion: ACubismMotion?, priority: Int): Int {
        if (priority == reservationPriority) {
            reservationPriority = 0 // Cancel the reservation.
        }

        // Set priority of the motion during playback.
        currentPriority = priority

        return startMotion(motion)
    }

    /**
     * Update the motion and reflect the parameter values to the model.
     *
     * @param model target model
     * @param deltaTimeSeconds delta time[s]
     * @return If it is updated, return true.
     */
    fun updateMotion(model: CubismModel?, deltaTimeSeconds: Float): Boolean {
        userTimeSeconds += deltaTimeSeconds

        val isUpdated: Boolean = doUpdateMotion(model, userTimeSeconds)

        if (isFinished()) {
            currentPriority = 0 // 再生中モーションの優先度を解除
        }
        return isUpdated
    }

    /**
     * Reserve motion with a priority.
     *
     *
     * If the given priority is lower than the already existing reserved priority and the priority of the current motion, it is not reserved and "false" is returned.
     *
     *
     * @param priority motion's priority
     * @return If reserving the motion is successful, return true.
     */
    fun reserveMotion(priority: Int): Boolean {
        if (priority <= reservationPriority || priority <= currentPriority) {
            return false
        }
        reservationPriority = priority

        return true
    }

    /**
     * Get the priority of the motion being played now.
     *
     * @return priority of the motion
     */
    /**
     * Priority of the currently playing motion.
     */
    var currentPriority: Int = 0
        private set
    /**
     * Get the priority of the reserved motion.
     *
     * @return priority of the motion
     */
    /**
     * Set the priority to the reserved motion.
     *
     * @param priority motion's priority
     */
    /**
     * Priority of the motion to be played. The value becomes 0 during playback. This is function for loading motion files in a separate thread.
     */
    var reservationPriority: Int = 0
}
