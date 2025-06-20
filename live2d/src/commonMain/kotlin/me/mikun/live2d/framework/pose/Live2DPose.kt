package me.mikun.live2d.framework.pose

import kotlinx.serialization.json.Json
import me.mikun.live2d.framework.data.PoseJson
import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.framework.id.Live2DIdManager
import me.mikun.live2d.framework.model.Live2DModel
import kotlin.math.min

class Live2DPose {

    data class PartInfo(
        var partId: Live2DId, // 既是 partId 也是 parameterId
        var linkedParameter: List<PartInfo> = listOf(),
    ) {
        var parameterIndex: Int = -1
        var partIndex: Int = -1

        fun init(model: Live2DModel) {
            parameterIndex = model.getParameterIndex(partId)
            partIndex = model.getPartIndex(partId)

            linkedParameter.forEach {
                it.init(model)
            }
        }
    }

    val groups: List<List<PartInfo>>
    val fadeTimeSeconds: Float

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

    fun init(model: Live2DModel) {
        reset(model)
    }

    private fun reset(model: Live2DModel) {
        groups.forEach {
            it.first().let {
                it.init(model)
                model.setPartOpacity(it.partId, 1.0f)
                model.setParameterValue(it.partId, 1.0f)
            }
            it.drop(1).forEach {
                it.init(model)
                model.setPartOpacity(it.partId, 0.0f)
                model.setParameterValue(it.partId, 0.0f)
            }
        }
    }

    fun update(model: Live2DModel, deltaSeconds: Float) {
        doFade(model, deltaSeconds)
        copyPartOpacities(model)
    }

    private fun doFade(
        model: Live2DModel,
        deltaSeconds: Float,
    ) {
        var opacity = 1.0f

        groups.forEach {
            val current = it.find {
                model.getParameterValue(it.partId) > EPSILON
            }?.also {
                opacity = calculateOpacity(model, it.partId, deltaSeconds)
                model.setPartOpacity(
                    it.partId,
                    opacity
                )
            } ?: groups.first().first()

            it.filter { it.partId != current.partId }.forEach { partInfo ->
                calcNonDisplayedPartsOpacity(
                    model.getPartOpacity(partInfo.partId),
                    opacity
                ).also {
                    model.setPartOpacity(
                        partInfo.partId,
                        it
                    )
                }
            }
        }
    }

    private fun calculateOpacity(
        model: Live2DModel,
        partId: Live2DId,
        deltaSeconds: Float,
    ): Float {
        if (fadeTimeSeconds <= 0.0f) {
            return 1.0f
        }

        return min(
            model.getPartOpacity(
                partId
            ) + deltaSeconds / fadeTimeSeconds,
            1.0f
        )
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

            // Linear equation passing through (0,1),(PHI,PHI)
            a1 = newOpacity * (PHI - 1) / PHI + 1.0f
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

    /**
     * 设置子项透明度与父项相同
     */
    private fun copyPartOpacities(model: Live2DModel) {
        groups.forEach {
            it.forEach {
                for (linkedPart in it.linkedParameter) {
                    model.setPartOpacity(
                        linkedPart.partId,
                        model.getPartOpacity(it.partId)
                    )
                }
            }
        }
    }

    companion object {
        private const val EPSILON = 0.001f
        private const val DEFAULT_FADE_IN_SECONDS = 0.5f
    }
}