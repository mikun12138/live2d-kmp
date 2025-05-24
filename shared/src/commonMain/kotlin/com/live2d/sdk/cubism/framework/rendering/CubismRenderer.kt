/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.CubismModel
import kotlin.Boolean
import kotlin.Int

/**
 * A renderer which processes drawing models.
 *
 *
 * Environment-dependent drawing instructions are written in subclasses that inherit from this class.
 */
abstract class CubismRenderer protected constructor() {
    // TODO:: remove this
//    enum class RendererType {
//        ANDROID,
//        UNKNOWN // 不明・未定義なレンダラー
//    }

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

    /**
     * Initialize this renderer.
     * A model instance has the information which is required to initialize the renderer.
     *
     * @param model model instance
     */
    fun initialize(model: CubismModel) {
        this.model = model
    }

    /**
     * レンダラーの初期化処理を実行する。<br></br>
     * マスクバッファを2つ以上作成する場合はこのメソッドを使用する。第2引数に何も入れない場合のデフォルト値は1となる。
     *
     * @param model モデルのインスタンス
     * @param maskBufferCount バッファの生成数
     */
    abstract fun initialize(model: CubismModel?, maskBufferCount: Int)

    fun close() {
        model.close()
    }

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

    fun getModelColorWithOpacity(opacity: Float): CubismTextureColor {
        return CubismTextureColor().apply {
            this.r = modelColor.r
            this.g = modelColor.g
            this.b = modelColor.b
            this.a = modelColor.a

            this.a *= opacity

            if (this@CubismRenderer.isPremultipliedAlpha) {
                this.r *= this.a
                this.g *= this.a
                this.b *= this.a
            }
        }
    }

    protected abstract fun doDrawModel()

    protected abstract fun saveProfile()

    protected abstract fun restoreProfile()

    private val mvpMatrix44: CubismMatrix44 = CubismMatrix44.create()

    var isCulling: Boolean = false

    var isPremultipliedAlpha: Boolean = false

    var anisotropy: Float = 0f

    lateinit var model: CubismModel
    /**
     * If this is false, the masks are drawn together. If this is true, the masks are redrawn for each part drawing.
     */
    var isUsingHighPrecisionMask: Boolean = false

    init {
        mvpMatrix44.loadIdentity()
    }
}
