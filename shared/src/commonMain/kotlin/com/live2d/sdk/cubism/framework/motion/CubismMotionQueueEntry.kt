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

     var startTime: Float = -1.0f
//    /**
//     * Fade-in start time[s] (When in a loop, only the first time.)
//     */
//    var fadeInStartTime: Float = -1.0f
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
    /**
     * 再生中の表情モーションのウェイトのリスト
     * 0为开始 fade, >=1为完成fade
     */
    var fadeWeight: Float = 0.0f

    var state: State = State.Init

    enum class State {
        Init,
        FadeIn,
        Playing,
        FadeOut,
        End
        ;

        fun inInit() = this == Init
        fun inActive() = this == FadeIn || this == Playing || this == FadeOut
        fun inEnd() = this == End
    }

}
