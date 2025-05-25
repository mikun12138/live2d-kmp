/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

/**
 * Manager class for each motion being played by CubismMotionQueueManager.
 */
class CubismMotionQueueEntry(
     val motion: ACubismMotion
) {

    /**
     * Set the fade-out to the start state.
     *
     * @param fadeOutSeconds time it takes to fade out[s]
     */
    fun setFadeOut(fadeOutSeconds: Float) {
        this.fadeOutSeconds = fadeOutSeconds
        isTriggeredFadeOut = true
    }

    /**
     * Start fade-out.
     *
     * @param fadeOutSeconds time it takes to fade out[s]
     * @param totalSeconds total delta time[s]
     */
    fun startFadeOut(fadeOutSeconds: Float, totalSeconds: Float) {
        val newEndTimeSeconds = totalSeconds + fadeOutSeconds
        if (this.endTime !in 0.0f..newEndTimeSeconds) {
            this.endTime = newEndTimeSeconds
        }

        isTriggeredFadeOut = true
    }

    /**
     * Enable flag
     */
    var isAvailable: Boolean = true
    /**
     * finished flag
     */
    var isFinished: Boolean = false
    /**
     * start flag(0.9.00 or later)
     */
    var isStarted: Boolean = false
    /**
     * Motion playback start time[s]
     */
    var startTime: Float = -1.0f
    /**
     * Fade-in start time[s] (When in a loop, only the first time.)
     */
    var fadeInStartTime: Float = 0f
    /**
     * Scheduled end time[s]
     */
    var endTime: Float = -1.0f
    /**
     * state of time[s]
     */
    var stateSeconds: Float = 0f
    /**
     * state of weight
     */
    var stateWeight: Float = 0f
    /**
     * last event check time
     */
    var lastCheckEventTime: Float = 0f
    /**
     * fade-out duration of the motion[s]
     */
    var fadeOutSeconds: Float = 0f
        private set
    /**
     * Whether the motion fade-out is started
     */
    var isTriggeredFadeOut: Boolean = false
        private set
}
