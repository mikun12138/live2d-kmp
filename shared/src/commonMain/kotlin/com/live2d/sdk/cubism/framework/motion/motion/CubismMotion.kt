package com.live2d.sdk.cubism.framework.motion.motion

import com.live2d.sdk.cubism.framework.data.MotionJson
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.id.CubismIdManager
import com.live2d.sdk.cubism.framework.math.CubismMath
import com.live2d.sdk.cubism.framework.motion.ACubismMotion
import com.live2d.sdk.cubism.framework.motion.CubismMotionInternal
import com.live2d.sdk.cubism.framework.motion.CubismMotionInternal.CubismMotionPoint
import com.live2d.sdk.cubism.framework.motion.CubismMotionInternal.CubismMotionSegmentType
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import kotlinx.serialization.json.Json
import java.util.BitSet
import kotlin.math.max


/**
 * Motion class.
 */
class CubismMotion : ACubismMotion {

    constructor(
        buffer: ByteArray,
        finishedMotionCallBack: IFinishedMotionCallback = IFinishedMotionCallback { },
        beganMotionCallBack: IBeganMotionCallback = IBeganMotionCallback { },
    ) {
        parse(buffer)

        beganMotionCallback = beganMotionCallBack
        finishedMotionCallback = finishedMotionCallBack
    }

