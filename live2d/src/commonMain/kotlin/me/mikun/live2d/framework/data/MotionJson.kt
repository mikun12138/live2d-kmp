package me.mikun.live2d.framework.data


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
    @SerialName("UserData")
    val userData: List<UserData> = emptyList()
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
        val fadeInTime: Float = -1.0f,
        @SerialName("FadeOutTime")
        val fadeOutTime: Float = -1.0f,
    )
    @Serializable
    data class Curve(
        @SerialName("Target")
        val target: String, // type
        @SerialName("Id")
        val id: String,
        @SerialName("Segments")
        val segments: List<Float>,
        @SerialName("FadeInTime")
        val fadeInTime: Float = -1.0f,
        @SerialName("FadeOutTime")
        val fadeOutTime: Float = -1.0f,
    )

    @Serializable
    data class UserData(
        @SerialName("Time")
        val time: Float,
        @SerialName("Value")
        val value: String,
    )
}