///*
// * Copyright(c) Live2D Inc. All rights reserved.
// *
// * Use of this source code is governed by the Live2D Open Software license
// * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
// */
//package com.live2d.sdk.cubism.framework
//
//import com.live2d.sdk.cubism.framework.utils.jsonparser.ACubismJsonValue
//import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJson
//
///**
// * This class handles cdi.json data.
// */
//class CubismCdiJson private constructor(json: CubismJson) {
//    // ----Parameters----//
//    val parametersCount: Int
//        /**
//         * Get the parameters number.
//         *
//         * @return number of parameters
//         */
//        get() {
//            if (!existsParameters()) {
//                return 0
//            }
//            return json.root.get(JsonKey.PARAMETERS.key).size()
//        }
//
//    /**
//     * Get Parameters ID
//     *
//     * @param index index
//     * @return parameter ID
//     */
//    fun getParametersId(index: Int): String {
//        return json.root.get(JsonKey.PARAMETERS.key).get(index).get(JsonKey.ID.key).string!!
//    }
//
//    /**
//     * Get parameters group ID.
//     *
//     * @param index index
//     * @return parameters group ID
//     */
//    fun getParametersGroupId(index: Int): String {
//        return json.root.get(JsonKey.PARAMETERS.key).get(index).get(JsonKey.GROUP_ID.key).string!!
//    }
//
//    /**
//     * Get parameters name
//     *
//     * @param index index
//     * @return parameters name
//     */
//    fun getParametersName(index: Int): String {
//        return json.root.get(JsonKey.PARAMETERS.key).get(index).get(JsonKey.NAME.key).string!!
//    }
//
//    // ----ParameterGroups----//
//    val parameterGroupsCount: Int
//        /**
//         * Get the number of parameter groups.
//         *
//         * @return number of parameter groups
//         */
//        get() {
//            if (!existsParameterGroups()) {
//                return 0
//            }
//            return json.root.get(JsonKey.PARAMETER_GROUPS.key).size()
//        }
//
//    /**
//     * ZGet parameter groups ID
//     *
//     * @param index index
//     * @return parameter groups ID
//     */
//    fun getParameterGroupsId(index: Int): String {
//        return json.root.get(JsonKey.PARAMETER_GROUPS.key).get(index).get(JsonKey.ID.key).string!!
//    }
//
//    /**
//     * Get parameter groups' group ID.
//     *
//     * @param index index
//     * @return parameter groups' group ID
//     */
//    fun getParameterGroupsGroupId(index: Int): String {
//        return json.root.get(JsonKey.PARAMETER_GROUPS.key).get(index).get(JsonKey.GROUP_ID.key).string!!
//    }
//
//    /**
//     * Get parameter groups name
//     *
//     * @param index index
//     * @return parameter groups name
//     */
//    fun getParameterGroupsName(index: Int): String {
//        return json.root.get(JsonKey.PARAMETER_GROUPS.key).get(index).get(JsonKey.NAME.key).string!!
//    }
//
//    // ----Parts----
//    val partsCount: Int
//        /**
//         * Get the number of parts.
//         *
//         * @return number of parts
//         */
//        get() {
//            if (!existsParts()) {
//                return 0
//            }
//            return json.root.get(JsonKey.PARTS.key).size()
//        }
//
//    /**
//     * Get parts ID.
//     *
//     * @param index index
//     * @return parts ID
//     */
//    fun getPartsId(index: Int): String {
//        return json.root.get(JsonKey.PARTS.key).get(index).get(JsonKey.ID.key).string!!
//    }
//
//    /**
//     * Get parts name.
//     *
//     * @param index index
//     * @return parts name
//     */
//    fun getPartsName(index: Int): String {
//        return json.root.get(JsonKey.PARTS.key).get(index).get(JsonKey.NAME.key).string!!
//    }
//
//    // ----- Combined Parameters -----//
//    val combinedParametersCount: Int
//        /**
//         * Returns the number of combined parameters.
//         *
//         * @return number of combined parameters
//         */
//        get() {
//            if (!existsCombinedParameters()) {
//                return 0
//            }
//            return json.root.get(JsonKey.COMBINED_PARAMETERS.key).size()
//        }
//
//    /**
//     * Returns the pair list of the combined parameter.
//     *
//     * @param index index to the desired combined parameters.
//     * @return pair list of the combined parameter
//     */
//    fun getCombinedParameter(index: Int): List<ACubismJsonValue?> {
//        return json.root.get(JsonKey.COMBINED_PARAMETERS.key).get(index).list
//    }
//
//    // JSON keys
//    private enum class JsonKey(val key: String) {
//        VERSION("Version"),
//        PARAMETERS("Parameters"),
//        PARAMETER_GROUPS("ParameterGroups"),
//        PARTS("Parts"),
//        COMBINED_PARAMETERS("CombinedParameters"),
//        ID("Id"),
//        GROUP_ID("GroupId"),
//        NAME("Name");
//
//    }
//
//    /**
//     * Check if the parameter key exists.
//     *
//     * @return If true, the key exists
//     */
//    private fun existsParameters(): Boolean {
//        val node: ACubismJsonValue = json.root.get(JsonKey.PARAMETERS.key)
//        return !node.isNull && !node.isError
//    }
//
//    /**
//     * Check if the parameter group key exists.
//     *
//     * @return If true, the key exists
//     */
//    private fun existsParameterGroups(): Boolean {
//        val node: ACubismJsonValue = json.root.get(JsonKey.PARAMETER_GROUPS.key)
//        return !node.isNull && !node.isError
//    }
//
//    /**
//     * Check if the part's key exists.
//     *
//     * @return If true, the key exists
//     */
//    private fun existsParts(): Boolean {
//        val node: ACubismJsonValue = json.root.get(JsonKey.PARTS.key)
//        return !node.isNull && !node.isError
//    }
//
//    /**
//     * Returns whether the combined parameters exist in the Display Information File(cdi3.json).
//     *
//     * @return If true, the key exists.
//     */
//    private fun existsCombinedParameters(): Boolean {
//        val node: ACubismJsonValue = json.root.get(JsonKey.COMBINED_PARAMETERS.key)
//        return !node.isNull && !node.isError
//    }
//
//    /**
//     * cdi.json data
//     */
//    private val json: CubismJson
//
//    init {
//        this.json = json
//    }
//
//    companion object {
//        fun create(buffer: ByteArray): CubismCdiJson {
//            val json: CubismJson = CubismJson.create(buffer)
//
//            return CubismCdiJson(json)
//        }
//    }
//}
