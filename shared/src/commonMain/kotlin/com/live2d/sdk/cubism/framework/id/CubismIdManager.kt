/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.id

/**
 * Manager class of ID names
 */
class CubismIdManager {
    /**
     * Register IDs from list
     *
     * @param ids id list
     */
    fun registerIds(ids: MutableList<String>) {
        for (i in ids.indices) {
            val id = ids.get(i)
            registerId(id)
        }
    }

    /**
     * Register the specified number of IDs from the given list.
     *
     * @param ids ID list
     * @param count number of IDs to be registered
     */
    fun registerIds(ids: MutableList<String?>, count: Int) {
        for (i in 0..<count) {
            registerId(ids.get(i)!!)
        }
    }

    /**
     * Register ID name
     *
     * @param id ID name
     * @return ID instance
     */
    fun registerId(id: String): CubismId {
        val foundId = findId(id)

        if (foundId != null) {
            return foundId
        }

        val cubismId = CubismId(id)
        ids.add(cubismId)

        return cubismId
    }

    /**
     * Register ID.
     *
     * @param id ID instance
     * @return ID instance
     */
    fun registerId(id: CubismId): CubismId {
        return registerId(id.string!!)
    }

    /**
     * Get ID from ID name.
     * If the given ID has not registered, register the ID, too.
     *
     * @param id ID name
     * @return ID instance
     */
    fun getId(id: String): CubismId {
        return registerId(id)
    }

    /**
     * Get ID from ID instance.
     * If the given ID has not registered, register the ID, too.
     *
     * @param id ID instance
     * @return ID instance
     */
    fun getId(id: CubismId): CubismId {
        return registerId(id)
    }

    /**
     * Check whether the ID has been already registered from an ID name.
     *
     * @return If given ID has been already registered, return true
     */
    fun isExist(id: String?): Boolean {
        return findId(id) != null
    }

    fun isExist(id: CubismId?): Boolean {
        return findId(id) != null
    }

    /**
     * Search an ID from given ID name.
     *
     * @param foundId ID name
     * @return If there is a registered ID, return the CubismId instance.
     */
    private fun findId(foundId: String?): CubismId? {
        for (i in ids.indices) {
            val id = ids.get(i)

            if (id.string == foundId) {
                return id
            }
        }
        return null
    }

    /**
     * Search an ID from given ID instance.
     *
     * @param foundId ID instance
     * @return If there is a registered ID, return the CubismId instance.
     */
    private fun findId(foundId: CubismId?): CubismId? {
        for (i in ids.indices) {
            val id = ids.get(i)

            if (id.equals(foundId)) {
                return id
            }
        }
        return null
    }

    /**
     * The registered IDs list.
     */
    private val ids: MutableList<CubismId> = ArrayList<CubismId>()
}
