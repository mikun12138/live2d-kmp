/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.math.CubismMath.getEasingSine
import com.live2d.sdk.cubism.framework.model.CubismModel
import java.util.Collections

abstract class ACubismMotion {

//    fun updateParameters(
//        model: CubismModel,
//        motionQueueEntry: CubismMotionQueueEntry,
//        userTimeSeconds: Float
//    ) {
//        if (!motionQueueEntry.isFinished) {
//            setupMotionQueueEntry(motionQueueEntry, userTimeSeconds)
//
//            val fadeWeight = updateFadeWeight(motionQueueEntry, userTimeSeconds)
//
//            //---- 全てのパラメータIDをループする ----
//            doUpdateParameters(model, userTimeSeconds, fadeWeight, motionQueueEntry)
//
//            // 後処理
//            // 終了時刻を過ぎたら終了フラグを立てる（CubismMotionQueueManager）
//            if (motionQueueEntry.endTime > 0.0f && motionQueueEntry.endTime < userTimeSeconds) {
//                motionQueueEntry.isFinished = true // 終了
//            }
//        }
//    }

    fun setupMotionQueueEntry(
        motionQueueEntry: CubismMotionQueueEntry,
        totalSeconds: Float
    ) {
        motionQueueEntry.state = CubismMotionQueueEntry.State.FadeIn

        // Record the start time of the motion.
        motionQueueEntry.startTime = totalSeconds

        adjustEndTime(motionQueueEntry)
    }

    fun updateFadeWeight(motionQueueEntry: CubismMotionQueueEntry, totalSeconds: Float): Float {
        val fadeIn = if (this.fadeInSeconds < 0.0f)
            1.0f
        else
            getEasingSine(
                (totalSeconds - motionQueueEntry.startTime) / this.fadeInSeconds
            )
        val fadeOut = if (this.fadeOutSeconds < 0.0f || motionQueueEntry.endTime < 0.0f)
            1.0f
        else
            getEasingSine((motionQueueEntry.endTime - totalSeconds) / this.fadeOutSeconds)

        check(fadeIn * fadeOut in 0.0f..1.0f)

        return fadeIn * fadeOut
    }


    val duration: Float = -1.0f

    val loopDuration: Float = -1.0f

    /**
     * Check for event firing.
     * The input time reference is set to zero at the called motion timing.
     *
     * @param beforeCheckTimeSeconds last event check time [s]
     * @param motionTimeSeconds playback time this time [s]
     * @return list of events that have fired
     */
    open fun getFiredEvent(
        beforeCheckTimeSeconds: Float,
        motionTimeSeconds: Float
    ): MutableList<String?> {
        return Collections.unmodifiableList<String?>(firedEventValues)
    }

    /**
     * Perform parameter updates for the model.
     *
     * @param model target model
     * @param userTimeSeconds total delta time[s]
     * @param weight weight of motion
     * @param motionQueueEntry motion managed by CubismMotionQueueManager
     */
    abstract fun doUpdateParameters(
        model: CubismModel,
        userTimeSeconds: Float,
        weight: Float,
        motionQueueEntry: CubismMotionQueueEntry
    )

    protected fun adjustEndTime(motionQueueEntry: CubismMotionQueueEntry) {
        val endTime = if (duration < 0)
            -1.0f
        else
            motionQueueEntry.startTime + duration

        motionQueueEntry.endTime = endTime
    }

    protected open val modelOpacityValue: Float = 1.0f
    var fadeInSeconds: Float = -1.0f
    var fadeOutSeconds: Float = -1.0f

    /**
     * Enable/Disable loop
     */
    var loop: Boolean = false

    /**
     * flag whether fade-in is enabled at looping. Default value is true.
     */
    var loopFadeIn: Boolean = true

    /**
     * The previous state of `_isLoop`.
     */
    protected var previousLoopState: Boolean = this.loop

    /**
     * List of events that have fired
     */
    protected var firedEventValues: MutableList<String?> = ArrayList()

    var beganMotionCallback: IBeganMotionCallback = IBeganMotionCallback { }
    var finishedMotionCallback: IFinishedMotionCallback = IFinishedMotionCallback { }
}
