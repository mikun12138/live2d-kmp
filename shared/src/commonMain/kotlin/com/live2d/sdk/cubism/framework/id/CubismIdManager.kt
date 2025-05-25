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

    fun registerId(id: String): CubismId {
        return CubismId(id).also { ids.add(it) }
    }

    fun id(id: String): CubismId {
        return registerId(id)
    }

    fun id(id: CubismId): CubismId {
        return registerId(id)
    }

    fun registerId(cubismId: CubismId): CubismId {
        return registerId(cubismId.value)
    }

    fun isExist(id: String?): Boolean {
        return findId(id) != null
    }

    fun isExist(id: CubismId?): Boolean {
        return findId(id) != null
    }

    private fun findId(foundId: String?): CubismId? {
        return ids.find { it.value == foundId }
    }

    private fun findId(foundId: CubismId?): CubismId? {
        return ids.find { it == foundId }
    }

    private val ids: MutableSet<CubismId> = mutableSetOf()
}
