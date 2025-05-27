/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.CubismFramework.idManager
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.math.CubismMath
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.motion.CubismMotionInternal.*
import com.live2d.sdk.cubism.framework.utils.json.MotionJson
import kotlinx.serialization.json.Json
import java.util.BitSet
import java.util.Collections
import kotlin.math.max

/**
 * Motion class.
 */
class CubismMotion : ACubismMotion() {

    constructor(
        buffer: ByteArray,
        finishedMotionCallBack: IFinishedMotionCallback = IFinishedMotionCallback { },
        beganMotionCallBack: IBeganMotionCallback = IBeganMotionCallback { }
    ) {
        parse(buffer)

        sourceFrameRate = motionData.fps
        loopDurationSeconds = motionData.duration
        beganMotionCallback = beganMotionCallBack
        finishedMotionCallback = finishedMotionCallBack
    }

    private fun parse(motionJson: ByteArray) {
        val json = Json.decodeFromString<MotionJson>(String(motionJson))

        motionData = CubismMotionData(
            duration = json.meta.duration,
            loop = json.meta.loop,
            curveCount = json.meta.curveCount,
            userDataCount = json.meta.userDataCount,
            fps = json.meta.fps,
            curves = ArrayList<CubismMotionCurve>(json.meta.curveCount).apply {
                repeat(json.meta.curveCount) {
                    add(CubismMotionCurve())
                }
            },
            segments = ArrayList<CubismMotionSegment>(json.meta.totalSegmentCount).apply {
                repeat(json.meta.totalSegmentCount) {
                    add(CubismMotionSegment())
                }
            },
            points = ArrayList<CubismMotionPoint>(json.meta.totalPointCount).apply {
                repeat(json.meta.totalPointCount) {
                    add(CubismMotionPoint())
                }
            },
            events = ArrayList<CubismMotionEvent>(json.meta.userDataCount).apply {
                repeat(motionData.userDataCount) {
                    add(CubismMotionEvent())
                }
            }
        )

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

        var totalPointIndex = 0
        var totalSegmentIndex = 0

        // Curves
        for (curveIndex in 0..<motionData.curveCount) {
            motionData.curves[curveIndex].let { curve ->
                // Register target type.
                curve.type = CubismMotionCurveTarget.byName(
                    json.curves[curveIndex].target
                )

                curve.id = idManager.id(
                    json.curves[curveIndex].id
                )
                curve.baseSegmentIndex = totalSegmentIndex
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
                    motionData.segments[totalSegmentIndex].basePointIndex = totalPointIndex

                    motionData.points[totalPointIndex].time =
                        json.curves[curveIndex].segments[0]
                    motionData.points[totalPointIndex].value =
                        json.curves[curveIndex].segments[1]

                    totalPointIndex += 1
                    segmentIndex += 2
                } else {
                    motionData.segments[totalSegmentIndex].basePointIndex = totalPointIndex - 1
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
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionSegmentType.LINEAR
                            segment.evaluator = LinearEvaluator
                        }
                        // 线性需要另外一个点
                        motionData.points[totalPointIndex].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 1]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 2]
                        }

                        totalPointIndex += 1
                        segmentIndex += 3
                    }

                    CubismMotionSegmentType.BEZIER -> {
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionSegmentType.BEZIER

                            segment.evaluator =
                                if (json.meta.areBeziersRestricted || USE_OLD_BEZIERS_CURVE_MOTION)
                                    BezierEvaluator
                                else
                                    BezierEvaluatorCardanoInterpretation
                        }

                        // 贝塞尔曲线需要另外三个点
                        motionData.points[totalPointIndex].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 1]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 2]
                        }
                        motionData.points[totalPointIndex + 1].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 3]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 4]
                        }
                        motionData.points[totalPointIndex + 2].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 5]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 6]
                        }

                        totalPointIndex += 3
                        segmentIndex += 7
                    }

                    CubismMotionSegmentType.STEPPED -> {
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionSegmentType.STEPPED
                            segment.evaluator = SteppedEvaluator
                        }

                        motionData.points[totalPointIndex].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 1]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 2]
                        }

                        totalPointIndex += 1
                        segmentIndex += 3
                    }

                    CubismMotionSegmentType.INVERSESTEPPED -> {
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionSegmentType.INVERSESTEPPED
                            segment.evaluator = InverseSteppedEvaluator
                        }

                        motionData.points[totalPointIndex].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 1]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 2]
                        }

                        totalPointIndex += 1
                        segmentIndex += 3
                    }
                }

                ++motionData.curves[curveIndex].segmentCount
                ++totalSegmentIndex
            }
        }

        repeat(json.meta.userDataCount) { userDataIndex ->
            motionData.events[userDataIndex].fireTime = json.userData!![userDataIndex].time
            motionData.events[userDataIndex].value = json.userData!![userDataIndex].value
        }
    }

    enum class MotionBehavior {
        //        MOTION_BEHAVIOR_V1,
        MOTION_BEHAVIOR_V2,
    }

    fun setParameterFadeInTime(parameterId: CubismId, value: Float) {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                curve.fadeInTime = value
                return
            }
        }
    }

    fun getParameterFadeInTime(parameterId: CubismId): Float {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                return curve.fadeInTime
            }
        }
        return -1f
    }

    fun setParameterFadeOutTime(parameterId: CubismId, value: Float) {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                curve.fadeOutTime = value
                return
            }
        }
    }

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

    override fun getFiredEvent(
        beforeCheckTimeSeconds: Float,
        motionTimeSeconds: Float
    ): MutableList<String?> {
        firedEventValues.clear()

        for (event in motionData.events) {
            if (event.fireTime in beforeCheckTimeSeconds..motionTimeSeconds) {
//            }
//            if ((event.fireTime > beforeCheckTimeSeconds) && (event.fireTime <= motionTimeSeconds)) {
                firedEventValues.add(event.value)
            }
        }
        return Collections.unmodifiableList<String?>(firedEventValues)
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

    private fun updateForNextLoop(
        motionQueueEntry: CubismMotionQueueEntry,
        totalSeconds: Float,
        time: Float
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
                motionQueueEntry.startTimePoint = totalSeconds - time //最初の状態へ
                if (loopFadeIn) {
                    //ループ中でループ用フェードインが有効のときは、フェードイン設定し直し
                    // TODO:: 你知道我要todo什么
//                    motionQueueEntry.setFadeInStartTime(totalSeconds - time)
                }
            }
        }
    }


    private fun evaluateCurve(
        curve: CubismMotionCurve,
        time: Float,
        isCorrection: Boolean,
        endTime: Float
    ): Float {

        val nextBaseSegmentIndex: Int = curve.baseSegmentIndex + curve.segmentCount
        var nextSegmentBasicPointIndex = 0
        return (curve.baseSegmentIndex until nextBaseSegmentIndex).firstOrNull {
            // Get first point of next segment.
            nextSegmentBasicPointIndex =
                motionData.segments[it].basePointIndex + motionData.segments[it].segmentType.pointCount
            // Break if time lies within current segment.
            motionData.points[nextSegmentBasicPointIndex].time > time
        }?.let { it: Int ->

            val points: MutableList<CubismMotionPoint> =
                motionData.points.subList(
                    motionData.segments[it].basePointIndex,
                    nextSegmentBasicPointIndex
                )

            motionData.segments[it].evaluator.evaluate(points, time)
        } ?: run {
            if (isCorrection && time < endTime) {
                // 終点から始点への補正処理
                correctEndPoint(
                    motionData,
                    nextBaseSegmentIndex - 1,
                    motionData.segments[curve.baseSegmentIndex].basePointIndex,
                    nextSegmentBasicPointIndex,
                    time,
                    endTime
                )
            }

            motionData.points[nextSegmentBasicPointIndex].value
        }

    }

    private fun correctEndPoint(
        motionData: CubismMotionData,
        segmentIndex: Int,
        beginIndex: Int,
        endIndex: Int,
        time: Float,
        endTime: Float
    ): Float {
        val motionPoint = listOf(
            run {
                val src: CubismMotionPoint = motionData.points[endIndex]
                CubismMotionPoint(src.time, src.value)
            },
            run {
                val src: CubismMotionPoint = motionData.points[beginIndex]
                CubismMotionPoint(endTime, src.value)
            },
        )

        return when (motionData.segments[segmentIndex].segmentType) {
            CubismMotionSegmentType.STEPPED -> SteppedEvaluator.evaluate(
                motionPoint,
                time
            )

            CubismMotionSegmentType.INVERSESTEPPED -> InverseSteppedEvaluator.evaluate(
                motionPoint,
                time
            )

            CubismMotionSegmentType.LINEAR,
            CubismMotionSegmentType.BEZIER -> LinearEvaluator.evaluate(
                motionPoint,
                time
            )

        }
    }


    object LinearEvaluator : CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionPoint>, time: Float): Float {
            val p0 = points[0]
            val p1 = points[1]

            val t: Float = max(
                (time - p0.time) / (p1.time - p0.time),
                0.0f
            )

            return p0.value + ((p1.value - p0.value) * t)
        }
    }

    object BezierEvaluator : CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionPoint>, time: Float): Float {
            val p0 = points[0]
            val p1 = points[1]
            val p2 = points[2]
            val p3 = points[3]

            val t: Float = max(
                (time - p0.time) / (p3.time - p0.time),
                0.0f
            )

            val p01: CubismMotionPoint = lerpPoints(p0, p1, t)
            val p12: CubismMotionPoint = lerpPoints(p1, p2, t)
            val p23: CubismMotionPoint = lerpPoints(p2, p3, t)

            val p012: CubismMotionPoint = lerpPoints(p01, p12, t)
            val p123: CubismMotionPoint = lerpPoints(p12, p23, t)

            return lerpPoints(p012, p123, t).value
        }
    }

    object BezierEvaluatorCardanoInterpretation : CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionPoint>, time: Float): Float {
            val p0 = points[0]
            val p1 = points[1]
            val p2 = points[2]
            val p3 = points[3]

            val x1: Float = p0.time
            val x2: Float = p3.time
            val cx1: Float = p1.time
            val cx2: Float = p2.time

            val a = x2 - 3.0f * cx2 + 3.0f * cx1 - x1
            val b = 3.0f * cx2 - 6.0f * cx1 + 3.0f * x1
            val c = 3.0f * cx1 - 3.0f * x1
            val d = x1 - time

            val t: Float = CubismMath.cardanoAlgorithmForBezier(a, b, c, d)

            val p01: CubismMotionPoint = lerpPoints(p0, p1, t)
            val p12: CubismMotionPoint = lerpPoints(p1, p2, t)
            val p23: CubismMotionPoint = lerpPoints(p2, p3, t)

            val p012: CubismMotionPoint = lerpPoints(p01, p12, t)
            val p123: CubismMotionPoint = lerpPoints(p12, p23, t)

            return lerpPoints(p012, p123, t).value
        }
    }

    object SteppedEvaluator : CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionPoint>, time: Float): Float {
            return points[0].value
        }
    }

    object InverseSteppedEvaluator : CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionPoint>, time: Float): Float {
            return points[1].value
        }
    }

    private fun existFadeIn(curve: CubismMotionCurve): Boolean {
        return curve.fadeInTime >= 0.0f
    }

    private fun existFadeOut(curve: CubismMotionCurve): Boolean {
        return curve.fadeOutTime >= 0.0f
    }

    private fun existFade(curve: CubismMotionCurve): Boolean {
        return existFadeIn(curve) || existFadeOut(curve)
    }

    var sourceFrameRate = 30.0f
    var loopDurationSeconds: Float = -1.0f

    private lateinit var motionData: CubismMotionData
    private val eyeBlinkParameterIds: MutableList<CubismId> = ArrayList<CubismId>()
    private val lipSyncParameterIds: MutableList<CubismId> = ArrayList<CubismId>()
    private val eyeBlinkOverrideFlags = BitSet(eyeBlinkParameterIds.size)
    private val lipSyncOverrideFlags = BitSet(lipSyncParameterIds.size)

    override var modelOpacityValue: Float = 0f

    companion object {

        private enum class EffectID(
            val value: String
        ) {
            EYE_BLINK("EyeBlink"),
            LIP_SYNC("LipSync");
        }

        private enum class OpacityID(
            val value: String
        ) {
            OPACITY("Opacity")
        }

        val modelCurveIdEyeBlink: CubismId = idManager.id(EffectID.EYE_BLINK.value)

        val modelCurveIdLipSync: CubismId = idManager.id(EffectID.LIP_SYNC.value)

        val modelCurveIdOpacity: CubismId = idManager.id(OpacityID.OPACITY.value)

        /**
         * It is set to "true" to reproduce the motion of Cubism SDK R2 or earlier, or "false" to reproduce the animator's motion correctly.
         */
        private const val USE_OLD_BEZIERS_CURVE_MOTION = false


        // lerp: Linear Interpolate(線形補間の略)
        private fun lerpPoints(
            a: CubismMotionPoint,
            b: CubismMotionPoint,
            t: Float
        ): CubismMotionPoint = CubismMotionPoint(
            a.time + ((b.time - a.time) * t),
            a.value + ((b.value - a.value) * t),
        )
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
}

