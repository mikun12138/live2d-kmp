package com.live2d.sdk.cubism.framework.motion.motion

import com.live2d.sdk.cubism.framework.id.Live2DId

/**
 * Internal data used by the CubismMotion class.
 */
class Live2DMotionInternal {
    /**
     * Type of motion curve.
     */
    enum class CubismMotionCurveTarget(
        val value: String
    ) {
        /**
         * motion curve for the model
         */
        MODEL("Model"),

        /**
         * motion curve for parameters
         */
        PARAMETER("Parameter"),

        /**
         * motion curve for part opacity
         */
        PART_OPACITY("PartOpacity")
        ;

        companion object {
            fun byName(name: String): CubismMotionCurveTarget {
                return entries.find { it.value == name }
                    ?: error("Unknown CubismMotionCurveTarget: [$name]")
            }
        }
    }

    /**
     * Type of motion curve segment.
     */
    enum class CubismMotionSegmentType(
        val pointCount: Int
    ) {
        LINEAR(1),

        BEZIER(3),

        STEPPED(1),

        INVERSESTEPPED(1)
    }

    /**
     * Motion curve control points.
     */
    data class CubismMotionPoint(
        var time: Float = 0f,
        var value: Float = 0f,
    )

    /**
     * Segment of motion curve.
     */
    class CubismMotionSegment {
        /**
         * used evaluation function
         */
        var evaluator: CsmMotionSegmentEvaluationFunction = Live2DMotion.LinearEvaluator

        /**
         * index to the first segment
         */
        var basePointIndex: Int = 0

        /**
         * type of segment
         */
        var segmentType: CubismMotionSegmentType = CubismMotionSegmentType.LINEAR
    }

    /**
     * Motion curve
     */
    class CubismMotionCurve {
        var type: CubismMotionCurveTarget = CubismMotionCurveTarget.MODEL
        lateinit var id: Live2DId

        /**
         * number of segments
         */
        var segmentCount: Int = 0

        /**
         * index to the first segment
         */
        var baseSegmentIndex: Int = 0

        var fadeInTime: Float = 0f
        var fadeOutTime: Float = 0f
    }

    /**
     * Motion event
     */
    class CubismMotionEvent {
        /**
         * duration of event
         */
        var fireTime: Float = 0f

        /**
         * value
         */
        lateinit var value: String
    }

    data class CubismMotionData(

        val duration: Float = 0f,

        val loop: Boolean = false,

        val curveCount: Int = 0,

        val userDataCount: Int = 0,

        val fps: Float = 0f,

        val curves: MutableList<CubismMotionCurve> = mutableListOf(),
        val segments: MutableList<CubismMotionSegment> = mutableListOf(),
        val points: MutableList<CubismMotionPoint> = mutableListOf(),
        val events: MutableList<CubismMotionEvent> = mutableListOf(),
    )

    /**
     * For strategy pattern.
     */
    interface CsmMotionSegmentEvaluationFunction {
        fun evaluate(points: List<CubismMotionPoint>, time: Float): Float
    }
}