/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.Model


expect fun ALive2DRenderer.Companion.create(): ALive2DRenderer

abstract class ALive2DRenderer {


    fun init(model: Model, maskBufferCount: Int) {
        doInit(model, maskBufferCount)
        this.model = model
    }

    abstract fun doInit(model: Model, maskBufferCount: Int)

    fun drawModel() {

        /*
         * Call the following methods before and after DoDrawModel() method.
         * ・saveProfile()
         * ・restoreProfile()
         * These methods save and restore the renderer's drawing settings to the state immediately before the model is drawn.
         */
        saveProfile()
        doDrawModel()
        restoreProfile()
    }


    protected abstract fun doDrawModel()

    protected abstract fun saveProfile()

    protected abstract fun restoreProfile()

    abstract fun preDraw()

    abstract fun drawMeshAndroid(
        model: Model,
        clipDrawIndex: Int,
    )



    val mvpMatrix44: CubismMatrix44 = CubismMatrix44.create().apply {
        loadIdentity()
    }

    var isCulling = false

    var isPremultipliedAlpha = false

    private var anisotropy = 0f

    lateinit var model: Model
    lateinit var offscreenSurfaces: Array<ACubismOffscreenSurface>

    val textures: MutableMap<Int, Int> = HashMap(32)

    private var useHighPrecisionMask = false

    private val tmpModelColor = CubismTextureColor()


    var clippingContextBufferForMask: CubismClippingContext? = null

    var clippingContextBufferForDraw: CubismClippingContext?? = null

    companion object

}

enum class CubismBlendMode {
    NORMAL,  // 通常
    ADDITIVE,  // 加算
    MULTIPLICATIVE,  // 乗算
    MASK // マスク
}

data class CubismTextureColor(
    val r: Float = 1.0f,
    val g: Float = 1.0f,
    val b: Float = 1.0f,
    val a: Float = 1.0f,
)
