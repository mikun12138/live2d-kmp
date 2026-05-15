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

    constructor(mocBytes: ByteArray, checkMocConsistency: Boolean = true) {
        moc = CubismMoc(mocBytes, checkMocConsistency)
    }

    fun instantiateModel(): Live2DModel {
        return Live2DModel(
            moc.instantiateModel()
        )
    }

}
