package com.live2d.sdk.cubism.framework.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpressionJson(
    @SerialName("Type")
    val type: String,
    @SerialName("Parameters")
    val parameters: List<Parameter>,
    @SerialName("FadeInTime")
    val fadeInTime: Float?,
    @SerialName("FadeOutTime")
    val fadeOutTime: Float?,
) {
    @Serializable
    data class Parameter(
        @SerialName("Id")
        val id: String,
        @SerialName("Value")
        val value: Float,
        @SerialName("Blend")
        val blend: String,
    )
}