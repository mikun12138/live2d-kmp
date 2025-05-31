/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Model
import kotlin.Boolean
import kotlin.Int

expect fun <T : Live2DRenderer> Live2DRenderer.create(creation: () -> T): T

abstract class Live2DRenderer {

    /**
     * レンダラーの初期化処理を実行する。<br></br>
     * マスクバッファを2つ以上作成する場合はこのメソッドを使用する。第2引数に何も入れない場合のデフォルト値は1となる。
     *
     * @param model モデルのインスタンス
     * @param maskBufferCount バッファの生成数
     */
    abstract fun initialize(model: Model, maskBufferCount: Int)

    fun drawModel(model: Model) {
        /*
         * Call the following methods before and after DoDrawModel() method.
         * ・saveProfile()
         * ・restoreProfile()
         * These methods save and restore the renderer's drawing settings to the state immediately before the model is drawn.
         */
        saveProfile()
        doDrawModel(model)
        restoreProfile()
    }

    abstract fun saveProfile()
    abstract fun doDrawModel(model: Model)
    abstract fun restoreProfile()
    abstract fun preDraw()
    abstract fun drawMesh(model: Model, index: Int)



    abstract var isPremultipliedAlpha: Boolean


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
