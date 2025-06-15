package me.mikun.live2d.framework.motion.motion

import me.mikun.live2d.framework.id.Live2DIdManager
import com.live2d.sdk.cubism.framework.math.CubismMath.getEasingSine
import me.mikun.live2d.framework.model.Live2DModel
import me.mikun.live2d.framework.motion.ALive2DMotionQueueEntry
import me.mikun.live2d.framework.motion.motion.Live2DMotionInternal.CubismMotionCurveTarget
import me.mikun.live2d.framework.motion.motion.Live2DMotion.Companion.EffectID
import me.mikun.live2d.framework.motion.motion.Live2DMotion.Companion.MotionBehavior
import me.mikun.live2d.framework.motion.motion.Live2DMotion.Companion.OpacityID
import me.mikun.live2d.framework.utils.switchStateTo

class Live2DMotionQueueEntry(
    override val manager: Live2DMotionManager,
    override val motion: Live2DMotion,
) : ALive2DMotionQueueEntry(
    manager,
    motion
) {
    override fun doInit() {
        motion.beganMotionCallback(motion)
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
        model: Live2DModel,
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

            motion.motionData.curves.filter { it.type == CubismMotionCurveTarget.MODEL }
                .forEach { curve ->

                    // Evaluate curve and call handler.
                    value = motion.evaluateCurve(
                        curve, time, isCorrection, duration
                    )

                    when (curve.id) {
                        Live2DIdManager.id(EffectID.EYE_BLINK.value) -> {
                            eyeBlinkValue = value
                            isUpdatedEyeBlink = true
                        }

                        Live2DIdManager.id(EffectID.LIP_SYNC.value) -> {
                            lipSyncValue = value
                            isUpdatedLipSync = true
                        }

                        Live2DIdManager.id(OpacityID.OPACITY.value) -> {
                            // 不透明度の値が存在すれば反映する。
                            // TODO::
//                            model.modelOpacity = value
                        }
                    }
                }

            val tmpFadeIn =
                if (motion.fadeInSeconds <= 0.0f)
                    1.0f
                else
                    getEasingSine((totalSeconds - startTimePoint) / motion.fadeInSeconds)
            val tmpFadeOut =
                if (motion.fadeOutSeconds <= 0.0f || endTimePoint < 0.0f)
                    1.0f
                else
                    getEasingSine((endTimePoint - totalSeconds) / motion.fadeOutSeconds)

            val fadeWeight = calFadeWeight(totalSeconds)
            motion.motionData.curves.filter { it.type == CubismMotionCurveTarget.PARAMETER }
                .forEach { curve ->

                    val sourceValue = model.getParameterValue(curve.id)

                    // Evaluate curve and apply value.
                    value = motion.evaluateCurve(
                        curve, time, isCorrection, duration
                    )

                    if (isUpdatedEyeBlink) {
                        motion.eyeBlinkParameterIds.indexOfFirst { it == curve.id }
                            .takeIf { it >= 0 }
                            ?.let {
                                value *= eyeBlinkValue
                                motion.eyeBlinkOverrideFlags[it] = true
                            }
                    }

                    if (isUpdatedLipSync) {
                        motion.lipSyncParameterIds.indexOfFirst { it == curve.id }
                            .takeIf { it >= 0 }
                            ?.let {
                                value += lipSyncValue
                                motion.lipSyncOverrideFlags[it] = true
                            }
                    }

                    val v: Float
                    if (motion.existFade(curve)) {

                        // If the parameter has a fade-in or fade-out setting, apply it.
                        val fin: Float = if (motion.existFadeIn(curve)) {
                            if (curve.fadeInTime <= 0.0f)
                                1.0f
                            else
                                getEasingSine((totalSeconds - startTimePoint) / curve.fadeInTime)
                        } else {
                            tmpFadeIn
                        }
                        val fout: Float = if (motion.existFadeOut(curve)) {
                            if (curve.fadeOutTime <= 0.0f || endTimePoint < 0.0f)
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
                    model.setParameterValue(curve.id, v)
                }


            if (isUpdatedEyeBlink) {
                motion.eyeBlinkParameterIds.forEachIndexed { index, id ->

                    // Blink does not apply when there is a motion overriding.
                    if (motion.eyeBlinkOverrideFlags[index]) {
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
                    if (motion.lipSyncOverrideFlags[index]) {
                        return@forEachIndexed
                    }

                    val sourceValue: Float = model.getParameterValue(id)
                    val v = sourceValue + (lipSyncValue - sourceValue) * fadeWeight

                    model.setParameterValue(id, v)
                }
            }

            motion.motionData.curves.filter { it.type == CubismMotionCurveTarget.PART_OPACITY }
                .forEach { curve ->

                    // Evaluate curve and apply value.
                    value = motion.evaluateCurve(curve, time, isCorrection, duration)
                    model.setParameterValue(curve.id, value)
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


    // TODO:: level: 114514
//    val firedEvents: List<String>
//        get() = run {
////            if ((event.fireTime > beforeCheckTimeSeconds) && (event.fireTime <= motionTimeSeconds)) {
//            val beforeCheckTimeSeconds: Float = manager.lastTotalSeconds - startTimePoint
//            val motionTimeSeconds: Float = manager.totalSeconds - startTimePoint
//
//            motion.motionData.events
//                .filter { it.fireTime in beforeCheckTimeSeconds..motionTimeSeconds }
//                .map { it.value }
//        }

}