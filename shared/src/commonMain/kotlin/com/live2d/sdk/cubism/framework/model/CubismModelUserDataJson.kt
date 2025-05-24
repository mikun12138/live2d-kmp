/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.CubismFramework.idManager
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJson

/**
 * This class deals with an userdata3.json data.
 */
internal class CubismModelUserDataJson(userdata3Json: ByteArray?) {
    val userDataCount: Int
        /**
         * Get the number of user data in userdata3.json.
         *
         * @return the number of user data
         */
        get() = json.root.get(JsonKey.META.key)
            .get(JsonKey.USER_DATA_COUNT.key)
            .toInt()

    val totalUserDataSize: Int
        /**
         * Get the total user data string number.
         *
         * @return the total user data string number
         */
        get() = json.root.get(JsonKey.META.key)
            .get(JsonKey.TOTAL_USER_DATA_SIZE.key)
            .toInt()

    /**
     * Get the user data type of specified number.
     *
     * @param index index
     * @return user data type
     */
    fun getUserDataTargetType(index: Int): String? {
        return json.root.get(JsonKey.USER_DATA.key).get(index).get(JsonKey.TARGET.key).string
    }

    /**
     * Get the user data target ID of the specified number.
     *
     * @param index index
     * @return a user data target ID
     */
    fun getUserDataId(index: Int): CubismId {
        return idManager!!.id(
            json.root.get(JsonKey.USER_DATA.key)
                .get(index)
                .get(JsonKey.ID.key).string!!
        )
    }

    /**
     * Get the user data string of the specified number.
     *
     * @param index index
     * @return user data
     */
    fun getUserDataValue(index: Int): String? {
        return json.root.get(JsonKey.USER_DATA.key).get(index).get(JsonKey.VALUE.key).string
    }

    private enum class JsonKey(val key: String) {
        META("Meta"),
        USER_DATA_COUNT("UserDataCount"),
        TOTAL_USER_DATA_SIZE("TotalUserDataSize"),
        USER_DATA("UserData"),
        TARGET("Target"),
        ID("Id"),
        VALUE("Value");

    }

    /**
     * JSON data
     */
    private val json: CubismJson = CubismJson.create(userdata3Json!!)
}
