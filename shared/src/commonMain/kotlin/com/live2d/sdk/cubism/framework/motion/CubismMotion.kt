/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.CubismFramework.idManager
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.motion.CubismMotionInternal.*
import com.live2d.sdk.cubism.framework.utils.CubismDebug
import com.live2d.sdk.cubism.framework.utils.json.MotionJson
import kotlinx.serialization.json.Json
import java.util.BitSet
import java.util.Collections
import kotlin.Array
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Deprecated
import kotlin.Float
import kotlin.Int
import kotlin.String
import kotlin.assert
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.indices
import kotlin.collections.minus
import kotlin.compareTo
import kotlin.div
import kotlin.run
import kotlin.sequences.minus
import kotlin.text.equals

/**
 * Motion class.
 */
class CubismMotion : ACubismMotion() {
    /**
     * Enumerator for version control of Motion Behavior.
     * For details, see the SDK Manual.
     */
    enum class MotionBehavior {
        MOTION_BEHAVIOR_V1,
        MOTION_BEHAVIOR_V2,
    }

    /**
     * Set loop information.
     *
     * @param loop loop information
     *
     */
    @Deprecated(
        """Not recommended due to the relocation of isLoop to the base class.
                  Use ACubismMotion.setLoop(boolean loop) instead."""
    )
    fun isLooped(loop: Boolean) {
        CubismDebug.cubismLogWarning("isLoop(boolean loop) is a deprecated function. Please use setLoop(boolean loop).")
        super.setLoop(loop)
    }

    @get:Deprecated(
        """Not recommended due to the relocation of isLoop to the base class.
                  Use ACubismMotion.getLoop() instead."""
    )
    val isLooped: Boolean
        /**
         * Whether the motion loops.
         *
         * @return If it loops, return true.
         *
         */
        get() {
            CubismDebug.cubismLogWarning("isLoop() is a deprecated function. Please use getLoop().")
            return super.getLoop()
        }

    /**
     * Set the fade-in information at looping.
     *
     * @param loopFadeIn fade-in information at looping
     *
     */
    @Deprecated(
        """Not recommended due to the relocation of isLoopFadeIn to the base class.
                  Use ACubismMotion.setLoopFadeIn(boolean loopFadeIn) instead."""
    )
    fun isLoopFadeIn(loopFadeIn: Boolean) {
        CubismDebug.cubismLogWarning("isLoopFadeIn(boolean loopFadeIn) is a deprecated function. Please use setLoopFadeIn(boolean loopFadeIn)")
        super.setLoopFadeIn(loopFadeIn)
    }

    @get:Deprecated(
        """Not recommended due to the relocation of isLoopFadeIn to the base class.
                  Use ACubismMotion.getLoopFadeIn() instead."""
    )
    val isLoopFadeIn: Boolean
        /**
         * Whether the motion fade in at looping.
         *
         * @return If it fades in, return true.
         *
         */
        get() {
            CubismDebug.cubismLogWarning("isLoopFadeIn() is a deprecated function. Please use getLoopFadeIn().")
            return super.getLoopFadeIn()
        }