    private fun parse(motionJson: ByteArray) {
        val json = Json.decodeFromString<MotionJson>(String(motionJson))

        motionData = CubismMotionInternal.CubismMotionData(
            duration = json.meta.duration,
            loop = json.meta.loop,
            curveCount = json.meta.curveCount,
            userDataCount = json.meta.userDataCount,
            fps = json.meta.fps,
            curves = ArrayList<CubismMotionInternal.CubismMotionCurve>(json.meta.curveCount).apply {
                repeat(json.meta.curveCount) {
                    add(CubismMotionInternal.CubismMotionCurve())
                }
            },
            segments = ArrayList<CubismMotionInternal.CubismMotionSegment>(json.meta.totalSegmentCount).apply {
                repeat(json.meta.totalSegmentCount) {
                    add(CubismMotionInternal.CubismMotionSegment())
                }
            },
            points = ArrayList<CubismMotionInternal.CubismMotionPoint>(json.meta.totalPointCount).apply {
                repeat(json.meta.totalPointCount) {
                    add(CubismMotionInternal.CubismMotionPoint())
                }
            },
            events = ArrayList<CubismMotionInternal.CubismMotionEvent>(json.meta.userDataCount).apply {
                repeat(json.meta.userDataCount) {
                    add(CubismMotionInternal.CubismMotionEvent())
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
                curve.type = CubismMotionInternal.CubismMotionCurveTarget.Companion.byName(
                    json.curves[curveIndex].target
                )

                curve.id = CubismIdManager.id(
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
            while (segmentIndex < json.curves[curveIndex].segments.size) {
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

                val segmentType: CubismMotionInternal.CubismMotionSegmentType
                json.curves[curveIndex]
                    .segments[segmentIndex].toInt().let { flagSegment ->
                    segmentType = when (flagSegment) {
                        0 -> CubismMotionInternal.CubismMotionSegmentType.LINEAR
                        1 -> CubismMotionInternal.CubismMotionSegmentType.BEZIER
                        2 -> CubismMotionInternal.CubismMotionSegmentType.STEPPED
                        3 -> CubismMotionInternal.CubismMotionSegmentType.INVERSESTEPPED
                        else -> error("unsupported segmentType!")
                    }
                }

                when (segmentType) {
                    CubismMotionInternal.CubismMotionSegmentType.LINEAR -> {
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionInternal.CubismMotionSegmentType.LINEAR
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

                    CubismMotionInternal.CubismMotionSegmentType.BEZIER -> {
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionInternal.CubismMotionSegmentType.BEZIER

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

                    CubismMotionInternal.CubismMotionSegmentType.STEPPED -> {
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionInternal.CubismMotionSegmentType.STEPPED
                            segment.evaluator = SteppedEvaluator
                        }

                        motionData.points[totalPointIndex].let { point ->
                            point.time = json.curves[curveIndex].segments[segmentIndex + 1]
                            point.value = json.curves[curveIndex].segments[segmentIndex + 2]
                        }

                        totalPointIndex += 1
                        segmentIndex += 3
                    }

                    CubismMotionInternal.CubismMotionSegmentType.INVERSESTEPPED -> {
                        motionData.segments[totalSegmentIndex].let { segment ->
                            segment.segmentType = CubismMotionInternal.CubismMotionSegmentType.INVERSESTEPPED
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
/*

    fun setParameterFadeInTime(parameterId: CubismId, value: Float) {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionInternal.CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                curve.fadeInTime = value
                return
            }
        }
    }

    fun getParameterFadeInTime(parameterId: CubismId): Float {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionInternal.CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                return curve.fadeInTime
            }
        }
        return -1f
    }

    fun setParameterFadeOutTime(parameterId: CubismId, value: Float) {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionInternal.CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                curve.fadeOutTime = value
                return
            }
        }
    }

    fun getParameterFadeOutTime(parameterId: CubismId): Float {
        for (i in 0..<motionData.curves.size()) {
            val curve: CubismMotionInternal.CubismMotionCurve = motionData.curves.get(i)

            if (parameterId == curve.id) {
                return curve.fadeOutTime
            }
        }
        return -1f
    }

    */
    /**
     * Set the parameter ID list to which automatic effects are applied.
     *
     * @param eyeBlinkParameterIds parameter ID list to which automatic eye blinking is applied
     * @param lipSyncParameterIds parameter ID list to which automatic lip-syncing is applied
     */
    fun setEffectIds(
        eyeBlinkParameterIds: List<CubismId>,
        lipSyncParameterIds: List<CubismId>,
    ) {
        this.eyeBlinkParameterIds.clear()
        this.eyeBlinkParameterIds.addAll(eyeBlinkParameterIds)

        this.lipSyncParameterIds.clear()
        this.lipSyncParameterIds.addAll(lipSyncParameterIds)
    }

    fun evaluateCurve(
        curve: CubismMotionInternal.CubismMotionCurve,
        time: Float,
        isCorrection: Boolean,
        endTime: Float,
    ): Float {
        if (curve.id.value == "ParamLeg") {
            println("time: " + time);
            println("isCorrection: " + isCorrection);
            println("endTime: " + endTime);
            println()
        }

        var target = -1
        val totalSegmentCount = curve.baseSegmentIndex + curve.segmentCount
        if (curve.id.value == "ParamLeg") {
            println("baseSegmentIndex: " + curve.baseSegmentIndex)
            println("segmentCount: " + curve.segmentCount)
        }

        var pointPosition = 0
        for (i in curve.baseSegmentIndex..<totalSegmentCount) {
            // Get first point of next segment.
            pointPosition = (motionData.segments.get(i).basePointIndex
                    + (if (motionData.segments.get(i).segmentType == CubismMotionSegmentType.BEZIER)
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

        val segment = motionData.segments.get(target)

        val points: MutableList<CubismMotionPoint> =
            motionData.points.subList(segment.basePointIndex, motionData.points.size)
        return segment.evaluator.evaluate(points, time)

//        val nextBaseSegmentIndex: Int = curve.baseSegmentIndex + curve.segmentCount
//        var nextSegmentBasicPointIndex = 0
//        return (curve.baseSegmentIndex until nextBaseSegmentIndex).firstOrNull {
//            // Get first point of next segment.
//            nextSegmentBasicPointIndex =
//                motionData.segments[it].basePointIndex + motionData.segments[it].segmentType.pointCount
//            // Break if time lies within current segment.
//            motionData.points[nextSegmentBasicPointIndex].time > time
//        }?.let { it: Int ->
//
//            val points: MutableList<CubismMotionInternal.CubismMotionPoint> =
//                motionData.points.subList(
//                    motionData.segments[it].basePointIndex,
//                    nextSegmentBasicPointIndex
//                )
//
//            motionData.segments[it].evaluator.evaluate(points, time)
//        } ?: run {
//            if (isCorrection && time < endTime) {
//                // 終点から始点への補正処理
//                correctEndPoint(
//                    motionData,
//                    nextBaseSegmentIndex - 1,
//                    motionData.segments[curve.baseSegmentIndex].basePointIndex,
//                    nextSegmentBasicPointIndex,
//                    time,
//                    endTime
//                )
//            }
//
//            motionData.points[nextSegmentBasicPointIndex].value
//        }

    }

    private fun correctEndPoint(
        motionData: CubismMotionInternal.CubismMotionData,
        segmentIndex: Int,
        beginIndex: Int,
        endIndex: Int,
        time: Float,
        endTime: Float,
    ): Float {
        val motionPoint = listOf(
            run {
                val src: CubismMotionInternal.CubismMotionPoint = motionData.points[endIndex]
                CubismMotionInternal.CubismMotionPoint(src.time, src.value)
            },
            run {
                val src: CubismMotionInternal.CubismMotionPoint = motionData.points[beginIndex]
                CubismMotionInternal.CubismMotionPoint(endTime, src.value)
            },
        )

        return when (motionData.segments[segmentIndex].segmentType) {
            CubismMotionInternal.CubismMotionSegmentType.STEPPED -> SteppedEvaluator.evaluate(
                motionPoint,
                time
            )

            CubismMotionInternal.CubismMotionSegmentType.INVERSESTEPPED -> InverseSteppedEvaluator.evaluate(
                motionPoint,
                time
            )

            CubismMotionInternal.CubismMotionSegmentType.LINEAR,
            CubismMotionInternal.CubismMotionSegmentType.BEZIER,
                -> LinearEvaluator.evaluate(
                motionPoint,
                time
            )

        }
    }

    object LinearEvaluator : CubismMotionInternal.CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionInternal.CubismMotionPoint>, time: Float): Float {
            val p0 = points[0]
            val p1 = points[1]

            val t: Float = max(
                (time - p0.time) / (p1.time - p0.time),
                0.0f
            )

            return p0.value + ((p1.value - p0.value) * t)
        }
    }

    object BezierEvaluator : CubismMotionInternal.CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionInternal.CubismMotionPoint>, time: Float): Float {
            val p0 = points[0]
            val p1 = points[1]
            val p2 = points[2]
            val p3 = points[3]

            val t: Float = max(
                (time - p0.time) / (p3.time - p0.time),
                0.0f
            )

            val p01: CubismMotionInternal.CubismMotionPoint = lerpPoints(p0, p1, t)
            val p12: CubismMotionInternal.CubismMotionPoint = lerpPoints(p1, p2, t)
            val p23: CubismMotionInternal.CubismMotionPoint = lerpPoints(p2, p3, t)

            val p012: CubismMotionInternal.CubismMotionPoint = lerpPoints(p01, p12, t)
            val p123: CubismMotionInternal.CubismMotionPoint = lerpPoints(p12, p23, t)

            return lerpPoints(p012, p123, t).value
        }
    }

    object BezierEvaluatorCardanoInterpretation :
        CubismMotionInternal.CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionInternal.CubismMotionPoint>, time: Float): Float {
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

            val p01: CubismMotionInternal.CubismMotionPoint = lerpPoints(p0, p1, t)
            val p12: CubismMotionInternal.CubismMotionPoint = lerpPoints(p1, p2, t)
            val p23: CubismMotionInternal.CubismMotionPoint = lerpPoints(p2, p3, t)

            val p012: CubismMotionInternal.CubismMotionPoint = lerpPoints(p01, p12, t)
            val p123: CubismMotionInternal.CubismMotionPoint = lerpPoints(p12, p23, t)

            return lerpPoints(p012, p123, t).value
        }
    }

    object SteppedEvaluator : CubismMotionInternal.CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionInternal.CubismMotionPoint>, time: Float): Float {
            return points[0].value
        }
    }

    object InverseSteppedEvaluator : CubismMotionInternal.CsmMotionSegmentEvaluationFunction {
        override fun evaluate(points: List<CubismMotionInternal.CubismMotionPoint>, time: Float): Float {
            return points[1].value
        }
    }

    fun existFadeIn(curve: CubismMotionInternal.CubismMotionCurve): Boolean {
        return curve.fadeInTime >= 0.0f
    }

    fun existFadeOut(curve: CubismMotionInternal.CubismMotionCurve): Boolean {
        return curve.fadeOutTime >= 0.0f
    }

    fun existFade(curve: CubismMotionInternal.CubismMotionCurve): Boolean {
        return existFadeIn(curve) || existFadeOut(curve)
    }

    lateinit var motionData: CubismMotionInternal.CubismMotionData

    /**
     * Enable/Disable loop
     */
    // TODO::
    var loop: Boolean = false

    val eyeBlinkParameterIds: MutableList<CubismId> = mutableListOf()
    val lipSyncParameterIds: MutableList<CubismId> = mutableListOf()
    // TODO::
    val eyeBlinkOverrideFlags = BitSet(eyeBlinkParameterIds.size)
    val lipSyncOverrideFlags = BitSet(lipSyncParameterIds.size)

    // event / userdata
    var beganMotionCallback: IBeganMotionCallback = IBeganMotionCallback { }
    var finishedMotionCallback: IFinishedMotionCallback = IFinishedMotionCallback { }

    companion object {

        enum class MotionBehavior {
            //        MOTION_BEHAVIOR_V1,
            MOTION_BEHAVIOR_V2,
        }

        enum class EffectID(
            val value: String,
        ) {
            EYE_BLINK("EyeBlink"),
            LIP_SYNC("LipSync");
        }

        enum class OpacityID(
            val value: String,
        ) {
            OPACITY("Opacity")
        }

        /**
         * It is set to "true" to reproduce the motion of Cubism SDK R2 or earlier, or "false" to reproduce the animator's motion correctly.
         */
        private const val USE_OLD_BEZIERS_CURVE_MOTION = false


        // lerp: Linear Interpolate(線形補間の略)
        private fun lerpPoints(
            a: CubismMotionInternal.CubismMotionPoint,
            b: CubismMotionInternal.CubismMotionPoint,
            t: Float,
        ): CubismMotionInternal.CubismMotionPoint = CubismMotionInternal.CubismMotionPoint(
            a.time + ((b.time - a.time) * t),
            a.value + ((b.value - a.value) * t),
        )
    }

//    private fun bezierEvaluateBinarySearch(points: Array<CubismMotionInternal.CubismMotionPoint?>, time: Float): Float {
//        val x_error = 0.01f
//
//        var x1: Float = points[0].time
//        var x2: Float = points[3].time
//        var cx1: Float = points[1].time
//        var cx2: Float = points[2].time
//
//        var ta = 0.0f
//        var tb = 1.0f
//        var t = 0.0f
//        var i = 0
//
//        val var33 = true
//        while (i < 20) {
//            if (time < x1 + x_error) {
//                t = ta
//                break
//            }
//
//            if (x2 - x_error < time) {
//                t = tb
//                break
//            }
//
//            var centerx = (cx1 + cx2) * 0.5f
//            cx1 = (x1 + cx1) * 0.5f
//            cx2 = (x2 + cx2) * 0.5f
//            val ctrlx12 = (cx1 + centerx) * 0.5f
//            val ctrlx21 = (cx2 + centerx) * 0.5f
//            centerx = (ctrlx12 + ctrlx21) * 0.5f
//            if (time < centerx) {
//                tb = (ta + tb) * 0.5f
//                if (centerx - x_error < time) {
//                    t = tb
//                    break
//                }
//
//                x2 = centerx
//                cx2 = ctrlx12
//            } else {
//                ta = (ta + tb) * 0.5f
//                if (time < centerx + x_error) {
//                    t = ta
//                    break
//                }
//
//                x1 = centerx
//                cx1 = ctrlx21
//            }
//            ++i
//        }
//
//        if (i == 20) {
//            t = (ta + tb) * 0.5f
//        }
//
//        if (t < 0.0f) {
//            t = 0.0f
//        }
//        if (t > 1.0f) {
//            t = 1.0f
//        }
//
//        val p01: CubismMotionInternal.CubismMotionPoint = lerpPoints(points[0], points[1], t)
//        val p12: CubismMotionInternal.CubismMotionPoint = lerpPoints(points[1], points[2], t)
//        val p23: CubismMotionInternal.CubismMotionPoint = lerpPoints(points[2], points[3], t)
//
//        val p012: CubismMotionInternal.CubismMotionPoint = lerpPoints(p01, p12, t)
//        val p123: CubismMotionInternal.CubismMotionPoint = lerpPoints(p12, p23, t)
//
//        return lerpPoints(p012, p123, t).value
//    }
}