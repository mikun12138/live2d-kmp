/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework

import com.live2d.sdk.cubism.framework.id.CubismId

/**
 * This class deals with model3.json data.
 */
class CubismModelSettingJson(buffer: ByteArray?) : ICubismModelSetting {
    @Override
    fun getJson(): CubismJson {
        return json
    }

    @get:Override
    val modelFileName: String?
        get() {
            if (!existsModelFile()) {
                return ""
            }

            return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOC.id)
                .getString()
        }

    @get:Override
    val textureCount: Int
        get() {
            if (!existsTextureFiles()) {
                return 0
            }

            return json.getRoot()
                .get(JsonKey.FILE_REFERENCES.key)
                .get(JsonKey.TEXTURES.key)
                .size()
        }

    @get:Override
    val textureDirectory: String?
        get() {
            if (!existsTextureFiles()) {
                return ""
            }

            val rowString: String =
                jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.TEXTURES.id)
                    .get(0).getString()
            return rowString.split("/")[0]
        }

    @Override
    fun getTextureFileName(index: Int): String {
        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.TEXTURES.id)
            .get(index).getString()
    }

    @get:Override
    val hitAreasCount: Int
        get() {
            if (!existsHitAreas()) {
                return 0
            }
            return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.HIT_AREAS.id)
                .size()
        }

    @Override
    fun getHitAreaId(index: Int): CubismId {
        return CubismFramework.getIdManager().getId(
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.HIT_AREAS.id)
                .get(index).get(JsonKey.ID.key).getString()
        )
    }

    @Override
    fun getHitAreaName(index: Int): String {
        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.HIT_AREAS.id)
            .get(index).get(JsonKey.NAME.key).getString()
    }

    @get:Override
    val physicsFileName: String?
        get() {
            if (!existsPhysicsFile()) {
                return ""
            }
            return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.PHYSICS.id)
                .getString()
        }

    @get:Override
    val poseFileName: String?
        get() {
            if (!existsPoseFile()) {
                return ""
            }

            return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.POSE.id)
                .getString()
        }

    @get:Override
    val displayInfoFileName: String?
        get() {
            if (!existsDisplayInfoFile()) {
                return ""
            }
            return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.DISPLAY_INFO.id)
                .getString()
        }

    @get:Override
    val expressionCount: Int
        get() {
            if (!existsExpressionFile()) {
                return 0
            }
            return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.EXPRESSIONS.id)
                .size()
        }

    @Override
    fun getExpressionName(index: Int): String {
        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.EXPRESSIONS.id)
            .get(index).get(JsonKey.NAME.key).getString()
    }

    @Override
    fun getExpressionFileName(index: Int): String {
        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.EXPRESSIONS.id)
            .get(index).get(JsonKey.FILEPATH.key).getString()
    }

    @get:Override
    val motionGroupCount: Int
        get() {
            if (!existsMotionGroups()) {
                return 0
            }
            return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
                .size()
        }

    @Override
    fun getMotionGroupName(index: Int): String? {
        if (!existsMotionGroups()) {
            return null
        }
        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
            .getKeys().get(index).getString()
    }

    @Override
    fun getMotionCount(groupName: String?): Int {
        if (!existsMotionGroupName(groupName)) {
            return 0
        }
        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
            .get(groupName).size()
    }

    @Override
    fun getMotionFileName(groupName: String?, index: Int): String? {
        if (!existsMotionGroupName(groupName)) {
            return ""
        }

        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.FILEPATH.key).getString()
    }

    @Override
    fun getMotionSoundFileName(groupName: String?, index: Int): String? {
        if (!existsMotionSoundFile(groupName, index)) {
            return ""
        }

        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.SOUND_PATH.key).getString()
    }

    @Override
    fun getMotionFadeInTimeValue(groupName: String?, index: Int): Float {
        if (!existsMotionFadeIn(groupName, index)) {
            return -1.0f
        }

        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.FADE_IN_TIME.key).toFloat()
    }

    @Override
    fun getMotionFadeOutTimeValue(groupName: String?, index: Int): Float {
        if (!existsMotionFadeOut(groupName, index)) {
            return -1.0f
        }

        return jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
            .get(groupName).get(index).get(JsonKey.FADE_OUT_TIME.key).toFloat()
    }

    @get:Override
    val userDataFile: String?
        get() {
            if (!existsUserDataFile()) {
                return ""
            }
            return json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.USER_DATA.key)
                .getString()
        }

    @Override
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

    @get:Override
    val eyeBlinkParameterCount: Int
        get() {
            if (!existsEyeBlinkParameters()) {
                return 0
            }

            var eyeBlinkParameterCount = 0
            for (i in 0..<jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                .size()) {
                val refI: ACubismJsonValue =
                    jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
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

    @Override
    fun getEyeBlinkParameterId(index: Int): CubismId? {
        if (!existsEyeBlinkParameters()) {
            return null
        }

        for (i in 0..<jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
            .size()) {
            val refI: ACubismJsonValue =
                jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                    .get(i)

            if (refI.isNull() || refI.isError()) {
                continue
            }

            if (refI.get(JsonKey.NAME.key).getString().equals(JsonKey.EYE_BLINK.key)) {
                return CubismFramework.getIdManager()
                    .getId(refI.get(JsonKey.IDS.key).get(index).getString())
            }
        }
        return null
    }


    @get:Override
    val lipSyncParameterCount: Int
        get() {
            if (!existsLipSyncParameters()) {
                return 0
            }


            var lipSyncParameterCount = 0
            for (i in 0..<jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                .size()) {
                val refI: ACubismJsonValue =
                    jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
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

    @Override
    fun getLipSyncParameterId(index: Int): CubismId? {
        if (!existsLipSyncParameters()) {
            return null
        }

        for (i in 0..<jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
            .size()) {
            val refI: ACubismJsonValue =
                jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
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
    private enum class FrequentNode(private val id: Int) {
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

    private enum class JsonKey(key: String) {
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

        private val key: String?

        init {
            this.key = key
        }
    }


    // キーが存在するかどうかのチェック
    private fun existsModelFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOC.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsTextureFiles(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.TEXTURES.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsHitAreas(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.HIT_AREAS.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsPhysicsFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.PHYSICS.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsPoseFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.POSE.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsDisplayInfoFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.DISPLAY_INFO.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsExpressionFile(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.EXPRESSIONS.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsMotionGroups(): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
        return !node.isNull() && !node.isError()
    }

    private fun existsMotionGroupName(groupName: String?): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
                .get(groupName)
        return !node.isNull() && !node.isError()
    }

    private fun existsMotionSoundFile(groupName: String?, index: Int): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
                .get(groupName).get(index).get(JsonKey.SOUND_PATH.key)
        return !node.isNull() && !node.isError()
    }

    private fun existsMotionFadeIn(groupName: String?, index: Int): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
                .get(groupName).get(index).get(JsonKey.FADE_IN_TIME.key)
        return !node.isNull() && !node.isError()
    }

    private fun existsMotionFadeOut(groupName: String?, index: Int): Boolean {
        val node: ACubismJsonValue =
            jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.MOTIONS.id)
                .get(groupName).get(index).get(JsonKey.FADE_OUT_TIME.key)
        return !node.isNull() && !node.isError()
    }

    private fun existsUserDataFile(): Boolean {
        return !json.getRoot().get(JsonKey.FILE_REFERENCES.key).get(JsonKey.USER_DATA.key).isNull()
    }

    private fun existsEyeBlinkParameters(): Boolean {
        if (jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                .isNull() || jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                .isError()
        ) {
            return false
        }

        for (i in 0..<jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
            .size()) {
            if (jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                    .get(i).get(JsonKey.NAME.key).getString().equals(JsonKey.EYE_BLINK.key)
            ) {
                return true
            }
        }

        return false
    }

    private fun existsLipSyncParameters(): Boolean {
        if (jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                .isNull() || jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
                .isError()
        ) {
            return false
        }

        for (i in 0..<jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
            .size()) {
            if (jsonFrequencyValue!!.get(com.live2d.sdk.cubism.framework.CubismModelSettingJson.FrequentNode.GROUPS.id)
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
    private var jsonFrequencyValue: List<ACubismJsonValue?>? = null

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
