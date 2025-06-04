package com.live2d.sdk.cubism.framework.userdata

import com.live2d.sdk.cubism.framework.data.UserDataJson
import com.live2d.sdk.cubism.framework.id.Live2DId
import com.live2d.sdk.cubism.framework.id.Live2DIdManager
import kotlinx.serialization.json.Json

/**
 * This class is a manager of user data. It can load, manage user data.
 */
class CubismModelUserData {
    constructor(buffer: ByteArray) {
        parse(buffer)
    }

    class CubismModelUserDataNode(
        val targetType: Live2DId,
        val targetId: Live2DId,
        val value: String,
    )

    private fun parse(buffer: ByteArray) {
        Json.Default.decodeFromString<UserDataJson>(String(buffer)).let { json ->
            val artMeshType = Live2DIdManager.id(ART_MESH)

            repeat(json.meta.userDataCount) {
                val addedNode = CubismModelUserDataNode(
                    Live2DIdManager.id(json.userData[it].target),
                    Live2DIdManager.id(json.userData[it].id),
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