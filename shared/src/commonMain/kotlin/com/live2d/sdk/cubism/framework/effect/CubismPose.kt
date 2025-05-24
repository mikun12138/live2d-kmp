/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.effect

import com.live2d.sdk.cubism.framework.CubismFramework.idManager
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.utils.jsonparser.ACubismJsonValue
import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJson
import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJsonString

/**
 * This class deals with parts opacity value and settings.
 */
class CubismPose {
    /**
     * Manage data related parts.
     */
    class PartData {
        /**
         * Default constructor
         */
        constructor()

        /**
         * Copy constructor
         *
         * @param partData original part data
         */
        constructor(partData: PartData) {
            partId = partData.partId
            parameterIndex = partData.parameterIndex
            partIndex = partData.partIndex

            linkedParameter!!.addAll(partData.linkedParameter!!)
        }

        fun initialize(model: CubismModel) {
            parameterIndex = model.getParameterIndex(partId!!)
            partIndex = model.getPartIndex(partId!!)

            model.setParameterValue(parameterIndex, 1)
        }

        /**
         * Part ID
         */
        var partId: CubismId? = null

        /**
         * Parameter index
         */
        var parameterIndex: Int = 0

        /**
         * Part index
         */
        var partIndex: Int = 0

        /**
         * Linked parameters list
         */
        var linkedParameter: MutableList<PartData>? = ArrayList<PartData>()
    }

    /**
     * Update model's parameters.
     *
     * @param model the target model
     * @param deltaTimeSeconds delta time[s]
     */
    fun updateParameters(model: CubismModel, deltaTimeSeconds: Float) {
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
    private fun reset(model: CubismModel) {
        var beginIndex = 0

        for (j in partGroupCounts.indices) {
            val groupCount: Int = partGroupCounts.get(j)!!

            for (i in beginIndex..<beginIndex + groupCount) {
                partGroups.get(i).initialize(model)

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
                        data.initialize(model)
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
    private fun copyPartOpacities(model: CubismModel) {
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
        model: CubismModel,
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
    private fun calculateOpacity(model: CubismModel, index: Int, deltaTime: Float): Float {
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

    // Tags of Pose3.json
    private enum class JsonTag(tag: String) {
        FADE_IN("FadeInTime"),
        LINK("Link"),
        GROUPS("Groups"),
        ID("Id");

        private val tag: String?

        init {
            this.tag = tag
        }
    }

    /**
     * Parts group
     */
    private val partGroups: MutableList<PartData> = ArrayList<PartData>()

    /**
     * Each parts group number
     */
    private val partGroupCounts: MutableList<Int?> = ArrayList<Int?>()

    /**
     * Fade time[s]
     */
    private var fadeTimeSeconds = DEFAULT_FADE_IN_SECONDS

    /**
     * Previous operated model
     */
    private var lastModel: CubismModel? = null

    companion object {
        /**
         * Create a CubismPose instance
         *
         * @param pose3json the byte data of pose3.json
         * @return the created instance
         */
        fun create(pose3json: ByteArray?): CubismPose {
            val pose = CubismPose()
            val json: CubismJson
            json = CubismJson.create(pose3json!!)

            val root = json.root
            val rootMap: MutableMap<CubismJsonString?, ACubismJsonValue?>? = root.map

            // Set the fade time.
            if (!root.get(JsonTag.FADE_IN.tag).isNull) {
                pose.fadeTimeSeconds =
                    root.get(JsonTag.FADE_IN.tag).toFloat(DEFAULT_FADE_IN_SECONDS)

                if (pose.fadeTimeSeconds < 0.0f) {
                    pose.fadeTimeSeconds = DEFAULT_FADE_IN_SECONDS
                }
            }

            // Parts group
            val poseListInfo = root.get(JsonTag.GROUPS.tag)
            val poseCount = poseListInfo.size()

            for (poseIndex in 0..<poseCount) {
                val idListInfo = poseListInfo.get(poseIndex)
                val idCount = idListInfo.size()
                var groupCount = 0

                for (groupIndex in 0..<idCount) {
                    val partInfo = idListInfo.get(groupIndex)
                    val partData = setupPartGroup(partInfo)
                    pose.partGroups.add(partData)
                    groupCount++
                }
                pose.partGroupCounts.add(groupCount)
            }

            return pose
        }

        private fun setupPartGroup(partInfo: ACubismJsonValue): PartData {
            val parameterId = idManager!!.id(partInfo.get(JsonTag.ID.tag).string!!)

            val partData = PartData()
            partData.partId = idManager!!.id(parameterId)

            val link = partInfo.get(JsonTag.LINK.tag)
            if (link != null) {
                setupLinkedPart(partData, link)
            }
            return partData
        }

        /**
         * Setup linked parts.
         *
         * @param partData part data to be done setting
         * @param linkedListInfo linked parts list information
         */
        private fun setupLinkedPart(partData: PartData, linkedListInfo: ACubismJsonValue) {
            val linkCount = linkedListInfo.size()

            for (index in 0..<linkCount) {
                val linkedPartId = idManager!!.id(linkedListInfo.get(index).string!!)

                val linkedPart = PartData()
                linkedPart.partId = linkedPartId

                partData.linkedParameter!!.add(linkedPart)
            }
        }

        /**
         * Epsilon value
         */
        private const val EPSILON = 0.001f

        /**
         * Default fade-in duration[s]
         */
        private const val DEFAULT_FADE_IN_SECONDS = 0.5f
    }
}
