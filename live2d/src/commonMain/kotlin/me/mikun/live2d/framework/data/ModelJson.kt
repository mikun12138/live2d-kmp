package me.mikun.live2d.framework.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelJson(
    @SerialName("Version")
    val version: Int,
    @SerialName("FileReferences")
    val fileReferences: FileReferences,
    @SerialName("Groups")
    val groups: List<Group>,
    @SerialName("HitAreas")
    val hitAreas: List<HitArea>,
) {
    @Serializable
    data class FileReferences(
        @SerialName("Moc")
        val moc: String,
        @SerialName("Textures")
        val textures: List<String>,
        @SerialName("Physics")
        val physics: String,
        @SerialName("Pose")
        val pose: String,
        @SerialName("DisplayInfo")
        val displayInfo: String,
        @SerialName("Expressions")
        val expressions: List<Expression> = emptyList(),
        @SerialName("Motions")
        val motionGroups: Map<String, List<MotionGroup.Motion>>,
        @SerialName("UserData")
        val userData: String? = null
    ) {
        @Serializable
        data class Expression(
            @SerialName("Name")
            val name: String,
            @SerialName("File")
            val file: String,
        )

        class MotionGroup {
            @Serializable
            data class Motion(
                @SerialName("File")
                val file: String,
                @SerialName("FadeInTime")
                val fadeInTime: Float = -1.0f,
                @SerialName("FadeOutTime")
                val fadeOutTime: Float = -1.0f,
                @SerialName("Sound")
                val sound: String? = null
            )
        }
    }

    @Serializable
    data class Group(
        @SerialName("Target")
        val target: String,
        @SerialName("Name")
        val name: String,
        @SerialName("Ids")
        val ids: List<String>,
    )

    // TODO:: layout
//    @Serializable
//    data class Layout

    @Serializable
    data class HitArea(
        @SerialName("Id")
        val id: String,
        @SerialName("Name")
        val name: String
    )
}