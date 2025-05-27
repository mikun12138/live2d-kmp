/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.math.CubismMath
import com.live2d.sdk.cubism.framework.math.CubismMath.getEasingSine
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.motion.CubismMotion.Companion.modelCurveIdEyeBlink
import com.live2d.sdk.cubism.framework.motion.CubismMotion.Companion.modelCurveIdLipSync
import com.live2d.sdk.cubism.framework.motion.CubismMotion.Companion.modelCurveIdOpacity
import com.live2d.sdk.cubism.framework.motion.CubismMotionInternal.CubismMotionCurveTarget

/**
 * Manager class for each motion being played by CubismMotionQueueManager.
 */
class CubismMotionQueueEntry(
    val motion: ACubismMotion
) {

    fun setFadeOut() {
        isTriggeredFadeOut = true
    }

    fun startFadeOut(fadeOutSeconds: Float, totalSeconds: Float) {
        val newEndTimeSeconds = totalSeconds + fadeOutSeconds
        if (this.endTimePoint !in 0.0f..newEndTimeSeconds) {
            this.endTimePoint = newEndTimeSeconds
        }

        isTriggeredFadeOut = true
    }

    fun calFadeWeight(totalSeconds: Float): Float {
        val fadeIn = if (motion.fadeInSeconds < 0.0f)
            1.0f
        else
            getEasingSine(
                (totalSeconds - startTimePoint) / motion.fadeInSeconds
            )
        val fadeOut = if (motion.fadeOutSeconds < 0.0f || endTimePoint < 0.0f)
            1.0f
        else
            getEasingSine((endTimePoint - totalSeconds) / motion.fadeOutSeconds)

        check(fadeIn * fadeOut in 0.0f..1.0f)

        return fadeIn * fadeOut
    }

    /**
     * Update parameters of the model.
     *
     * @param model target model
     * @param totalSeconds current time[s]
     * @param fadeWeight weight of motion
     * @param motionQueueEntry motion managed by CubismMotionQueueManager
     */
    override fun doUpdateParameters(
        model: CubismModel,
        totalSeconds: Float,
        fadeWeight: Float,
        motionQueueEntry: CubismMotionQueueEntry
    ) {
        if (previousLoopState != loop) {
            // 終了時間を再計算する
            adjustEndTime(motionQueueEntry)
            previousLoopState = loop
        }

        val start_2_nowSeconds: Float = totalSeconds - motionQueueEntry.startTimePoint
        check(start_2_nowSeconds >= 0.0f)

//        val MAX_TARGET_SIZE = 64
//
//        if (eyeBlinkParameterIds.size > MAX_TARGET_SIZE) {
//            val message = "too many eye blink targets: " + eyeBlinkParameterIds.size
//            CubismDebug.cubismLogDebug(message)
//        }
//        if (lipSyncParameterIds.size > MAX_TARGET_SIZE) {
//            val message = "too many lip sync targets: " + lipSyncParameterIds.size
//            CubismDebug.cubismLogDebug(message)
//        }

        run {

            // 'Repeat time as necessary'
            var time = start_2_nowSeconds
            var duration: Float = motionData.duration
            val isCorrection = loop

            if (loop) {
                duration += 1.0f / motionData.fps
                while (time > duration) {
                    time -= duration
                }
                // TODO:: use this
//                time %= duration
            }


            var eyeBlinkValue = 0f
            var lipSyncValue = 0f

            // A bit flag indicating whether the blink and lip-sync motions have been applied.
            var isUpdatedEyeBlink = false
            var isUpdatedLipSync = false

            var value: Float

            // Evaluate model curves
            motionData.curves.filter { it.type == CubismMotionCurveTarget.MODEL }
                .forEach { curve ->

                    // Evaluate curve and call handler.
                    value = evaluateCurve(
                        curve, time, isCorrection, duration
                    )

                    when (curve.id) {
                        modelCurveIdEyeBlink -> {
                            eyeBlinkValue = value
                            isUpdatedEyeBlink = true
                        }

                        modelCurveIdLipSync -> {
                            lipSyncValue = value
                            isUpdatedLipSync = true
                        }

                        modelCurveIdOpacity -> {
                            this.modelOpacityValue = value

                            // 不透明度の値が存在すれば反映する。
                            model.modelOpacity = this.modelOpacityValue
                        }
                    }
                }

            val tmpFadeIn = if (fadeInSeconds <= 0.0f)
                1.0f
            else
                CubismMath.getEasingSine((totalSeconds - motionQueueEntry.startTimePoint) / fadeInSeconds)
            val tmpFadeOut = if (fadeOutSeconds <= 0.0f || motionQueueEntry.endTimePoint < 0.0f)
                1.0f
            else
                CubismMath.getEasingSine((motionQueueEntry.endTimePoint - totalSeconds) / fadeOutSeconds)

            motionData.curves.filter { it.type == CubismMotionCurveTarget.PARAMETER }
                .forEach { curve ->

                    // Find parameter index.
                    val parameterIndex = model.getParameterIndex(curve.id)

                    // Skip curve evaluation if no value.
                    if (parameterIndex == -1) {
                        return@forEach
                    }

                    val sourceValue = model.getParameterValue(parameterIndex)

                    // Evaluate curve and apply value.
                    value = evaluateCurve(
                        curve, time, isCorrection, duration
                    )

                    if (isUpdatedEyeBlink) {
                        eyeBlinkParameterIds.indexOfFirst { it == curve.id }.takeIf { it >= 0 }
                            ?.let {
                                value *= eyeBlinkValue
                                eyeBlinkOverrideFlags.set(it)
                            }
                    }

                    if (isUpdatedLipSync) {
                        lipSyncParameterIds.indexOfFirst { it == curve.id }.takeIf { it >= 0 }
                            ?.let {
                                value += lipSyncValue
                                lipSyncOverrideFlags.set(it)
                            }
                    }

                    val v: Float
                    if (existFade(curve)) {

                        // If the parameter has a fade-in or fade-out setting, apply it.
                        val fin: Float = if (existFadeIn(curve)) {
                            if (curve.fadeInTime == 0.0f)
                                1.0f
                            else
                                CubismMath.getEasingSine((totalSeconds - motionQueueEntry.startTimePoint) / curve.fadeInTime)
                        } else {
                            tmpFadeIn
                        }
                        val fout: Float = if (existFadeOut(curve)) {
                            if (curve.fadeOutTime == 0.0f || motionQueueEntry.endTimePoint < 0.0f)
                                1.0f
                            else
                                CubismMath.getEasingSine((motionQueueEntry.endTimePoint - totalSeconds) / curve.fadeOutTime)
                        } else {
                            tmpFadeOut
                        }

                        val paramWeight: Float =
//                    weight *
                            fin * fout

                        // Apply each fading.
                        v = sourceValue + (value - sourceValue) * paramWeight
                    } else {
                        // Apply each fading.
                        v = sourceValue + (value - sourceValue) * fadeWeight
                    }
                    model.setParameterValue(parameterIndex, v)
                }


            if (isUpdatedEyeBlink) {
                eyeBlinkParameterIds.forEachIndexed { index, id ->

                    // Blink does not apply when there is a motion overriding.
                    if (eyeBlinkOverrideFlags.get(index)) {
                        return@forEachIndexed
                    }

                    val sourceValue: Float = model.getParameterValue(id)
                    val v = sourceValue + (eyeBlinkValue - sourceValue) * fadeWeight

                    model.setParameterValue(id, v)
                }
            }

            if (isUpdatedLipSync) {
                lipSyncParameterIds.forEachIndexed { index, id ->

                    // Lip-sync does not apply when there is a motion overriding.
                    if (lipSyncOverrideFlags.get(index)) {
                        return@forEachIndexed
                    }

                    val sourceValue: Float = model.getParameterValue(id)
                    val v = sourceValue + (lipSyncValue - sourceValue) * fadeWeight

                    model.setParameterValue(id, v)
                }
            }


            motionData.curves.filter { it.type == CubismMotionCurveTarget.PART_OPACITY }
                .forEach { curve ->

                    // Find parameter index.
                    val parameterIndex = model.getParameterIndex(curve.id)

                    // Skip curve evaluation if no value.
                    if (parameterIndex == -1) {
                        return@forEach
                    }

                    // Evaluate curve and apply value.
                    value = evaluateCurve(curve, time, isCorrection, duration)
                    model.setParameterValue(parameterIndex, value)
                }
        }


        if (start_2_nowSeconds >= duration) {
            finishedMotionCallback(this)
            if (loop) {
                updateForNextLoop(motionQueueEntry, totalSeconds, time)
            } else {
                motionQueueEntry.isFinished = true
            }
        }
        lastWeight = fadeWeight
    }


    fun setup(
        totalSeconds: Float
    ) {
        state = State.FadeIn

        // Record the start time of the motion.
        startTimePoint = totalSeconds

        adjustEndTime(motionQueueEntry)
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
