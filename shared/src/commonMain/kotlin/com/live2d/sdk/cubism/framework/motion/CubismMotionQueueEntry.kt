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

    fun setFadeOut(fadeOutSeconds: Float) {
        this.fadeOutSeconds = fadeOutSeconds
        isTriggeredFadeOut = true
    }

    fun startFadeOut(fadeOutSeconds: Float, totalSeconds: Float) {
        val newEndTimeSeconds = totalSeconds + fadeOutSeconds
        if (this.endTimePoint !in 0.0f..newEndTimeSeconds) {
            this.endTimePoint = newEndTimeSeconds
        }

        isTriggeredFadeOut = true
    }

     var startTimePoint: Float = -1.0f
    // TODO:: 你知道我要todo什么
//    /**
//     * Fade-in start time[s] (When in a loop, only the first time.)
//     */
//    var fadeInStartTime: Float = -1.0f
    var endTimePoint: Float = -1.0f
    var lastTotalSeconds: Float = 0f
    // TODO:: remove it
//    /**
//     * state of time[s]
//     */
//    var stateSeconds: Float = 0f
//    /**
//     * state of weight
//     */
//    var stateWeight: Float = 0f
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
