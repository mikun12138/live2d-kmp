package com.live2d.sdk.cubism.framework.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhysicsJson(
    @SerialName("Meta")
    val meta: Meta,
    @SerialName("PhysicsSettings")
    val physicsSettings: List<PhysicsSetting>,
    @SerialName("Version")
    val version: Int
) {
    @Serializable
    data class Meta(
        @SerialName("EffectiveForces")
        val effectiveForces: EffectiveForces,
        @SerialName("PhysicsDictionary")
        val physicsDictionary: List<PhysicsDictionary>,
        @SerialName("PhysicsSettingCount")
        val physicsSettingCount: Int,
        @SerialName("TotalInputCount")
        val totalInputCount: Int,
        @SerialName("TotalOutputCount")
        val totalOutputCount: Int,
        @SerialName("VertexCount")
        val vertexCount: Int,
        @SerialName("Fps")
        val fps: Int,
    ) {
        @Serializable
        data class EffectiveForces(
            @SerialName("Gravity")
            val gravity: Gravity,
            @SerialName("Wind")
            val wind: Wind
        ) {
            @Serializable
            data class Gravity(
                @SerialName("X")
                val x: Int,
                @SerialName("Y")
                val y: Int
            )

            @Serializable
            data class Wind(
                @SerialName("X")
                val x: Int,
                @SerialName("Y")
                val y: Int
            )
        }

        @Serializable
        data class PhysicsDictionary(
            @SerialName("Id")
            val id: String,
            @SerialName("Name")
            val name: String
        )
    }

    @Serializable
    data class PhysicsSetting(
        @SerialName("Id")
        val id: String,
        @SerialName("Input")
        val input: List<Input>,
        @SerialName("Normalization")
        val normalization: Normalization,
        @SerialName("Output")
        val output: List<Output>,
        @SerialName("Vertices")
        val vertices: List<Vertice>
    ) {
        @Serializable
        data class Input(
            @SerialName("Reflect")
            val reflect: Boolean,
            @SerialName("Source")
            val source: Source,
            @SerialName("Type")
            val type: String,
            @SerialName("Weight")
            val weight: Int
        ) {
            @Serializable
            data class Source(
                @SerialName("Id")
                val id: String,
                @SerialName("Target")
                val target: String
            )
        }

        @Serializable
        data class Normalization(
            @SerialName("Angle")
            val angle: Angle,
            @SerialName("Position")
            val position: Position
        ) {
            @Serializable
            data class Angle(
                @SerialName("Default")
                val default: Int,
                @SerialName("Maximum")
                val maximum: Int,
                @SerialName("Minimum")
                val minimum: Int
            )

            @Serializable
            data class Position(
                @SerialName("Default")
                val default: Int,
                @SerialName("Maximum")
                val maximum: Int,
                @SerialName("Minimum")
                val minimum: Int
            )
        }

        @Serializable
        data class Output(
            @SerialName("Destination")
            val destination: Destination,
            @SerialName("Reflect")
            val reflect: Boolean,
            @SerialName("Scale")
            val scale: Float,
            @SerialName("Type")
            val type: String,
            @SerialName("VertexIndex")
            val vertexIndex: Int,
            @SerialName("Weight")
            val weight: Float
        ) {
            @Serializable
            data class Destination(
                @SerialName("Id")
                val id: String,
                @SerialName("Target")
                val target: String
            )
        }

        @Serializable
        data class Vertice(
            @SerialName("Acceleration")
            val acceleration: Float,
            @SerialName("Delay")
            val delay: Float,
            @SerialName("Mobility")
            val mobility: Float,
            @SerialName("Position")
            val position: Position,
            @SerialName("Radius")
            val radius: Float
        ) {
            @Serializable
            data class Position(
                @SerialName("X")
                val x: Float,
                @SerialName("Y")
                val y: Float
            )
        }
    }
}