    /**
     * Set the fade-in duration for parameters.
     *
     * @param parameterId parameter ID
     * @param value fade-in duration[s]
     */
    fun setParameterFadeInTime(parameterId: CubismId, value: Float) {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                curve.fadeInTime = value
                return
            }
        }
    }

    /**
     * Get the fade-in duration for parameters.
     *
     * @param parameterId parameter ID
     * @return fade-in duration[s]
     */
    fun getParameterFadeInTime(parameterId: CubismId): Float {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                return curve.fadeInTime
            }
        }
        return -1f
    }

    /**
     * Set the fade-out duration for parameters.
     *
     * @param parameterId parameter ID
     * @param value fade-out duration[s]
     */
    fun setParameterFadeOutTime(parameterId: CubismId, value: Float) {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                curve.fadeOutTime = value
                return
            }
        }
    }

    /**
     * Get the fade-out duration for parameters.
     *
     * @param parameterId parameter ID
     * @return fade-out duration[s]
     */
    fun getParameterFadeOutTime(parameterId: CubismId): Float {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                return curve.fadeOutTime
            }
        }
        return -1f
    }

    /**
     * Set the parameter ID list to which automatic effects are applied.
     *
     * @param eyeBlinkParameterIds parameter ID list to which automatic eye blinking is applied
     * @param lipSyncParameterIds parameter ID list to which automatic lip-syncing is applied
     */
    fun setEffectIds(
        eyeBlinkParameterIds: MutableList<CubismId?>,
        lipSyncParameterIds: MutableList<CubismId?>
    ) {
        this.eyeBlinkParameterIds.clear()
        this.eyeBlinkParameterIds.addAll(eyeBlinkParameterIds)

        this.lipSyncParameterIds.clear()
        this.lipSyncParameterIds.addAll(lipSyncParameterIds)
    }

    val duration: Float
        get() = if (isLoop)
            -1.0f
        else
            this.loopDuration

    public override fun getFiredEvent(
        beforeCheckTimeSeconds: Float,
        motionTimeSeconds: Float
    ): MutableList<String?> {
        firedEventValues.clear()

        for (i in 0..<motionData.events.size()) {
            val event: CubismMotionEvent = motionData.events.get(i)

            if ((event.fireTime > beforeCheckTimeSeconds) && (event.fireTime <= motionTimeSeconds)) {
                firedEventValues.add(event.value)
            }
        }
        return Collections.unmodifiableList<String?>(firedEventValues)
    }

    val isExistModelOpacity: Boolean
        get() {
            for (i in 0..<motionData.curves.size()) {
                val curve: CubismMotionCurve = motionData.curves.get(i)

                if (curve.type !== CubismMotionCurveTarget.MODEL) {
                    continue
                }
                if (curve.id.getString()
                        .equals(ID_NAME_OPACITY)
                ) {
                    return true
                }
            }
            return false
        }

    val modelOpacityIndex: Int
        get() {
            if (this.isExistModelOpacity) {
                for (i in 0..<motionData.curves.size()) {
                    val curve: CubismMotionCurve = motionData.curves.get(i)

                    if (curve.type !== CubismMotionCurveTarget.MODEL) {
                        continue
                    }
                    if (curve.id.getString()
                            .equals(ID_NAME_OPACITY)
                    ) {
                        return i
                    }
                }
            }
            return -1
        }

    public override fun getModelOpacityId(index: Int): CubismId? {
        if (index == -1) {
            return null
        }

        val curve: CubismMotionCurve = motionData.curves.get(index)

        if (curve.type === CubismMotionCurveTarget.MODEL) {
            if (curve.id.getString().equals(ID_NAME_OPACITY)) {
                return CubismFramework.idManager.getId(curve.id)
            }
        }

        return null
    }

    /**
     * Update parameters of the model.
     *
     * @param model target model
     * @param userTimeSeconds current time[s]
     * @param fadeWeight weight of motion
     * @param motionQueueEntry motion managed by CubismMotionQueueManager
     */
    protected override fun doUpdateParameters(
        model: CubismModel,
        userTimeSeconds: Float,
        fadeWeight: Float,
        motionQueueEntry: CubismMotionQueueEntry
    ) {
        if (modelCurveIdEyeBlink == null) {
            modelCurveIdEyeBlink = CubismFramework.idManager.getId(EffectName.EYE_BLINK.name)
        }

        if (modelCurveIdLipSync == null) {
            modelCurveIdLipSync = CubismFramework.idManager.getId(EffectName.LIP_SYNC.name)
        }

        if (modelCurveIdOpacity == null) {
            modelCurveIdOpacity = CubismFramework.idManager.getId(ID_NAME_OPACITY)
        }

        if (motionBehavior == MotionBehavior.MOTION_BEHAVIOR_V2) {
            if (previousLoopState !== isLoop) {
                // 終了時間を再計算する
                adjustEndTime(motionQueueEntry)
                previousLoopState = isLoop
            }
        }

        var timeOffsetSeconds: Float = userTimeSeconds - motionQueueEntry.getStartTime()


        // Error avoidance.
        if (timeOffsetSeconds < 0.0f) {
            timeOffsetSeconds = 0.0f
        }

        val MAX_TARGET_SIZE = 64

        if (eyeBlinkParameterIds.size > MAX_TARGET_SIZE) {
            val message = "too many eye blink targets: " + eyeBlinkParameterIds.size
            CubismDebug.cubismLogDebug(message)
        }
        if (lipSyncParameterIds.size > MAX_TARGET_SIZE) {
            val message = "too many lip sync targets: " + lipSyncParameterIds.size
            CubismDebug.cubismLogDebug(message)
        }

        // 'Repeat time as necessary'
        var time = timeOffsetSeconds
        var duration: Float = motionData.duration
        val isCorrection = motionBehavior == MotionBehavior.MOTION_BEHAVIOR_V2 && isLoop

        if (isLoop) {
            if (motionBehavior == MotionBehavior.MOTION_BEHAVIOR_V2) {
                duration += 1.0f / motionData.fps
            }
            while (time > duration) {
                time -= duration
            }
        }

        val curves: MutableList<CubismMotionCurve> = motionData.curves

        var eyeBlinkValue = 0f
        var lipSyncValue = 0f

        // A bit flag indicating whether the blink and lip-sync motions have been applied.
        var isUpdatedEyeBlink = false
        var isUpdatedLipSync = false

        var value: Float

        // Evaluate model curves
        for (i in curves.indices) {
            val curve: CubismMotionCurve = curves.get(i)

            if (curve.type !== CubismMotionCurveTarget.MODEL) {
                continue
            }

            // Evaluate curve and call handler.
            value = evaluateCurve(motionData, i, time, isCorrection, duration)

            if (curve.id.equals(modelCurveIdEyeBlink)) {
                eyeBlinkValue = value
                isUpdatedEyeBlink = true
            } else if (curve.id.equals(modelCurveIdLipSync)) {
                lipSyncValue = value
                isUpdatedLipSync = true
            } else if (curve.id.equals(modelCurveIdOpacity)) {
                this.modelOpacityValue = value

                // 不透明度の値が存在すれば反映する。
                model.modelOpacity = this.modelOpacityValue
            }
        }

        val tmpFadeIn = if (fadeInSeconds <= 0.0f)
            1.0f
        else
            CubismMath.getEasingSine((userTimeSeconds - motionQueueEntry.getFadeInStartTime()) / fadeInSeconds)
        val tmpFadeOut = if (fadeOutSeconds <= 0.0f || motionQueueEntry.getEndTime() < 0.0f)
            1.0f
        else
            CubismMath.getEasingSine((motionQueueEntry.getEndTime() - userTimeSeconds) / fadeOutSeconds)

        for (i in curves.indices) {
            val curve: CubismMotionCurve = curves.get(i)

            if (curve.type !== CubismMotionCurveTarget.PARAMETER) {
                continue
            }

            // Find parameter index.
            val parameterIndex = model.getParameterIndex(curve.id)

            // Skip curve evaluation if no value.
            if (parameterIndex == -1) {
                continue
            }

            val sourceValue = model.getParameterValue(parameterIndex)

            // Evaluate curve and apply value.
            value = evaluateCurve(motionData, i, time, isCorrection, duration)

            if (isUpdatedEyeBlink) {
                for (j in eyeBlinkParameterIds.indices) {
                    val id: CubismId = eyeBlinkParameterIds.get(j)

                    if (j == MAX_TARGET_SIZE) {
                        break
                    }

                    if (id == curve.id) {
                        value *= eyeBlinkValue
                        eyeBlinkFlags.set(j)
                        break
                    }
                }
            }

            if (isUpdatedLipSync) {
                for (j in lipSyncParameterIds.indices) {
                    val id: CubismId = lipSyncParameterIds.get(j)

                    if (j == MAX_TARGET_SIZE) {
                        break
                    }

                    if (id == curve.id) {
                        value += lipSyncValue
                        lipSyncFlags.set(j)
                        break
                    }
                }
            }

            val v: Float
            if (existFade(curve)) {
                // If the parameter has a fade-in or fade-out setting, apply it.
                val fin: Float
                val fout: Float

                if (existFadeIn(curve)) {
                    val easedValue: Float =
                        (userTimeSeconds - motionQueueEntry.getFadeInStartTime()) / curve.fadeInTime

                    fin = if (curve.fadeInTime === 0.0f)
                        1.0f
                    else
                        CubismMath.getEasingSine(easedValue)
                } else {
                    fin = tmpFadeIn
                }

                if (existFadeOut(curve)) {
                    val easedValue: Float =
                        (motionQueueEntry.getEndTime() - userTimeSeconds) / curve.fadeOutTime

                    fout = if (curve.fadeOutTime === 0.0f || motionQueueEntry.getEndTime() < 0.0f)
                        1.0f
                    else
                        CubismMath.getEasingSine(easedValue)
                } else {
                    fout = tmpFadeOut
                }

                val paramWeight: Float = weight * fin * fout

                // Apply each fading.
                v = sourceValue + (value - sourceValue) * paramWeight
            } else {
                // Apply each fading.
                v = sourceValue + (value - sourceValue) * fadeWeight
            }
            model.setParameterValue(parameterIndex, v)
        }


        if (isUpdatedEyeBlink) {
            for (i in eyeBlinkParameterIds.indices) {
                val id: CubismId = eyeBlinkParameterIds.get(i)

                if (i == MAX_TARGET_SIZE) {
                    break
                }

                // Blink does not apply when there is a motion overriding.
                if (eyeBlinkFlags.get(i)) {
                    continue
                }

                val sourceValue: Float = model.getParameterValue(id)
                val v = sourceValue + (eyeBlinkValue - sourceValue) * fadeWeight

                model.setParameterValue(id, v)
            }
        }

        if (isUpdatedLipSync) {
            for (i in lipSyncParameterIds.indices) {
                val id: CubismId = lipSyncParameterIds.get(i)

                if (i == MAX_TARGET_SIZE) {
                    break
                }

                // Lip-sync does not apply when there is a motion overriding.
                if (lipSyncFlags.get(i)) {
                    continue
                }

                val sourceValue: Float = model.getParameterValue(id)

                val v = sourceValue + (lipSyncValue - sourceValue) * fadeWeight

                model.setParameterValue(id, v)
            }
        }

        val curveSize = curves.size
        for (i in 0..<curveSize) {
            val curve: CubismMotionCurve = curves.get(i)

            if (curve.type !== CubismMotionCurveTarget.PART_OPACITY) {
                continue
            }

            // Find parameter index.
            val parameterIndex = model.getParameterIndex(curve.id)

            // Skip curve evaluation if no value.
            if (parameterIndex == -1) {
                continue
            }

            // Evaluate curve and apply value.
            value = evaluateCurve(motionData, i, time, isCorrection, duration)
            model.setParameterValue(parameterIndex, value)
        }

        if (timeOffsetSeconds >= duration) {
            if (isLoop) {
                UpdateForNextLoop(motionQueueEntry, userTimeSeconds, time)
            } else {
                if (onFinishedMotion != null) {
                    onFinishedMotion.execute(this)
                }
                motionQueueEntry.isFinished(true)
            }
        }
        lastWeight = fadeWeight
    }

    private fun UpdateForNextLoop(
        motionQueueEntry: CubismMotionQueueEntry,
        userTimeSeconds: Float,
        time: Float
    ) {
        when (motionBehavior) {
            MotionBehavior.MOTION_BEHAVIOR_V1 -> {
                // 旧ループ処理
                motionQueueEntry.setStartTime(userTimeSeconds) //最初の状態へ
                if (isLoopFadeIn) {
                    //ループ中でループ用フェードインが有効のときは、フェードイン設定し直し
                    motionQueueEntry.setFadeInStartTime(userTimeSeconds)
                }
            }

            MotionBehavior.MOTION_BEHAVIOR_V2 -> {
                motionQueueEntry.setStartTime(userTimeSeconds - time) //最初の状態へ
                if (isLoopFadeIn) {
                    //ループ中でループ用フェードインが有効のときは、フェードイン設定し直し
                    motionQueueEntry.setFadeInStartTime(userTimeSeconds - time)
                }

                if (this.onFinishedMotion != null) {
                    this.onFinishedMotion.execute(this)
                }
            }

            else -> {
                motionQueueEntry.setStartTime(userTimeSeconds - time)
                if (isLoopFadeIn) {
                    motionQueueEntry.setFadeInStartTime(userTimeSeconds - time)
                }

                if (this.onFinishedMotion != null) {
                    this.onFinishedMotion.execute(this)
                }
            }
        }
    }

    private enum class EffectName(name: String) {
        EYE_BLINK("EyeBlink"),
        LIP_SYNC("LipSync");

        private val name: String?

        init {
            this.name = name
        }
    }

    private enum class TargetName(
        val name: String
    ) {
        MODEL("Model"),
        PARAMETER("Parameter"),
        PART_OPACITY("PartOpacity");
    }

    private class LinearEvaluator : CsmMotionSegmentEvaluationFunction {
        public override fun evaluate(points: MutableList<CubismMotionPoint?>, time: Float): Float {
            var t: Float = (time - points.get(0).time) / (points.get(1).time - points.get(0).time)

            if (t < 0.0f) {
                t = 0.0f
            }

            return points.get(0).value + ((points.get(1).value - points.get(0).value) * t)
        }
    }

    private class BezierEvaluator : CsmMotionSegmentEvaluationFunction {
        public override fun evaluate(points: MutableList<CubismMotionPoint?>, time: Float): Float {
            var t: Float = (time - points.get(0).time) / (points.get(3).time - points.get(0).time)

            if (t < 0.0f) {
                t = 0.0f
            }

            val p01: CubismMotionPoint = lerpPoints(points.get(0), points.get(1), t)
            val p12: CubismMotionPoint = lerpPoints(points.get(1), points.get(2), t)
            val p23: CubismMotionPoint = lerpPoints(points.get(2), points.get(3), t)

            val p012: CubismMotionPoint = lerpPoints(p01, p12, t)
            val p123: CubismMotionPoint = lerpPoints(p12, p23, t)

            return lerpPoints(p012, p123, t).value
        }
    }

    private class BezierEvaluatorCardanoInterpretation : CsmMotionSegmentEvaluationFunction {
        public override fun evaluate(points: MutableList<CubismMotionPoint?>, time: Float): Float {
            val x1: Float = points.get(0).time
            val x2: Float = points.get(3).time
            val cx1: Float = points.get(1).time
            val cx2: Float = points.get(2).time

            val a = x2 - 3.0f * cx2 + 3.0f * cx1 - x1
            val b = 3.0f * cx2 - 6.0f * cx1 + 3.0f * x1
            val c = 3.0f * cx1 - 3.0f * x1
            val d = x1 - time

            val t: Float = CubismMath.cardanoAlgorithmForBezier(a, b, c, d)

            val p01: CubismMotionPoint = lerpPoints(points.get(0), points.get(1), t)
            val p12: CubismMotionPoint = lerpPoints(points.get(1), points.get(2), t)
            val p23: CubismMotionPoint = lerpPoints(points.get(2), points.get(3), t)

            val p012: CubismMotionPoint = lerpPoints(p01, p12, t)
            val p123: CubismMotionPoint = lerpPoints(p12, p23, t)

            return lerpPoints(p012, p123, t).value
        }
    }

    private class SteppedEvaluator : CsmMotionSegmentEvaluationFunction {
        public override fun evaluate(points: MutableList<CubismMotionPoint?>, time: Float): Float {
            return points.get(0).value
        }
    }

    private class InverseSteppedEvaluator : CsmMotionSegmentEvaluationFunction {
        public override fun evaluate(points: MutableList<CubismMotionPoint?>, time: Float): Float {
            return points.get(1).value
        }
    }

    /**
     * Check for the presence of fade-in.
     *
     * @param curve motion curve instance
     * @return If fade-in exists, return true.
     */
    private fun existFadeIn(curve: CubismMotionCurve): Boolean {
        return curve.fadeInTime >= 0.0f
    }

    /**
     * Check for the presence of fade-out.
     *
     * @param curve motion curve instance
     * @return If fade-out exists, return true.
     */
    private fun existFadeOut(curve: CubismMotionCurve): Boolean {
        return curve.fadeOutTime >= 0.0f
    }

    /**
     * Check for the presence of fading.
     *
     * @param curve motion curve instance
     * @return If fading exists, return true.
     */
    private fun existFade(curve: CubismMotionCurve): Boolean {
        return existFadeIn(curve) || existFadeOut(curve)
    }

    /**
     * Parse motion3.json.
     *
     * @param motionJson buffer where motion3.json is loaded
     */
    private fun parse(motionJson: ByteArray) {
        val json = Json.decodeFromString<MotionJson>(String(motionJson))

        motionData = CubismMotionData()

        motionData.duration = json.meta.duration
        motionData.isLooped = json.meta.loop
        motionData.curveCount = json.meta.curveCount
        motionData.fps = json.meta.fps
        motionData.eventCount = json.meta.userDataCount

        val areBeziersRestricted: Boolean =
            json.meta.areBeziersRestricted

        fadeInSeconds = json.meta.fadeInTime?.let {
            if (it < 0.0f)
                1.0f
            else
                it
        } ?: 1.0f
        fadeOutSeconds = json.meta.fadeOutTime?.let {
            if (it < 0.0f)
                1.0f
            else
                it
        } ?: 1.0f

        motionData.curves = ArrayList<CubismMotionCurve>(motionData.curveCount)
        repeat(motionData.curveCount) {
            motionData.curves.add(CubismMotionCurve())
        }

        motionData.segments = ArrayList<CubismMotionSegment>(json.meta.totalSegmentCount)
        repeat(json.meta.totalSegmentCount) {
            motionData.segments.add(CubismMotionSegment())
        }

        motionData.points = ArrayList<CubismMotionPoint>(json.meta.totalPointCount)
        repeat(json.meta.totalPointCount) {
            motionData.points.add(CubismMotionPoint())
        }

        motionData.events = ArrayList<CubismMotionEvent>(motionData.eventCount)
        repeat(motionData.eventCount) {
            motionData.events.add(CubismMotionEvent())
        }

        var totalPointCount = 0
        var totalSegmentCount = 0

        // Curves
        for (curveIndex in 0..<motionData.curveCount) {
            motionData.curves[curveIndex].let { curve ->
                // Register target type.
                val targetName = json.curves[curveIndex].target
                curve.type = when (targetName) {
                    TargetName.MODEL.name -> CubismMotionCurveTarget.MODEL
                    TargetName.PARAMETER.name -> CubismMotionCurveTarget.PARAMETER
                    TargetName.PART_OPACITY.name -> CubismMotionCurveTarget.PART_OPACITY
                    else -> error("Warning: Unable to get segment type from Curve! The number of \"CurveCount\" may be incorrect!")
                }

                curve.id = idManager.id(
                    json.curves[curveIndex].id
                )
                curve.baseSegmentIndex = totalSegmentCount
                curve.fadeInTime =
                    json.curves[curveIndex].fadeInTime?.let {
                        it
                    } ?: -1.0f
                curve.fadeOutTime =
                    json.curves[curveIndex].fadeOutTime?.let {
                        it
                    } ?: -1.0f

            }

            // Segments
            var segmentIndex = 0
            while (segmentIndex < json.meta.totalSegmentCount) {
                // 起始点
                if (segmentIndex == 0) {
                    motionData.segments[totalSegmentCount].basePointIndex = totalPointCount

                    motionData.points[totalPointCount].time =
                        json.curves[curveIndex].segments[0]
                    motionData.points[totalPointCount].value =
                        json.curves[curveIndex].segments[1]

                    totalPointCount += 1
                    segmentIndex += 2
                } else {
                    motionData.segments[totalSegmentCount].basePointIndex = totalPointCount - 1
                }

                val segmentType: CubismMotionSegmentType
                json.curves[curveIndex].segments[segmentIndex].toInt().let { flagSegment ->
                    segmentType = when (flagSegment) {
                        0 -> CubismMotionSegmentType.LINEAR
                        1 -> CubismMotionSegmentType.BEZIER
                        2 -> CubismMotionSegmentType.STEPPED
                        3 -> CubismMotionSegmentType.INVERSESTEPPED
                        else -> error("unsupported segmentType!")
                    }
                }

                when (segmentType) {
                    CubismMotionSegmentType.LINEAR -> {
                        motionData.segments[totalSegmentCount].let { segment ->
                            segment.segmentType = CubismMotionSegmentType.LINEAR
                            segment.evaluator = linearEvaluator
                        }
                        motionData.points[totalPointCount].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 1]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 2]
                        }

                        totalPointCount += 1
                        segmentIndex += 3
                    }

                    CubismMotionSegmentType.BEZIER -> {
                        motionData.segments[totalSegmentCount].let { segment ->
                            segment.segmentType = CubismMotionSegmentType.BEZIER

                            segment.evaluator =
                                if (areBeziersRestricted || USE_OLD_BEZIERS_CURVE_MOTION)
                                    bezierEvaluator
                                else
                                    bezierCardanoInterpretationEvaluator
                        }

                        motionData.points.get(totalPointCount).time =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 1))
                        motionData.points.get(totalPointCount).value =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 2))

                        motionData.points.get(totalPointCount + 1).time =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 3))
                        motionData.points.get(totalPointCount + 1).value =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 4))

                        motionData.points.get(totalPointCount + 2).time =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 5))
                        motionData.points.get(totalPointCount + 2).value =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 6))

                        totalPointCount += 3
                        segmentIndex += 7
                    }

                    STEPPED -> {
                        motionData.segments.get(totalSegmentCount).segmentType =
                            CubismMotionSegmentType.STEPPED
                        motionData.segments.get(totalSegmentCount).evaluator = steppedEvaluator

                        motionData.points.get(totalPointCount).time =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 1))
                        motionData.points.get(totalPointCount).value =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 2))

                        totalPointCount += 1
                        segmentIndex += 3
                    }

                    INVERSESTEPPED -> {
                        motionData.segments.get(totalSegmentCount).segmentType =
                            CubismMotionSegmentType.INVERSESTEPPED
                        motionData.segments.get(totalSegmentCount).evaluator =
                            inverseSteppedEvaluator

                        motionData.points.get(totalPointCount).time =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 1))
                        motionData.points.get(totalPointCount).value =
                            json.getMotionCurveSegment(curveIndex, (segmentIndex + 2))

                        totalPointCount += 1
                        segmentIndex += 3
                    }

                    else -> {
                        assert(false)
                    }
                }

                ++motionData.curves.get(curveIndex).segmentCount
                ++totalSegmentCount
            }
        }

        for (userdatacount in 0..<json.getEventCount()) {
            motionData.events.get(userdatacount).fireTime = json.getEventTime(userdatacount)
            motionData.events.get(userdatacount).value = json.getEventValue(userdatacount)
        }
    }

    private fun bezierEvaluateBinarySearch(points: Array<CubismMotionPoint?>, time: Float): Float {
        val x_error = 0.01f

        var x1: Float = points[0].time
        var x2: Float = points[3].time
        var cx1: Float = points[1].time
        var cx2: Float = points[2].time

        var ta = 0.0f
        var tb = 1.0f
        var t = 0.0f
        var i = 0

        val var33 = true
        while (i < 20) {
            if (time < x1 + x_error) {
                t = ta
                break
            }

            if (x2 - x_error < time) {
                t = tb
                break
            }

            var centerx = (cx1 + cx2) * 0.5f
            cx1 = (x1 + cx1) * 0.5f
            cx2 = (x2 + cx2) * 0.5f
            val ctrlx12 = (cx1 + centerx) * 0.5f
            val ctrlx21 = (cx2 + centerx) * 0.5f
            centerx = (ctrlx12 + ctrlx21) * 0.5f
            if (time < centerx) {
                tb = (ta + tb) * 0.5f
                if (centerx - x_error < time) {
                    t = tb
                    break
                }

                x2 = centerx
                cx2 = ctrlx12
            } else {
                ta = (ta + tb) * 0.5f
                if (time < centerx + x_error) {
                    t = ta
                    break
                }

                x1 = centerx
                cx1 = ctrlx21
            }
            ++i
        }

        if (i == 20) {
            t = (ta + tb) * 0.5f
        }

        if (t < 0.0f) {
            t = 0.0f
        }
        if (t > 1.0f) {
            t = 1.0f
        }

        val p01: CubismMotionPoint = lerpPoints(points[0], points[1], t)
        val p12: CubismMotionPoint = lerpPoints(points[1], points[2], t)
        val p23: CubismMotionPoint = lerpPoints(points[2], points[3], t)

        val p012: CubismMotionPoint = lerpPoints(p01, p12, t)
        val p123: CubismMotionPoint = lerpPoints(p12, p23, t)

        return lerpPoints(p012, p123, t).value
    }

    private fun correctEndPoint(
        motionData: CubismMotionData,
        segmentIndex: Int,
        beginIndex: Int,
        endIndex: Int,
        time: Float,
        endTime: Float
    ): Float {
        val motionPoint: ArrayList<CubismMotionPoint?> = ArrayList<CubismMotionPoint?>(2)
        run {
            val src: CubismMotionPoint = motionData.points.get(endIndex)
            motionPoint.add(CubismMotionPoint(src.time, src.value))
        }
        run {
            val src: CubismMotionPoint = motionData.points.get(beginIndex)
            motionPoint.add(CubismMotionPoint(src.time, src.value))
        }
        motionPoint.get(1).time = endTime

        when (motionData.segments.get(segmentIndex).segmentType) {
            STEPPED -> return steppedEvaluator.evaluate(motionPoint, time)
            INVERSESTEPPED -> return inverseSteppedEvaluator.evaluate(motionPoint, time)
            LINEAR, BEZIER -> return linearEvaluator.evaluate(motionPoint, time)
            else -> return linearEvaluator.evaluate(motionPoint, time)
        }
    }

    private fun evaluateCurve(
        motionData: CubismMotionData,
        index: Int,
        time: Float,
        isCorrection: Boolean,
        endTime: Float
    ): Float {
        // Find segment to evaluate.
        val curve: CubismMotionCurve = motionData.curves.get(index)

        var target = -1
        val totalSegmentCount: Int = curve.baseSegmentIndex + curve.segmentCount
        var pointPosition = 0
        for (i in curve.baseSegmentIndex..<totalSegmentCount) {
            // Get first point of next segment.
            pointPosition = (motionData.segments.get(i).basePointIndex
                    + (if (motionData.segments.get(i).segmentType === CubismMotionSegmentType.BEZIER)
                3
            else
                1))

            // Break if time lies within current segment.
            if (motionData.points.get(pointPosition).time > time) {
                target = i
                break
            }
        }

        if (target == -1) {
            if (isCorrection && time < endTime) {
                // 終点から始点への補正処理
                return correctEndPoint(
                    motionData,
                    totalSegmentCount - 1,
                    motionData.segments.get(curve.baseSegmentIndex).basePointIndex,
                    pointPosition,
                    time,
                    endTime
                )
            }

            return motionData.points.get(pointPosition).value
        }

        val segment: CubismMotionSegment = motionData.segments.get(target)

        val points: MutableList<CubismMotionPoint?>? =
            motionData.points.subList(segment.basePointIndex, motionData.points.size())
        return segment.evaluator.evaluate(points, time)
    }

    /**
     * FPS of the loaded file; if not specified, the default value is 30 fps.
     */
    private var sourceFrameRate = 30.0f

    /**
     * length of the sequence of motions defined in the motion3.json file.
     */
    var loopDuration: Float = -1.0f
        private set

    /**
     * Gets the version of the Motion Behavior.
     *
     * @return Returns the version of the Motion Behavior.
     */
    /**
     * Sets the version of the Motion Behavior.
     *
     * @param motionBehavior the version of the Motion Behavior.
     */
    var motionBehavior: MotionBehavior = MotionBehavior.MOTION_BEHAVIOR_V2

    /**
     * last set weight
     */
    private var lastWeight = 0f

    /**
     * actual motion data itself
     */
    private lateinit var motionData: CubismMotionData

    /**
     * list of parameter ID handles to which automatic eye blinking is applied. Corresponds to a model (model setting) and a parameter.
     */
    private val eyeBlinkParameterIds: MutableList<CubismId> = ArrayList<CubismId>()

    /**
     * list of parameter ID handles to which lip-syncing is applied. Corresponds to a model (model setting) and a parameter.
     */
    private val lipSyncParameterIds: MutableList<CubismId> = ArrayList<CubismId>()

    private val eyeBlinkFlags = BitSet(eyeBlinkParameterIds.size)
    private val lipSyncFlags = BitSet(lipSyncParameterIds.size)

    /**
     * handle to the parameter ID for automatic eye blinking that the model has. Map a model to a motion.
     */
    private var modelCurveIdEyeBlink: CubismId? = null

    /**
     * handle to the parameter ID for lip-syncing that the model has. Map a model to a motion.
     */
    private var modelCurveIdLipSync: CubismId? = null

    /**
     * handle to the parameter ID for opacity that the moder has. Map a model to a motion.
     */
    private var modelCurveIdOpacity: CubismId? = null

    /**
     * モーションから取得した不透明度
     */
    var modelOpacityValue: Float = 0f
        private set


    private val linearEvaluator = LinearEvaluator()
    private val bezierEvaluator = BezierEvaluator()
    private val bezierCardanoInterpretationEvaluator = BezierEvaluatorCardanoInterpretation()
    private val steppedEvaluator = SteppedEvaluator()
    private val inverseSteppedEvaluator = InverseSteppedEvaluator()

    companion object {
        /**
         * Create an instance.
         *
         * @param buffer buffer where motion3.json is loaded
         * @param finishedMotionCallBack callback function called at the end of motion playback, not called if null.
         * @param beganMotionCallBack callback function called at the start of motion playback, not called if null.
         * @return instance of CubismMotion
         */
        /**
         * Create an instance.
         * If callback function is not specified, it becomes 'null'.
         *
         * @param buffer buffer where motion3.json is loaded.
         * @return instance of CubismMotion
         */
        @JvmOverloads
        fun create(
            buffer: ByteArray?,
            finishedMotionCallBack: IFinishedMotionCallback? = null,
            beganMotionCallBack: IBeganMotionCallback? = null
        ): CubismMotion {
            val motion = CubismMotion()
            motion.parse(buffer)

            motion.sourceFrameRate = motion.motionData.fps
            motion.loopDuration = motion.motionData.duration
            motion.onFinishedMotion = finishedMotionCallBack
            motion.onBeganMotion = beganMotionCallBack

            // NOTE: Exporting motion with loop is not supported in Editor.
            return motion
        }

        // ID
        private const val ID_NAME_OPACITY = "Opacity"

        /**
         * It is set to "true" to reproduce the motion of Cubism SDK R2 or earlier, or "false" to reproduce the animator's motion correctly.
         */
        private const val USE_OLD_BEZIERS_CURVE_MOTION = false


        // lerp: Linear Interpolate(線形補間の略)
        private fun lerpPoints(
            a: CubismMotionPoint,
            b: CubismMotionPoint,
            t: Float
        ): CubismMotionPoint {
            val result: CubismMotionPoint = CubismMotionPoint()

            result.time = a.time + ((b.time - a.time) * t)
            result.value = a.value + ((b.value - a.value) * t)

            return result
        }
    }
}

