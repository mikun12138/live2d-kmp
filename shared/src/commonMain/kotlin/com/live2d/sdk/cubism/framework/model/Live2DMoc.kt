/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.core.CubismMoc

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
