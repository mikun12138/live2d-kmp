package com.live2d.sdk.cubism.framework.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PoseJson(
    @SerialName("Groups")
    val groups: List<List<PartInfo>>,
    @SerialName("Type")
    val type: String,
    @SerialName("FadeInTime")
    val fadeInTime: Float?
) {
    @Serializable
    data class PartInfo(
        @SerialName("Id")
        val id: String,
        @SerialName("Link")
        val link: List<String>
    )
}