/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.id

object Live2DIdManager {

    fun registerId(id: String): Live2DId {
        return Live2DId(id).also { ids.add(it) }
    }

    fun id(id: String): Live2DId {
        return registerId(id)
    }

    fun id(id: Live2DId): Live2DId {
        return registerId(id)
    }

    fun registerId(live2DId: Live2DId): Live2DId {
        return registerId(live2DId.value)
    }

    fun isExist(id: String?): Boolean {
        return findId(id) != null
    }

    fun isExist(id: Live2DId?): Boolean {
        return findId(id) != null
    }

    private fun findId(foundId: String?): Live2DId? {
        return ids.find { it.value == foundId }
    }

    private fun findId(foundId: Live2DId?): Live2DId? {
        return ids.find { it == foundId }
    }

    fun clear() {
        ids.clear()
    }

    private val ids: MutableSet<Live2DId> = mutableSetOf()
}
