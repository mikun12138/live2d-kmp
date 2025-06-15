package me.mikun.live2d.framework.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PoseJson(
    @SerialName("Type")
    val type: String,
    @SerialName("Groups")
    val groups: List<List<PartInfo>>,
    @SerialName("FadeInTime")
    val fadeInTime: Float = -1.0f
) {
    @Serializable
    data class PartInfo(
        @SerialName("Id")
        val id: String,
        @SerialName("Link")
        val links: List<String>
    )
}