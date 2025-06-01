/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.Model

expect fun Live2DRenderer.Companion.create(): Live2DRenderer

abstract class Live2DRenderer {

    abstract fun init(model: Model, maskBufferCount: Int)

    fun drawModel(model: Model) {
        /*
         * Call the following methods before and after DoDrawModel() method.
         * ・saveProfile()
         * ・restoreProfile()
         * These methods save and restore the renderer's drawing settings to the state immediately before the model is drawn.
         */
        saveProfile()
        // In the case of clipping mask and buffer preprocessing method
//        if (model.isUsingMasking()) {
        if (true) {

//            if (isUsingHighPrecisionMask()) {
            if (false) {
                clippingManager.setupMatrixForHighPrecision(model)
            } else {
                clippingManager.setupClippingContext(
                    model,
                    this,
                )
            }
        }
        doDrawModel(model)
        restoreProfile()
    }

    abstract fun saveProfile()
    abstract fun doDrawModel(model: Model)
    abstract fun restoreProfile()

    fun draw(model: Model, index: Int) { // model + drawableIndex = drawable
        preDraw()
        doDraw(model, index)
        postDraw()
    }

    protected abstract fun preDraw()
    protected abstract fun doDraw(model: Model, index: Int)
    protected abstract fun postDraw()

    val textures = mutableListOf<Int>()

    val mvpMatrix: CubismMatrix44 = CubismMatrix44.create().apply {
        loadIdentity()
    }
    lateinit var clippingManager: ACubismClippingManager

    var clippingContextBufferForMask: CubismClippingContext? = null

    var clippingContextBufferForDraw: CubismClippingContext? = null

    abstract var isPremultipliedAlpha: Boolean

    companion object

}

class RendererConfig {
    var anisotropy: Float = 0f
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
