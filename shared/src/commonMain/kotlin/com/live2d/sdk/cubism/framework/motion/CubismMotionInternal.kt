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
    enum class CubismMotionCurveTarget {
        /**
         * motion curve for the model
         */
        MODEL,

        /**
         * motion curve for parameters
         */
        PARAMETER,

        /**
         * motion curve for part opacity
         */
        PART_OPACITY
    }

    /**
     * Type of motion curve segment.
     */
    enum class CubismMotionSegmentType {
        /**
         * linear
         */
        LINEAR,

        /**
         * bezier curve
         */
        BEZIER,

        /**
         * step
         */
        STEPPED,

        /**
         * inverse step
         */
        INVERSESTEPPED
    }

    /**
     * Motion curve control points.
     */
    class CubismMotionPoint {
        /**
         * time[s]
         */
        var time: Float = 0f

        /**
         * value
         */
        var value: Float = 0f

        constructor()

        constructor(time: Float, value: Float) {
            this.time = time
            this.value = value
        }
    }

    /**
     * Segment of motion curve.
     */
    class CubismMotionSegment {
        /**
         * used evaluation function
         */
        var evaluator: CsmMotionSegmentEvaluationFunction? = null

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
        /**
         * type of curve
         */
        var type: CubismMotionCurveTarget = CubismMotionCurveTarget.MODEL

        /**
         * curve ID
         */
        var id: CubismId

        /**
         * number of segments
         */
        var segmentCount: Int = 0

        /**
         * index to the first segment
         */
        var baseSegmentIndex: Int = 0

        /**
         * time for fade-in[s]
         */
        var fadeInTime: Float = 0f

        /**
         * time for fade-out[s]
         */
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
