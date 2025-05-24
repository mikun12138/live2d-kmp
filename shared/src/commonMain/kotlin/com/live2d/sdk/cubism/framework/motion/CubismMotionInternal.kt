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
internal class CubismMotionInternal {
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
        var id: CubismId? = null

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

    /**
     * Motion data
     */
    class CubismMotionData {
        /**
         * motion duration
         */
        var duration: Float = 0f

        /**
         * Whether the motion loops
         */
        var isLooped: Boolean = false

        /**
         * number of curves
         */
        var curveCount: Int = 0

        /**
         * number of UserData
         */
        var eventCount: Int = 0

        /**
         * framerate per second
         */
        var fps: Float = 0f

        /**
         * list of curves
         */
        var curves: MutableList<CubismMotionCurve> = ArrayList<CubismMotionCurve?>()

        /**
         * list of segments
         */
        var segments: MutableList<CubismMotionSegment> = ArrayList<CubismMotionSegment?>()

        /**
         * list of points
         */
        var points: MutableList<CubismMotionPoint> = ArrayList<CubismMotionPoint?>()

        /**
         * list of events
         */
        var events: MutableList<CubismMotionEvent> = ArrayList<CubismMotionEvent?>()
    }

    /**
     * For strategy pattern.
     */
    interface CsmMotionSegmentEvaluationFunction {
        fun evaluate(points: MutableList<CubismMotionPoint?>?, time: Float): Float
    }
}
