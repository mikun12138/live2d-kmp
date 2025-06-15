/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.model

import me.mikun.live2d.core.CubismMoc

class Live2DMoc {
    val moc: CubismMoc

    private val live2DModelList: MutableList<Live2DModel> = mutableListOf()

    constructor(mocBytes: ByteArray, checkMocConsistency: Boolean = false) {
        moc = CubismMoc(mocBytes, checkMocConsistency)
    }

    fun instantiateModel(): Live2DModel {
        return Live2DModel(
            moc.instantiateModel()
        ).also {
            live2DModelList.add(it)
        }
    }

}
