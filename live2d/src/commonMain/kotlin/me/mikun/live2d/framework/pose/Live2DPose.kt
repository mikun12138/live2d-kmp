package me.mikun.live2d.framework.pose

import me.mikun.live2d.framework.data.PoseJson
import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.framework.id.Live2DIdManager
import me.mikun.live2d.framework.model.Live2DModel
import kotlinx.serialization.json.Json

/**
 * This class deals with parts opacity value and settings.
 */
class Live2DPose {
    constructor(pose3json: ByteArray) {
        Json.Default.decodeFromString<PoseJson>(String(pose3json)).let { json ->

            // Set the fade time.
            fadeTimeSeconds = json.fadeInTime?.let {
                if (it < 0.0f)
                    DEFAULT_FADE_IN_SECONDS
                else
                    it
            } ?: DEFAULT_FADE_IN_SECONDS


            // Parts group
            fun setupPartGroup(partInfo: PoseJson.PartInfo): PartData {

                val partData = PartData(
                    partId = Live2DIdManager.id(partInfo.id)
                )

                for (linkedPart in partInfo.link) {
                    partData.linkedParameter.add(
                        PartData(
                            partId = Live2DIdManager.id(linkedPart)
                        )
                    )
                }
                return partData
            }
            for (idListInfo in json.groups) {
                for (partInfo in idListInfo) {
                    partGroups.add(setupPartGroup(partInfo))
                }
                partGroupCounts.add(idListInfo.size)
            }
        }
    }

    data class PartData(
        var partId: Live2DId? = null,
        var parameterIndex: Int = 0,
        var partIndex: Int = 0,
        var linkedParameter: MutableList<PartData> = mutableListOf(),
    ) {
        fun init(model: Live2DModel) {
            parameterIndex = model.getParameterIndex(partId!!)
            partIndex = model.getPartIndex(partId!!)

            model.setParameterValue(parameterIndex, 1.0f)
        }
    }

    /**
     * Update model's parameters.
     *
     * @param model the target model
     * @param deltaTimeSeconds delta time[s]
     */
    fun updateParameters(model: Live2DModel, deltaTimeSeconds: Float) {
        // If given model is different from previous model, it is required to initialize some parameters.
        var deltaTimeSeconds = deltaTimeSeconds
        if (model != lastModel) {
            reset(model)
        }

        lastModel = model

        // If a negative time is given, 0 value is set.
        if (deltaTimeSeconds < 0.0f) {
            deltaTimeSeconds = 0.0f
        }

        var beginIndex = 0

        for (i in partGroupCounts.indices) {
            val partGroupCount: Int = partGroupCounts.get(i)!!

            doFade(model, deltaTimeSeconds, beginIndex, partGroupCount)
            beginIndex += partGroupCount
        }
        copyPartOpacities(model)
    }

    /**
     * Initialize display.
     *
     * @param model the target model(Parameters that initial opacity is not 0, the opacity is set to 1.)
     */
    private fun reset(model: Live2DModel) {
        var beginIndex = 0

        for (j in partGroupCounts.indices) {
            val groupCount: Int = partGroupCounts.get(j)!!

            for (i in beginIndex..<beginIndex + groupCount) {
                partGroups.get(i).init(model)

                val partsIndex = partGroups.get(i).partIndex
                val paramIndex = partGroups.get(i).parameterIndex

                if (partsIndex < 0) {
                    continue
                }

                if (i == beginIndex) {
                    model.setPartOpacity(partsIndex, 1.0f)
                    model.setParameterValue(paramIndex, 1.0f)
                }

                val value =
                    if (i == beginIndex)
                        1.0f
                    else
                        0.0f
                model.setPartOpacity(partsIndex, value)
                model.setParameterValue(paramIndex, value)

                val link = partGroups.get(i).linkedParameter
                if (link != null) {
                    for (data in link) {
                        data.init(model)
                    }
                }
            }
            beginIndex += groupCount
        }
    }

    /**
     * Parts opacity is copied and set to linked parts.
     *
     * @param model the target model
     */
    private fun copyPartOpacities(model: Live2DModel) {
        for (i in partGroups.indices) {
            val partData = partGroups.get(i)
            if (partData.linkedParameter == null) {
                continue
            }

            val partIndex = partData.partIndex
            val opacity = model.getPartOpacity(partIndex)

            for (j in partData.linkedParameter!!.indices) {
                val linkedPart = partData.linkedParameter!!.get(j)

                val linkedPartIndex = linkedPart.partIndex

                if (linkedPartIndex < 0) {
                    continue
                }

                model.setPartOpacity(linkedPartIndex, opacity)
            }
        }
    }

    /**
     * Fade parts
     *
     * @param model the target model
     * @param deltaTimeSeconds delta time[s]
     * @param beginIndex the head index of parts groups done fading
     * @param partGroupCount the number of parts groups done fading
     */
    private fun doFade(
        model: Live2DModel,
        deltaTimeSeconds: Float,
        beginIndex: Int,
        partGroupCount: Int
    ) {
        var visiblePartIndex = -1
        var newOpacity = 1.0f

        // Get parts displayed now.
        for (i in beginIndex..<beginIndex + partGroupCount) {
            val paramIndex = partGroups.get(i).parameterIndex

            if (model.getParameterValue(paramIndex) > EPSILON) {
                if (visiblePartIndex >= 0) {
                    break
                }

                newOpacity = calculateOpacity(model, i, deltaTimeSeconds)

                visiblePartIndex = i
            }
        }

        if (visiblePartIndex < 0) {
            visiblePartIndex = 0
            newOpacity = 1.0f
        }

        // Set opacity to displayed, and non-displayed parts
        for (i in beginIndex..<beginIndex + partGroupCount) {
            val partsIndex = partGroups.get(i).partIndex

            // Setting of displayed parts
            if (visiblePartIndex == i) {
                model.setPartOpacity(partsIndex, newOpacity)
            } else {
                val opacity = model.getPartOpacity(partsIndex)
                val result = calcNonDisplayedPartsOpacity(opacity, newOpacity)
                model.setPartOpacity(partsIndex, result)
            }
        }
    }

    /**
     * Calculate the new part opacity. This method is used at fading.
     *
     * @param model target model
     * @param index part index
     * @param deltaTime delta time[s]
     * @return new calculated opacity. If fade time is 0.0[s], return 1.0f.
     */
    private fun calculateOpacity(model: Live2DModel, index: Int, deltaTime: Float): Float {
        if (fadeTimeSeconds == 0f) {
            return 1.0f
        }

        val partIndex = partGroups.get(index).partIndex
        var opacity = model.getPartOpacity(partIndex)

        opacity += deltaTime / fadeTimeSeconds

        if (opacity > 1.0f) {
            opacity = 1.0f
        }

        return opacity
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

    private val partGroups: MutableList<PartData> = ArrayList<PartData>()

    private val partGroupCounts: MutableList<Int?> = ArrayList()

    private var fadeTimeSeconds = DEFAULT_FADE_IN_SECONDS

    private var lastModel: Live2DModel? = null

    companion object {
        private const val EPSILON = 0.001f
        private const val DEFAULT_FADE_IN_SECONDS = 0.5f
    }
}