package me.mikun.live2d.framework.pose

import me.mikun.live2d.framework.data.PoseJson
import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.framework.id.Live2DIdManager
import me.mikun.live2d.framework.model.Live2DModel
import kotlinx.serialization.json.Json
import kotlin.math.min

class Live2DPose {
    constructor(pose3json: ByteArray) {
        Json.decodeFromString<PoseJson>(String(pose3json)).let { json ->
            fadeTimeSeconds = json.fadeInTime.let {
                if (it < 0.0f)
                    DEFAULT_FADE_IN_SECONDS
                else
                    it
            }

            groups = json.groups.map {
                it.map {
                    PartInfo(
                        partId = Live2DIdManager.id(it.id),
                        linkedParameter = it.link.map { PartInfo(Live2DIdManager.id(it)) }
                    )
                }
            }
        }
    }

    val groups: List<List<PartInfo>>

    data class PartInfo(
        var partId: Live2DId,
        var linkedParameter: List<PartInfo> = listOf(),
    ) {
        var parameterIndex: Int = -1
        var partIndex: Int = -1

        fun init(model: Live2DModel) {
            parameterIndex = model.getParameterIndex(partId)
            partIndex = model.getPartIndex(partId)

            model.setParameterValue(parameterIndex, 1.0f)

            linkedParameter.forEach {
                it.init(model)
            }
        }

    }


    data class PartData(
        var partId: Live2DId,
        var parameterIndex: Int = 0,
        var partIndex: Int = 0,
        var linkedParameter: MutableList<PartData> = mutableListOf(),
    ) {
        fun init(model: Live2DModel) {
            parameterIndex = model.getParameterIndex(partId)
            partIndex = model.getPartIndex(partId)

            model.setParameterValue(parameterIndex, 1.0f)

            linkedParameter.forEach {
                it.init(model)
            }
        }
    }

    fun updateParameters1(model: Live2DModel, deltaSeconds: Float) {
        // If given model is different from previous model, it is required to initialize some parameters.
        if (model != lastModel) {
            reset1(model)
        }
        lastModel = model

        doFade(model, deltaSeconds)

        copyPartOpacities(model)

    }

    private fun reset1(model: Live2DModel) {
        groups.forEach {
            it.first().let {
                it.init(model)
                model.setPartOpacity(it.partIndex, 1.0f)
                model.setParameterValue(it.parameterIndex, 1.0f)
            }
            it.drop(1).forEach {
                it.init(model)
                model.setPartOpacity(it.partIndex, 0.0f)
                model.setParameterValue(it.parameterIndex, 0.0f)
            }
        }
    }

    private fun doFade(
        model: Live2DModel,
        deltaSeconds: Float,
    ) {
        var opacity = 1.0f

        groups.forEach {
            val current = it.find {
                model.getParameterValue(it.parameterIndex) > EPSILON
            }?.also {
                opacity = calculateOpacity(model, it.partIndex, deltaSeconds)
                model.setPartOpacity(
                    it.partIndex,
                    opacity
                )
            } ?: groups.first().first()

            it.filter { it.partIndex != current.partIndex }.forEach { partInfo ->
                calcNonDisplayedPartsOpacity(
                    model.getPartOpacity(partInfo.partIndex),
                    opacity
                ).also {
                    model.setPartOpacity(
                        partInfo.partIndex,
                        it
                    )
                }
            }
        }
    }

    private fun calculateOpacity(
        model: Live2DModel,
        partIndex: Int,
        deltaSeconds: Float,
    ): Float {
        if (fadeTimeSeconds <= 0.0f) {
            return 1.0f
        }

        return min(
            model.getPartOpacity(
                partIndex
            ) + deltaSeconds / fadeTimeSeconds,
            1.0f
        )
    }

    /**
     * 设置子项透明度与父项相同
     */
    private fun copyPartOpacities(model: Live2DModel) {
        groups.forEach {
            it.forEach {
                for (linkedPart in it.linkedParameter) {
                    model.setPartOpacity(
                        linkedPart.partIndex,
                        model.getPartOpacity(it.partIndex)
                    )
                }
            }
        }
    }

    /**
     * Calculate opacity of non-displayed parts.
     *
     * @param currentOpacity current part opacity
     * @param newOpacity calculated opacity
     * @return opacity for non-displayed part
     */
    private fun calcNonDisplayedPartsOpacity(currentOpacity: Float, newOpacity: Float): Float {
        var currentOpacity = currentOpacity
        val PHI = 0.5f
        val BACK_OPACITY_THRESHOLD = 0.15f

        var a1: Float // opacity to be calculated
        if (newOpacity < PHI) {
            // Linear equation passing through (0,1),(PHI,PHI)
            a1 = newOpacity * (PHI - 1.0f) / (PHI + 1.0f)
        } else {
            a1 = (1 - newOpacity) * PHI / (1.0f - PHI)
        }

        val backOpacity = (1.0f - a1) * (1.0f - newOpacity)

        if (backOpacity > BACK_OPACITY_THRESHOLD) {
            a1 = 1.0f - BACK_OPACITY_THRESHOLD / (1.0f - newOpacity)
        }

        // The opacity is raised if the opacity is larger(more thicken) than the calculated opacity.
        if (currentOpacity > a1) {
            currentOpacity = a1
        }

        return currentOpacity
    }

    val fadeTimeSeconds: Float

    private var lastModel: Live2DModel? = null

    companion object {
        private const val EPSILON = 0.001f
        private const val DEFAULT_FADE_IN_SECONDS = 0.5f
    }
}