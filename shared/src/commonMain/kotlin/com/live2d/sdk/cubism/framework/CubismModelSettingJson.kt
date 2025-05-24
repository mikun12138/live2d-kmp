/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework

import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.utils.jsonparser.ACubismJsonValue
import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJson

/**
 * This class deals with model3.json data.
 */
class CubismModelSettingJson(buffer: ByteArray?) : ICubismModelSetting {

    val modelFileName: String?
        get() {
            if (!existsModelFile()) {
                return ""
            }

            return jsonFrequencyValue.get(FrequentNode.MOC.id).string
        }

    val textureCount: Int
        get() {
            if (!existsTextureFiles()) {
                return 0
            }

            return json.root
                .get(JsonKey.FILE_REFERENCES.key)
                .get(JsonKey.TEXTURES.key)
                .size()
        }

    val textureDirectory: String?
        get() {
            if (!existsTextureFiles()) {
                return ""
            }

            val rowString: String =
                jsonFrequencyValue.get(FrequentNode.TEXTURES.id)
                    .get(0).getString()
            return rowString.split("/")[0]
        }

    
    fun getTextureFileName(index: Int): String {
        return jsonFrequencyValue!!.get(FrequentNode.TEXTURES.id)
            .get(index).getString()
    }

    val hitAreasCount: Int
        get() {
            if (!existsHitAreas()) {
                return 0
            }
            return jsonFrequencyValue!!.get(FrequentNode.HIT_AREAS.id)
                .size()
        }

    
    fun getHitAreaId(index: Int): CubismId {
        return CubismFramework.getIdManager().getId(
            jsonFrequencyValue!!.get(FrequentNode.HIT_AREAS.id)
                .get(index).get(JsonKey.ID.key).getString()
        )
    }

    
    fun getHitAreaName(index: Int): String {
        return jsonFrequencyValue!!.get(FrequentNode.HIT_AREAS.id)
            .get(index).get(JsonKey.NAME.key).getString()
    }

    val physicsFileName: String?
        get() {
            if (!existsPhysicsFile()) {
                return ""
            }
            return jsonFrequencyValue!!.get(FrequentNode.PHYSICS.id)
                .getString()
        }

    val poseFileName: String?
        get() {
            if (!existsPoseFile()) {
                return ""
            }

            return jsonFrequencyValue!!.get(FrequentNode.POSE.id)
                .getString()
        }

    val displayInfoFileName: String?
        get() {
            if (!existsDisplayInfoFile()) {
                return ""
            }
            return jsonFrequencyValue!!.get(FrequentNode.DISPLAY_INFO.id)
                .getString()
        }

    val expressionCount: Int
        get() {
            if (!existsExpressionFile()) {
                return 0
            }
            return jsonFrequencyValue!!.get(FrequentNode.EXPRESSIONS.id)
                .size()
        }

    
    fun getExpressionName(index: Int): String {
        return jsonFrequencyValue!!.get(FrequentNode.EXPRESSIONS.id)
            .get(index).get(JsonKey.NAME.key).getString()
    }

    
    fun getExpressionFileName(index: Int): String {
        return jsonFrequencyValue!!.get(FrequentNode.EXPRESSIONS.id)
            .get(index).get(JsonKey.FILEPATH.key).getString()
    }

    val motionGroupCount: Int
        get() {
            if (!existsMotionGroups()) {
                return 0
            }
            return jsonFrequencyValue!!.get(FrequentNode.MOTIONS.id)
                .size()
        }

    
    fun getMotionGroupName(index: Int): String? {
        if (!existsMotionGroups()) {
            return null
        }
        return jsonFrequencyValue!!.get(FrequentNode.MOTIONS.id)
            .getKeys().get(index).getString()
    }

    
    fun getMotionCount(groupName: String?): Int {
        if (!existsMotionGroupName(groupName)) {
            return 0
        }
        return jsonFrequencyValue!!.get(FrequentNode.MOTIONS.id)
            .get(groupName).size()
    }

    
    fun getMotionFileName(groupName: String?, index: Int): String? {
        if (!existsMotionGroupName(groupName)) {
            return ""
        }

        return jsonFrequencyValue!!.get(FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.FILEPATH.key).getString()
    }

    
    fun getMotionSoundFileName(groupName: String?, index: Int): String? {
        if (!existsMotionSoundFile(groupName, index)) {
            return ""
        }

        return jsonFrequencyValue!!.get(FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.SOUND_PATH.key).getString()
    }

    
    fun getMotionFadeInTimeValue(groupName: String?, index: Int): Float {
        if (!existsMotionFadeIn(groupName, index)) {
            return -1.0f
        }

        return jsonFrequencyValue!!.get(FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.FADE_IN_TIME.key).toFloat()
    }

    
    fun getMotionFadeOutTimeValue(groupName: String?, index: Int): Float {
        if (!existsMotionFadeOut(groupName, index)) {
            return -1.0f
        }

        return jsonFrequencyValue!!.get(FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.FADE_OUT_TIME.key).toFloat()
    }

