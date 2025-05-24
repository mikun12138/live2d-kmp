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
class CubismMotionQueueEntry {
    /**
     * Get the motion.
     *
     * @return motion instance
     */
    fun getMotion(): ACubismMotion? {
        return motion
    }

    /**
     * Set a motion instance.
     *
     * @param motion motion instance
     */
    fun setMotion(motion: ACubismMotion?) {
        this.motion = motion
    }


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
     * @param userTimeSeconds total delta time[s]
     */
    fun startFadeOut(fadeOutSeconds: Float, userTimeSeconds: Float) {
        val newEndTimeSeconds = userTimeSeconds + fadeOutSeconds
        isTriggeredFadeOut = true

        if (this.endTime < 0.0f || newEndTimeSeconds < this.endTime) {
            this.endTime = newEndTimeSeconds
        }
    }

    /**
     * Set end state of the motion.
     *
     * @param isMotionFinished If true, set the motion to the end state.
     */
    fun isFinished(isMotionFinished: Boolean) {
        this.isFinished = isMotionFinished
    }

    /**
     * Set start state of the motion.
     *
     * @param isMotionStarted If true, set the motion to the start state.
     */
    fun isStarted(isMotionStarted: Boolean) {
        this.isStarted = isMotionStarted
    }

    /**
     * Set the status of the motion.(Enable/Disable)
     *
     * @param isMotionAvailable If it is true, the motion is enabled.
     */
    fun isAvailable(isMotionAvailable: Boolean) {
        this.isAvailable = isMotionAvailable
    }

    /**
     * Set the state of the motion.
     *
     * @param timeSeconds current time[s]
     * @param weight weight of motion
     */
    fun setState(timeSeconds: Float, weight: Float) {
        this.stateTime = timeSeconds
        stateWeight = weight
    }

    val cubismMotion: ACubismMotion?
        /**
         * ACubismMotionを継承したクラスのインスタンスを取得する。
         *
         * @return モーションのインスタンス
         */
        get() = motion

    /**
     * motion
     */
    private var motion: ACubismMotion? = null
    /**
     * Get the status of the motion.(Enable/Disable)
     *
     * @return If the motion is valid, return true.
     */
    /**
     * Enable flag
     */
    var isAvailable: Boolean = true
        private set
    /**
     * Whether the motion is finished.
     *
     * @return If the motion is finished, return true.
     */
    /**
     * finished flag
     */
    var isFinished: Boolean = false
        private set
    /**
     * Whether the motion is started.
     *
     * @return If the motion is started, return true.
     */
    /**
     * start flag(0.9.00 or later)
     */
    var isStarted: Boolean = false
        private set
    /**
     * Get the start time of the motion.
     *
     * @return start time of the motion[s]
     */
    /**
     * Set the start time of the motion.
     *
     * @param startTime start time of the motion[s]
     */
    /**
     * Motion playback start time[s]
     */
    var startTime: Float = -1.0f
    /**
     * Get the start time of the fade-in.
     *
     * @return start time of the fade-in[s]
     */
    /**
     * Set the start time of fade-in.
     *
     * @param startTime start time of fade-in[s]
     */
    /**
     * Fade-in start time[s] (When in a loop, only the first time.)
     */
    var fadeInStartTime: Float = 0f
    /**
     * Get the end time of the fade-in.
     *
     * @return end time of the fade-in
     */
    /**
     * Set end time of the motion.
     *
     * @param endTime end time of the motion[s]
     */
    /**
     * Scheduled end time[s]
     */
    var endTime: Float = -1.0f
    /**
     * Get the current time of the motion.
     *
     * @return current time of the motion.
     */
    /**
     * state of time[s]
     */
    var stateTime: Float = 0f
        private set
    /**
     * Get the weight of the motion.
     *
     * @return weight of the motion
     */
    /**
     * state of weight
     */
    var stateWeight: Float = 0f
        private set
    /**
     * Get the time when the last event firing was checked.
     *
     * @return time when the last event firing was checked[s]
     */
    /**
     * Set the time when the last event firing was checked.
     *
     * @param checkTime time when the last event firing was checked[s]
     */
    /**
     * last event check time
     */
    var lastCheckEventTime: Float = 0f
    /**
     * Get fade-out duration.
     *
     * @return fade-out duration
     */
    /**
     * fade-out duration of the motion[s]
     */
    var fadeOutSeconds: Float = 0f
        private set
    /**
     * Get the starting status of the fade-out.
     *
     * @return Whether fade out is started
     */
    /**
     * Whether the motion fade-out is started
     */
    var isTriggeredFadeOut: Boolean = false
        private set
}
