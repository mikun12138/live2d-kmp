package com.live2d.sdk.cubism.framework.utils.json


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MotionJson(
    @SerialName("Version")
    val version: Int,
    @SerialName("Meta")
    val meta: Meta,
    @SerialName("Curves")
    val curves: List<Curve>,
) {

    @Serializable
    data class Meta(
        @SerialName("Duration")
        val duration: Float,
        @SerialName("Fps")
        val fps: Float,
        @SerialName("Loop")
        val loop: Boolean,
        @SerialName("AreBeziersRestricted")
        val areBeziersRestricted: Boolean,
        @SerialName("CurveCount")
        val curveCount: Int,
        @SerialName("TotalSegmentCount")
        val totalSegmentCount: Int,
        @SerialName("TotalPointCount")
        val totalPointCount: Int,
        @SerialName("UserDataCount")
        val userDataCount: Int,
        @SerialName("TotalUserDataSize")
        val totalUserDataSize: Int,
        @SerialName("FadeInTime")
        val fadeInTime: Float?,
        @SerialName("FadeOutTime")
        val fadeOutTime: Float?,
    )
    @Serializable
    data class Curve(
        @SerialName("Target")
        val target: String,
        @SerialName("Id")
        val id: String,
        @SerialName("Segments")
        val segments: List<Float>,
        @SerialName("FadeInTime")
        val fadeInTime: Float?,
        @SerialName("FadeOutTime")
        val fadeOutTime: Float?,
    )
}