    val userDataFile: String?
        get() {
            if (!existsUserDataFile()) {
                return ""
            }
            return json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.USER_DATA.key)
                .getString()
        }

    
    fun getLayoutMap(outLayoutMap: Map<String?, Float?>): Boolean {
        val map: Map<CubismJsonString?, ACubismJsonValue?>? =
            json.getRoot().get(JsonKey.LAYOUT.key).getMap()

        if (map == null) {
            return false
        }

        var result = false
        for (entry in map.entrySet()) {
            outLayoutMap.put(entry.getKey().getString(), entry.getValue().toFloat())
            result = true
        }

        return result
    }

    val eyeBlinkParameterCount: Int
        get() {
            if (!existsEyeBlinkParameters()) {
                return 0
            }

            var eyeBlinkParameterCount = 0
            for (i in 0..<jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                .size()) {
                val refI: ACubismJsonValue =
                    jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                        .get(i)

                if (refI.isNull() || refI.isError()) {
                    continue
                }

                if (refI.get(JsonKey.NAME.key).getString().equals(JsonKey.EYE_BLINK.key)) {
                    eyeBlinkParameterCount = refI.get(JsonKey.IDS.key).getList().size()
                    break
                }
            }

            return eyeBlinkParameterCount
        }

    
    fun getEyeBlinkParameterId(index: Int): CubismId? {
        if (!existsEyeBlinkParameters()) {
            return null
        }

        repeat(jsonFrequencyValue[FrequentNode.GROUPS.id].size()) {

            val refI: ACubismJsonValue =
                jsonFrequencyValue[FrequentNode.GROUPS.id]
                    .get(it)

            if (refI.isNull || refI.isError) {
                return@repeat
            }

            if (refI.get(JsonKey.NAME.key).string == JsonKey.EYE_BLINK.key) {
                return CubismFramework.idManager
                    .id(refI.get(JsonKey.IDS.key).get(index).string)
            }
        }
        return null
    }


    val lipSyncParameterCount: Int
        get() {
            if (!existsLipSyncParameters()) {
                return 0
            }


            var lipSyncParameterCount = 0
            for (i in 0..<jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                .size()) {
                val refI: ACubismJsonValue =
                    jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                        .get(i)

                if (refI.isNull() || refI.isError()) {
                    continue
                }

                if (refI.get(JsonKey.NAME.key).getString().equals(JsonKey.LIP_SYNC.key)) {
                    lipSyncParameterCount = refI.get(JsonKey.IDS.key).getList().size()
                    break
                }
            }
            return lipSyncParameterCount
        }

    
    fun getLipSyncParameterId(index: Int): CubismId? {
        if (!existsLipSyncParameters()) {
            return null
        }

        for (i in 0..<jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
            .size()) {
            val refI: ACubismJsonValue =
                jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                    .get(i)

            if (refI.isNull() || refI.isError()) {
                continue
            }

            if (refI.get(JsonKey.NAME.key).getString().equals(JsonKey.LIP_SYNC.key)) {
                return CubismFramework.getIdManager()
                    .getId(refI.get(JsonKey.IDS.key).get(index).getString())
            }
        }
        return null
    }

    /**
     * Enum class for frequent nodes.
     */
    private enum class FrequentNode(
        val id: Int
    ) {
        GROUPS(0),
        MOC(1),
        MOTIONS(2),
        DISPLAY_INFO(3),
        EXPRESSIONS(4),
        TEXTURES(5),
        PHYSICS(6),
        POSE(7),
        HIT_AREAS(8)
    }

    private enum class JsonKey(
        val key: String
    ) {
        VERSION("Version"),
        FILE_REFERENCES("FileReferences"),
        GROUPS("Groups"),
        LAYOUT("Layout"),
        HIT_AREAS("HitAreas"),

        MOC("Moc"),
        TEXTURES("Textures"),
        PHYSICS("Physics"),
        DISPLAY_INFO("DisplayInfo"),
        POSE("Pose"),
        EXPRESSIONS("Expressions"),
        MOTIONS("Motions"),

        USER_DATA("UserData"),
        NAME("Name"),
        FILEPATH("File"),
        ID("Id"),
        IDS("Ids"),
        TARGET("Target"),

        // Motions
        IDLE("Idle"),
        TAP_BODY("TapBody"),
        PINCH_IN("PinchIn"),
        PINCH_OUT("PinchOut"),
        SHAKE("Shake"),
        FLICK_HEAD("FlickHead"),
        PARAMETER("Parameter"),

        SOUND_PATH("Sound"),
        FADE_IN_TIME("FadeInTime"),
        FADE_OUT_TIME("FadeOutTime"),

        // Layout
        CENTER_X("CenterX"),
        CENTER_Y("CenterY"),
        X("X"),
        Y("Y"),
        WIDTH("Width"),
        HEIGHT("Height"),

        LIP_SYNC("LipSync"),
        EYE_BLINK("EyeBlink"),

        INIT_PARAMETER("init_param"),
        INIT_PARTS_VISIBLE("init_parts_visible"),
        VAL("val");

    }


    // キーが存在するかどうかのチェック
    private fun existsModelFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.MOC.id)
        return !node.isNull && !node.isError
    }

    private fun existsTextureFiles(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.TEXTURES.id)
        return !node.isNull && !node.isError
    }

    private fun existsHitAreas(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.HIT_AREAS.id)
        return !node.isNull && !node.isError
    }

    private fun existsPhysicsFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.PHYSICS.id)
        return !node.isNull && !node.isError
    }

    private fun existsPoseFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.POSE.id)
        return !node.isNull && !node.isError
    }

    private fun existsDisplayInfoFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.DISPLAY_INFO.id)
        return !node.isNull && !node.isError
    }

    private fun existsExpressionFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.EXPRESSIONS.id)
        return !node.isNull && !node.isError
    }

    private fun existsMotionGroups(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.MOTIONS.id)
        return !node.isNull && !node.isError
    }

    private fun existsMotionGroupName(groupName: String?): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.MOTIONS.id)
                .get(groupName)
        return !node.isNull && !node.isError
    }

    private fun existsMotionSoundFile(groupName: String?, index: Int): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.MOTIONS.id)
                .get(groupName).get(index).get(JsonKey.SOUND_PATH.key)
        return !node.isNull && !node.isError
    }

    private fun existsMotionFadeIn(groupName: String?, index: Int): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.MOTIONS.id)
                .get(groupName).get(index).get(JsonKey.FADE_IN_TIME.key)
        return !node.isNull && !node.isError
    }

    private fun existsMotionFadeOut(groupName: String?, index: Int): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue.get(FrequentNode.MOTIONS.id)
                .get(groupName).get(index).get(JsonKey.FADE_OUT_TIME.key)
        return !node.isNull && !node.isError
    }

    private fun existsUserDataFile(): Boolean {
        return !json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.USER_DATA.key).isNull()
    }

    private fun existsEyeBlinkParameters(): Boolean {
        if (jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                .isNull() || jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                .isError()
        ) {
            return false
        }

        for (i in 0..<jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
            .size()) {
            if (jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                    .get(i).get(JsonKey.NAME.key).getString().equals(JsonKey.EYE_BLINK.key)
            ) {
                return true
            }
        }

        return false
    }

    private fun existsLipSyncParameters(): Boolean {
        if (jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                .isNull() || jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                .isError()
        ) {
            return false
        }

        for (i in 0..<jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
            .size()) {
            if (jsonFrequencyValue!!.get(FrequentNode.GROUPS.id)
                    .get(i).get(JsonKey.NAME.key).getString().equals(JsonKey.LIP_SYNC.key)
            ) {
                return true
            }
        }
        return false
    }

    /**
     * model3.json data
     */
    private val json: CubismJson

    /**
     * Frequent nodes in the _json data
     */
    private var jsonFrequencyValue: List<ACubismJsonValue>

    init {
        val json: CubismJson
        json = CubismJson.create(buffer)

        this.json = json

        if (jsonFrequencyValue != null) {
            jsonFrequencyValue.clear()
        } else {
            jsonFrequencyValue = ArrayList<ACubismJsonValue?>()
        }

        // The order should match the enum FrequentNode
        jsonFrequencyValue.add(this.json.getRoot().get(JsonKey.GROUPS.key))
        jsonFrequencyValue.add(
            this.json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.MOC.key)
        )
        jsonFrequencyValue.add(
            this.json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.MOTIONS.key)
        )
        jsonFrequencyValue.add(
            this.json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.DISPLAY_INFO.key)
        )
        jsonFrequencyValue.add(
            this.json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.EXPRESSIONS.key)
        )
        jsonFrequencyValue.add(
            this.json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.TEXTURES.key)
        )
        jsonFrequencyValue.add(
            this.json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.PHYSICS.key)
        )
        jsonFrequencyValue.add(
            this.json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.POSE.key)
        )
        jsonFrequencyValue.add(this.json.getRoot().get(JsonKey.HIT_AREAS.key))
    }
}
