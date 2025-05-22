/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.CubismFramework.idManager
import com.live2d.sdk.cubism.framework.id.CubismId
import java.util.Collections

/**
 * This class is a manager of user data. It can load, manage user data.
 */
class CubismModelUserData {
    /**
     * Class for recording the user data read from JSON.
     */
    class CubismModelUserDataNode(
        targetType: CubismId,
        targetId: CubismId,
        value: String
    ) {
        /**
         * User data target type
         */
        val targetType: CubismId

        /**
         * User data target ID
         */
        val targetId: CubismId

        /**
         * User data
         */
        val value: String

        /**
         * Constructor
         *
         * @param targetType user data target type
         * @param targetId ID of user data target
         * @param value user data
         */
        init {
            requireNotNull(value) { "value is null." }
            this.targetType = idManager!!.getId(targetType)
            this.targetId = idManager!!.getId(targetId)
            this.value = value
        }
    }

    val artMeshUserData: MutableList<CubismModelUserDataNode?>?
        /**
         * Get the user data list of ArtMesh.
         *
         * @return the user data list
         */
        get() {
            if (areArtMeshUserDataNodesChanged) {
                cachedImmutableArtMeshUserDataNodes =
                    Collections.unmodifiableList<CubismModelUserDataNode?>(
                        artMeshUserDataNodes
                    )
                areArtMeshUserDataNodesChanged = false
            }
            return cachedImmutableArtMeshUserDataNodes
        }

    /**
     * Get the user data of ArtMesh.
     *
     * @param index index of data to be obtained
     * @return CubismModelUserDataNode instance
     */
    fun getArtMeshUserData(index: Int): CubismModelUserDataNode? {
        return artMeshUserDataNodes.get(index)
    }

    /**
     * Parse a userdata3.json data.
     *
     * @param buffer a buffer where userdata3.json is loaded.
     * @return If parsing userdata3.json is successful, return true.
     */
    private fun parseUserData(buffer: ByteArray?): Boolean {
        val userdata3Json: CubismModelUserDataJson?
        userdata3Json = CubismModelUserDataJson(buffer)

        val artMeshType = idManager!!.getId(ART_MESH)
        val nodeCount = userdata3Json.userDataCount

        for (i in 0..<nodeCount) {
            val targetType = idManager!!.getId(userdata3Json.getUserDataTargetType(i)!!)
            val targetId = userdata3Json.getUserDataId(i)
            val value = userdata3Json.getUserDataValue(i)
            val addedNode = CubismModelUserData.CubismModelUserDataNode(
                targetType,
                targetId,
                value!!
            )
            userDataNodes.add(addedNode)

            if (addedNode.targetType.equals(artMeshType)) {
                artMeshUserDataNodes.add(addedNode)
            }
        }

        return true
    }

    /**
     * the list which has a user data struct class
     */
    private val userDataNodes: MutableList<CubismModelUserDataNode?> =
        ArrayList<CubismModelUserDataNode?>()

    /**
     * 閲覧リスト保持
     */
    private val artMeshUserDataNodes: MutableList<CubismModelUserDataNode?> =
        ArrayList<CubismModelUserDataNode?>()

    private var areArtMeshUserDataNodesChanged = true

    private var cachedImmutableArtMeshUserDataNodes: MutableList<CubismModelUserDataNode?>? = null

    companion object {
        /**
         * Create an instance.
         *
         * @param buffer a buffer where userdata3.json is loaded.
         * @return the created instance. If parsing JSON data failed, return null.
         */
        fun create(buffer: ByteArray?): CubismModelUserData? {
            val modelUserData = CubismModelUserData()
            val isSuccessful = modelUserData.parseUserData(buffer)

            if (isSuccessful) {
                return modelUserData
            }
            return null
        }

        /**
         * ID name "ArtMesh"
         */
        private const val ART_MESH = "ArtMesh"
    }
}
