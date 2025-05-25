///*
// * Copyright(c) Live2D Inc. All rights reserved.
// *
// * Use of this source code is governed by the Live2D Open Software license
// * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
// */
//package com.live2d.sdk.cubism.framework.motion
//
//import com.live2d.sdk.cubism.framework.CubismFramework.idManager
//import com.live2d.sdk.cubism.framework.id.CubismId
//import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJson
//
///**
// * Container for motion3.json.
// */
//class CubismMotionJson(buffer: ByteArray?) {
//    /**
//     * Flag type of Bezier curve interpretation method
//     */
//    enum class EvaluationOptionFlag {
//        /**
//         * Regulatory status of Bezier handle.
//         */
//        ARE_BEZIERS_RESTRICTED
//    }
//
//    val motionDuration: Float
//        /**
//         * Get the duration of the motion.
//         *
//         * @return motion duration[s]
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.DURATION.key)
//            .toFloat()
//
//    val isMotionLoop: Boolean
//        /**
//         * Whether the motion loops.
//         *
//         * @return If the motion loops, return true.
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.LOOP.key)
//            .toBoolean()
//
//    /**
//     * Get the state of the interpretation flag of Bezier curve handles in motion.
//     *
//     * @param flagType the flag type specified by EvaluationOptionFlag.
//     * @return If the flag is present, return true.
//     */
//    fun getEvaluationOptionFlag(flagType: EvaluationOptionFlag?): Boolean {
//        if (EvaluationOptionFlag.ARE_BEZIERS_RESTRICTED == flagType) {
//            return json.root.get(JsonKey.META.key).get(JsonKey.ARE_BEZIERS_RESTRICTED.key)
//                .toBoolean()
//        }
//        return false
//    }
//
//    val motionCurveCount: Int
//        /**
//         * Get the number of the motion curves.
//         *
//         * @return number of motion curve
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.CURVE_COUNT.key)
//            .toInt()
//
//    val motionFps: Float
//        /**
//         * Get the framerate of the motion.
//         *
//         * @return framerate[FPS]
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.FPS.key).toFloat()
//
//    val motionTotalSegmentCount: Int
//        /**
//         * Get the total number of motion segments.
//         *
//         * @return total number of motion segments
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.TOTAL_SEGMENT_COUNT.key)
//            .toInt()
//
//    val motionTotalPointCount: Int
//        /**
//         * Get the total number of control points for the curve of the motion.
//         *
//         * @return total number of control points for the curve of the motion
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.TOTAL_POINT_COUNT.key)
//            .toInt()
//
//    /**
//     * Whether a fade-in time is set for the motion.
//     *
//     * @return If motion fade-in time is set, return true.
//     */
//    fun existsMotionFadeInTime(): Boolean {
//        return !json.root.get(JsonKey.META.key).get(JsonKey.FADE_IN_TIME.key).isNull
//    }
//
//    /**
//     * Whether a fade-out time is set for the motion.
//     *
//     * @return If motion fade-out time is set, return true.
//     */
//    fun existsMotionFadeOutTime(): Boolean {
//        return !json.root.get(JsonKey.META.key).get(JsonKey.FADE_OUT_TIME.key).isNull
//    }
//
//    val motionFadeInTime: Float
//        /**
//         * Get the motion fade-in duration.
//         *
//         * @return fade-in duration[s]
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.FADE_IN_TIME.key)
//            .toFloat()
//
//    val motionFadeOutTime: Float
//        /**
//         * Get the motion fade-out duration.
//         *
//         * @return fade-out duration[s]
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.FADE_OUT_TIME.key)
//            .toFloat()
//
//    /**
//     * Get the type of motion curve.
//     *
//     * @param curveIndex index of curve
//     * @return type of motion curve
//     */
//    fun getMotionCurveTarget(curveIndex: Int): String {
//        return json.root.get(JsonKey.CURVES.key).get(curveIndex).get(JsonKey.TARGET.key).string
//    }
//
//    /**
//     * Get ID of motion curve.
//     *
//     * @param curveIndex index of curve
//     * @return curve ID
//     */
//    fun getMotionCurveId(curveIndex: Int): CubismId {
//        return idManager.getId(
//            json.root.get(JsonKey.CURVES.key).get(curveIndex).get(JsonKey.ID.key).string
//        )
//    }
//
//    /**
//     * Whether the fade-in duration is set for the motion's curve.
//     *
//     * @param curveIndex index of curve
//     * @return If fade-in duration is set, return true.
//     */
//    fun existsMotionCurveFadeInTime(curveIndex: Int): Boolean {
//        return !json.root.get(JsonKey.CURVES.key).get(curveIndex)
//            .get(JsonKey.FADE_IN_TIME.key).isNull
//    }
//
//    /**
//     * Whether the fade-out duration is set for the motion's curve.
//     *
//     * @param curveIndex index of curve
//     * @return If fade-out duration is set, return true.
//     */
//    fun existsMotionCurveFadeOutTime(curveIndex: Int): Boolean {
//        return !json.root.get(JsonKey.CURVES.key).get(curveIndex)
//            .get(JsonKey.FADE_OUT_TIME.key).isNull
//    }
//
//    /**
//     * Get the fade-in duration of the motion curve.
//     *
//     * @param curveIndex index of curve
//     * @return fade-in duration[s]
//     */
//    fun getMotionCurveFadeInTime(curveIndex: Int): Float {
//        return json.root.get(JsonKey.CURVES.key).get(curveIndex).get(JsonKey.FADE_IN_TIME.key)
//            .toFloat()
//    }
//
//    /**
//     * Get the fade-out duration of the motion curve.
//     *
//     * @param curveIndex index of curve
//     * @return fade-out duration[s]
//     */
//    fun getMotionCurveFadeOutTime(curveIndex: Int): Float {
//        return json.root.get(JsonKey.CURVES.key).get(curveIndex).get(JsonKey.FADE_OUT_TIME.key)
//            .toFloat()
//    }
//
//    /**
//     * Get the number of segments in the curve of the motion.
//     *
//     * @param curveIndex index of curve
//     * @return number of segments in the curve of the motion
//     */
//    fun getMotionCurveSegmentCount(curveIndex: Int): Int {
//        return json.root.get(JsonKey.CURVES.key).get(curveIndex).get(JsonKey.SEGMENTS.key).list.size
//    }
//
//    /**
//     * Get the value of a segment of a motion curve.
//     *
//     * @param curveIndex index of curve
//     * @param segmentIndex index of segment
//     * @return value of segment
//     */
//    fun getMotionCurveSegment(curveIndex: Int, segmentIndex: Int): Float {
//        return json.root.get(JsonKey.CURVES.key).get(curveIndex).get(JsonKey.SEGMENTS.key)
//            .get(segmentIndex).toFloat()
//    }
//
//    val eventCount: Int
//        /**
//         * Get the number of events.
//         *
//         * @return number of events
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.USER_DATA_COUNT.key)
//            .toInt()
//
//    val totalEventValueSize: Int
//        /**
//         * Get the total number of characters in the event.
//         *
//         * @return total number of characters in the event
//         */
//        get() = json.root.get(JsonKey.META.key)
//            .get(JsonKey.TOTAL_USER_DATA_SIZE.key)
//            .toInt()
//
//    /**
//     * Get the event duration.
//     *
//     * @param userDataIndex index of events
//     * @return event duration[s]
//     */
//    fun getEventTime(userDataIndex: Int): Float {
//        return json.root.get(JsonKey.USER_DATA.key).get(userDataIndex).get(JsonKey.TIME.key)
//            .toFloat()
//    }
//
//    /**
//     * Get the event string.
//     *
//     * @param userDataIndex index of event
//     * @return event strings
//     */
//    fun getEventValue(userDataIndex: Int): String {
//        return json.root.get(JsonKey.USER_DATA.key).get(userDataIndex).get(JsonKey.VALUE.key).string
//    }
//
//    private enum class JsonKey(key: String) {
//        META("Meta"),
//        DURATION("Duration"),
//        LOOP("Loop"),
//        ARE_BEZIERS_RESTRICTED("AreBeziersRestricted"),
//        CURVE_COUNT("CurveCount"),
//        FPS("Fps"),
//        TOTAL_SEGMENT_COUNT("TotalSegmentCount"),
//        TOTAL_POINT_COUNT("TotalPointCount"),
//        CURVES("Curves"),
//        TARGET("Target"),
//        ID("Id"),
//        FADE_IN_TIME("FadeInTime"),
//        FADE_OUT_TIME("FadeOutTime"),
//        SEGMENTS("Segments"),
//        USER_DATA("UserData"),
//        USER_DATA_COUNT("UserDataCount"),
//        TOTAL_USER_DATA_SIZE("TotalUserDataSize"),
//        TIME("Time"),
//        VALUE("Value");
//
//        private val key: String?
//
//        init {
//            this.key = key
//        }
//    }
//
//    /**
//     * motion3.json data
//     */
//    private val json: CubismJson
//
//    init {
//        val json: CubismJson
//        json = CubismJson.create(buffer!!)
//
//        this.json = json
//    }
//}
