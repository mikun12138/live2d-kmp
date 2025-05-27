/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.id.CubismId

/**
 * Internal data used by the CubismMotion class.
 */
class CubismMotionInternal {
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
        var evaluator: CsmMotionSegmentEvaluationFunction = null

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
        var id: CubismId

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
        var value: String? = null
    }

    data class CubismMotionData(

        val duration: Float = 0f
                get () = run {

            if (loop)
                -1.0f
            else
                field
        },


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
