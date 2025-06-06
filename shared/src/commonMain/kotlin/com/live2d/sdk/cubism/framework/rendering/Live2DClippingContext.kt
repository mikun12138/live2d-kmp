/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.type.csmRectF


class Live2DClippingContext(
    val manager: ACubismClippingManager,
    val clippingIdList: IntArray,
    val clippingIdCount: Int,
) {
    val clippedDrawableIndexList: MutableList<Int> = ArrayList()

    fun addClippedDrawable(drawableIndex: Int) {
        clippedDrawableIndexList.add(drawableIndex)
    }

    var isUsing = false


    var bufferIndex = 0
    val layoutBounds: csmRectF = csmRectF()
    var layoutChannelIndex = 0

    val allClippedDrawRect: csmRectF = csmRectF()

    val matrixForMask: CubismMatrix44 = CubismMatrix44.create()

    val matrixForDraw: CubismMatrix44 = CubismMatrix44.create()


}
