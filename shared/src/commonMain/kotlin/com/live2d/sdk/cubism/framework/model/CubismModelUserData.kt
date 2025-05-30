/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.data.UserDataJson
import com.live2d.sdk.cubism.framework.id.CubismIdManager
import kotlinx.serialization.json.Json

/**
 * This class is a manager of user data. It can load, manage user data.
 */
class CubismModelUserData {
    constructor(buffer: ByteArray) {
        parse(buffer)
    }

    class CubismModelUserDataNode(
        val targetType: CubismId,
        val targetId: CubismId,
        val value: String,
    )

    private fun parse(buffer: ByteArray) {
        Json.decodeFromString<UserDataJson>(String(buffer)).let { json ->
            val artMeshType = CubismIdManager.id(ART_MESH)

            repeat(json.meta.userDataCount) {
                val addedNode = CubismModelUserDataNode(
                    CubismIdManager.id(json.userData[it].target),
                    CubismIdManager.id(json.userData[it].id),
                    json.userData[it].value
                )
                userDataNodes.add(addedNode)

                if (addedNode.targetType == artMeshType) {
                    _artMeshUserDataNodes.add(addedNode)
                }
            }

        }
    }

    /**
     * the list which has a user data struct class
     */
    private val userDataNodes: MutableList<CubismModelUserDataNode> =
        ArrayList<CubismModelUserDataNode>()

    private val _artMeshUserDataNodes: MutableList<CubismModelUserDataNode> = mutableListOf()
    val artMeshUserDataNodes: List<CubismModelUserDataNode>
        get() = _artMeshUserDataNodes

    companion object {
        /**
         * ID name "ArtMesh"
         */
        private const val ART_MESH = "ArtMesh"
    }
}
