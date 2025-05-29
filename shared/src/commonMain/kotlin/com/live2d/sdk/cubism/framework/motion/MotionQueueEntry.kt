/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.math.CubismMath.getEasingSine
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotion.Companion.DEFAULT_ADDITIVE_VALUE
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotion.Companion.DEFAULT_MULTIPLY_VALUE
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotion.ExpressionBlendType
import com.live2d.sdk.cubism.framework.motion.CubismMotion.Companion.MotionBehavior
import com.live2d.sdk.cubism.framework.motion.CubismMotion.Companion.modelCurveIdEyeBlink
import com.live2d.sdk.cubism.framework.motion.CubismMotion.Companion.modelCurveIdLipSync
import com.live2d.sdk.cubism.framework.motion.CubismMotion.Companion.modelCurveIdOpacity
import com.live2d.sdk.cubism.framework.motion.CubismMotionInternal.CubismMotionCurveTarget
import com.live2d.sdk.cubism.util.IState
import com.live2d.sdk.cubism.util.Stateful
import com.live2d.sdk.cubism.util.switchStateTo

/**
 * Manager class for each motion being played by CubismMotionQueueManager.
 */

// TODO:: level 0, make children for expression and motion
class MotionQueueEntry(
    val manager: AMotionManager,
    val motion: CubismMotion,
) : Stateful<MotionQueueEntry, MotionQueueEntry.State> {

    fun setFadeOut() {
        this switchStateTo State.FadeOut
//        isTriggeredFadeOut = true
    }

    fun startFadeOut(fadeOutSeconds: Float, totalSeconds: Float) {
        val newEndTimeSeconds = totalSeconds + fadeOutSeconds
        if (this.endTimePoint !in 0.0f..newEndTimeSeconds) {
            this.endTimePoint = newEndTimeSeconds
        }

        isTriggeredFadeOut = true
    }


    fun init(
        totalSeconds: Float,
    ) {
        this switchStateTo State.FadeIn

        // Record the start time of the motion.
        startTimePoint = totalSeconds

        adjustEndTime()
    }

    private fun adjustEndTime() {
        val endTime = if (motion.loop || motion.motionData.duration < 0)
            -1.0f
        else
            startTimePoint + motion.motionData.duration

        endTimePoint = endTime
    }


    fun doUpdateParameters(
        model: CubismModel,
        totalSeconds: Float,
    ) {
//        if (previousLoopState != loop) {
//            // 終了時間を再計算する
//            adjustEndTime()
//            previousLoopState = loop
//        }

        val start_2_nowSeconds: Float = totalSeconds - startTimePoint
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
            var duration: Float = motion.motionData.duration
            val isCorrection = motion.loop

            if (motion.loop) {
                duration += 1.0f / motion.motionData.fps
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
            motion.motionData.curves.filter { it.type == CubismMotionCurveTarget.MODEL }
                .forEach { curve ->

                    // Evaluate curve and call handler.
                    value = motion.evaluateCurve(
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
                            // 不透明度の値が存在すれば反映する。
                            model.modelOpacity = value
                        }
                    }
                }

            val tmpFadeIn = if (motion.fadeInSeconds <= 0.0f)
                1.0f
            else
                getEasingSine((totalSeconds - startTimePoint) / motion.fadeInSeconds)
            val tmpFadeOut = if (motion.fadeOutSeconds <= 0.0f || endTimePoint < 0.0f)
                1.0f
            else
                getEasingSine((endTimePoint - totalSeconds) / motion.fadeOutSeconds)

            val fadeWeight = calFadeWeight(totalSeconds)
            motion.motionData.curves.filter { it.type == CubismMotionCurveTarget.PARAMETER }
                .forEach { curve ->

                    // Find parameter index.
                    val parameterIndex = model.getParameterIndex(curve.id)

                    // Skip curve evaluation if no value.
                    if (parameterIndex == -1) {
                        return@forEach
                    }

                    val sourceValue = model.getParameterValue(parameterIndex)

                    // Evaluate curve and apply value.
                    value = motion.evaluateCurve(
                        curve, time, isCorrection, duration
                    )

                    if (isUpdatedEyeBlink) {
                        motion.eyeBlinkParameterIds.indexOfFirst { it == curve.id }
                            .takeIf { it >= 0 }
                            ?.let {
                                value *= eyeBlinkValue
                                motion.eyeBlinkOverrideFlags.set(it)
                            }
                    }

                    if (isUpdatedLipSync) {
                        motion.lipSyncParameterIds.indexOfFirst { it == curve.id }
                            .takeIf { it >= 0 }
                            ?.let {
                                value += lipSyncValue
                                motion.lipSyncOverrideFlags.set(it)
                            }
                    }

                    val v: Float
                    if (motion.existFade(curve)) {

                        // If the parameter has a fade-in or fade-out setting, apply it.
                        val fin: Float = if (motion.existFadeIn(curve)) {
                            if (curve.fadeInTime == 0.0f)
                                1.0f
                            else
                                getEasingSine((totalSeconds - startTimePoint) / curve.fadeInTime)
                        } else {
                            tmpFadeIn
                        }
                        val fout: Float = if (motion.existFadeOut(curve)) {
                            if (curve.fadeOutTime == 0.0f || endTimePoint < 0.0f)
                                1.0f
                            else
                                getEasingSine((endTimePoint - totalSeconds) / curve.fadeOutTime)
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
                motion.eyeBlinkParameterIds.forEachIndexed { index, id ->

                    // Blink does not apply when there is a motion overriding.
                    if (motion.eyeBlinkOverrideFlags.get(index)) {
                        return@forEachIndexed
                    }

                    val sourceValue: Float = model.getParameterValue(id)
                    val v = sourceValue + (eyeBlinkValue - sourceValue) * fadeWeight

                    model.setParameterValue(id, v)
                }
            }

            if (isUpdatedLipSync) {
                motion.lipSyncParameterIds.forEachIndexed { index, id ->

                    // Lip-sync does not apply when there is a motion overriding.
                    if (motion.lipSyncOverrideFlags.get(index)) {
                        return@forEachIndexed
                    }

                    val sourceValue: Float = model.getParameterValue(id)
                    val v = sourceValue + (lipSyncValue - sourceValue) * fadeWeight

                    model.setParameterValue(id, v)
                }
            }

            motion.motionData.curves.filter { it.type == CubismMotionCurveTarget.PART_OPACITY }
                .forEach { curve ->

                    // Find parameter index.
                    val parameterIndex = model.getParameterIndex(curve.id)

                    // Skip curve evaluation if no value.
                    if (parameterIndex == -1) {
                        return@forEach
                    }

                    // Evaluate curve and apply value.
                    value = motion.evaluateCurve(curve, time, isCorrection, duration)
                    model.setParameterValue(parameterIndex, value)
                }

            if (start_2_nowSeconds >= duration) {
                motion.finishedMotionCallback(motion)
                if (motion.loop) {
                    updateForNextLoop(totalSeconds, time)
                } else {
                    this switchStateTo State.End
                }
            }
        }
    }

    private fun calFadeWeight(totalSeconds: Float): Float {
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

    private fun updateForNextLoop(
        totalSeconds: Float,
        time: Float,
    ) {
        when (MotionBehavior.MOTION_BEHAVIOR_V2) {
            /*
            MotionBehavior.MOTION_BEHAVIOR_V1 -> {
                // 旧ループ処理
                motionQueueEntry.setStartTime(userTimeSeconds) //最初の状態へ
                if (isLoopFadeIn) {
                    //ループ中でループ用フェードインが有効のときは、フェードイン設定し直し
                    motionQueueEntry.setFadeInStartTime(userTimeSeconds)
                }
            }
            */

            MotionBehavior.MOTION_BEHAVIOR_V2 -> {
                startTimePoint = totalSeconds - time //最初の状態へ
                if (loopFadeIn) {
                    //ループ中でループ用フェードインが有効のときは、フェードイン設定し直し
                    // TODO:: 你知道我要todo什么
//                    motionQueueEntry.setFadeInStartTime(totalSeconds - time)
                }
            }
        }
    }

    // TODO:: move below to another class

    fun updateParameter(
        model: CubismModel,
        parameterValueList: List<CubismExpressionMotionManager.ExpressionParameterValue>,
        isFirstExpression: Boolean,
        totalSeconds: Float,
    ) {
        val fadeWeight = calFadeWeight(totalSeconds)
        parameterValueList.forEach { parameterValue ->

            parameterValue.overwriteValue = model.getParameterValue(parameterValue.parameterId)

            (motion as CubismExpressionMotion).parameters.find { it.parameterId == parameterValue.parameterId }
                ?.let {

                    // 値を計算
                    val newAdditiveValue: Float
                    val newMultiplyValue: Float
                    val newOverwriteValue: Float

                    when (it.blendType) {
                        ExpressionBlendType.ADD -> {
                            newAdditiveValue = it.value
                            newMultiplyValue = DEFAULT_MULTIPLY_VALUE
                            newOverwriteValue = parameterValue.overwriteValue
                        }

                        ExpressionBlendType.MULTIPLY -> {
                            newAdditiveValue = DEFAULT_ADDITIVE_VALUE
                            newMultiplyValue = it.value
                            newOverwriteValue = parameterValue.overwriteValue
                        }

                        ExpressionBlendType.OVERWRITE -> {
                            newAdditiveValue = DEFAULT_ADDITIVE_VALUE
                            newMultiplyValue = DEFAULT_MULTIPLY_VALUE
                            newOverwriteValue = it.value
                        }
                    }

                    if (isFirstExpression) {
                        parameterValue.additiveValue = newAdditiveValue
                        parameterValue.multiplyValue = newMultiplyValue
                        parameterValue.overwriteValue = newOverwriteValue
                    } else {
                        parameterValue.additiveValue =
                            (parameterValue.additiveValue * (1.0f - fadeWeight)) + newAdditiveValue * fadeWeight
                        parameterValue.multiplyValue =
                            (parameterValue.multiplyValue * (1.0f - fadeWeight)) + newMultiplyValue * fadeWeight
                        parameterValue.overwriteValue =
                            (parameterValue.overwriteValue * (1.0f - fadeWeight)) + newOverwriteValue * fadeWeight
                    }
                } ?: run {
                // 再生中のExpressionが参照していないパラメータは初期値を適用
                if (isFirstExpression) {
                    parameterValue.additiveValue = DEFAULT_ADDITIVE_VALUE
                    parameterValue.multiplyValue = DEFAULT_MULTIPLY_VALUE
                } else {
                    parameterValue.additiveValue = calculateValue(
                        parameterValue.additiveValue,
                        DEFAULT_ADDITIVE_VALUE,
                        fadeWeight
                    )
                    parameterValue.multiplyValue = calculateValue(
                        parameterValue.multiplyValue,
                        DEFAULT_MULTIPLY_VALUE,
                        fadeWeight
                    )
                    parameterValue.overwriteValue = calculateValue(
                        parameterValue.overwriteValue,
                        parameterValue.overwriteValue,
                        fadeWeight
                    )
                }
            }
        }
    }

    private fun calculateValue(source: Float, destination: Float, fadeWeight: Float): Float {
        return (source * (1.0f - fadeWeight)) + (destination * fadeWeight)
    }


    val firedEvents: List<String?>
        get() = run {
//            if ((event.fireTime > beforeCheckTimeSeconds) && (event.fireTime <= motionTimeSeconds)) {
            val beforeCheckTimeSeconds: Float = lastTotalSeconds - startTimePoint
            val motionTimeSeconds: Float = totalSeconds - startTimePoint

            motion.motionData.events
                .filter { it.fireTime in beforeCheckTimeSeconds..motionTimeSeconds }
                .map { it.value }
        }

    var startTimePoint: Float = -1.0f

    // TODO:: 你知道我要todo什么
//    /**
//     * Fade-in start time[s] (When in a loop, only the first time.)
//     */
//    var fadeInStartTime: Float = -1.0f
    var endTimePoint: Float = -1.0f

    /**
     * flag whether fade-in is enabled at looping. Default value is true.
     */
    var loopFadeIn: Boolean = true

    //    /**
//     * 再生中の表情モーションのウェイトのリスト
//     * 0为开始 fade, >=1为完成fade
//     */
//    var fadeWeight: Float = 0.0f
//


    enum class State(
        override val onEnter: (MotionQueueEntry, State) -> Unit = { _, _ -> },
        override val onExit: (MotionQueueEntry, State) -> Unit = { _, _ -> },
    ) : IState<MotionQueueEntry, State> {
        Init,
        FadeIn,
        Playing,
        FadeOut(),
        End
        ;

        fun inInit() = this == Init
        fun inActive() = this == FadeIn || this == Playing || this == FadeOut
        fun inEnd() = this == End


    }


}
