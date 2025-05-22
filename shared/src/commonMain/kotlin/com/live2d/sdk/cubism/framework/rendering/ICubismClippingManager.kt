/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismVector2
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer.CubismTextureColor
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer.RendererType
import com.live2d.sdk.cubism.framework.type.csmRectF

interface ICubismClippingManager {
    /**
     * マネージャーの初期化処理
     * クリッピングマスクを使う描画オブジェクトの登録を行う。
     *
     * @param type               レンダラーの種類
     * @param model              モデルのインスタンス
     * @param maskBufferCount    バッファの生成数
     */
    fun initialize(
        type: RendererType?,
        model: CubismModel?,
        maskBufferCount: Int
    )

    /**
     * 高精細マスク処理用の行列を計算する。
     *
     * @param model         モデルのインスタンス
     * @param isRightHanded 処理が右手系かどうか。右手系ならtrue
     */
    fun setupMatrixForHighPrecision(model: CubismModel?, isRightHanded: Boolean)

    /**
     * マスク作成・描画用の行列を作成する。
     *
     * @param isRightHanded       座標を右手系として扱うかどうか。右手系として扱うならtrue
     * @param layoutBoundsOnTex01 マスクを収める領域
     * @param scaleX              描画オブジェクトのX方向への伸縮率
     * @param scaleY              描画オブジェクトのY方向への伸縮率
     */
    fun createMatrixForMask(
        isRightHanded: Boolean,
        layoutBoundsOnTex01: csmRectF?,
        scaleX: Float,
        scaleY: Float
    )

    /**
     * クリッピングコンテキストを配置するレイアウト。
     * 1つのレンダーテクスチャを極力いっぱいに使ってマスクをレイアウトする。
     * マスクグループの数が4以下ならRGBA各チャンネルに1つずつマスクを配置し、5以上6以下ならRGBAを2, 2, 1, 1と配置する。
     *
     * @param usingClipCount 配置するクリッピングコンテキストの数
     */
    fun setupLayoutBounds(usingClipCount: Int)

    /**
     * クリッピングマスクバッファのサイズを取得する。
     *
     * @return クリッピングマスクバッファのサイズ
     */
    val clippingMaskBufferSize: CubismVector2?

    /**
     * クリッピングマスクバッファのサイズを設定する。
     *
     * @param width  クリッピングマスクバッファの幅
     * @param height クリッピングマスクバッファの高さ
     */
    fun setClippingMaskBufferSize(width: Float, height: Float)

    /**
     * このバッファのレンダーテクスチャの枚数を取得する。
     *
     * @return このバッファのレンダーテクスチャの枚数
     */
    val renderTextureCount: Int

    /**
     * カラーチャンネル（RGBA）のフラグを取得する。
     *
     * @param channelIndex カラーチャンネル（RGBA）の番号（0:R, 1:G, 2:B, 3:A）
     * @return カラーチャンネルのフラグ
     */
    fun getChannelFlagAsColor(channelIndex: Int): CubismTextureColor?

    companion object {
        /**
         * 実験時に1チャンネルの場合は1、RGBだけの場合は3、アルファも含める場合は4
         */
        const val COLOR_CHANNEL_COUNT: Int = 4

        /**
         * 通常のフレームバッファ1枚あたりのマスク最大数
         */
        const val CLIPPING_MASK_MAX_COUNT_ON_DEFAULT: Int = 36

        /**
         * フレームバッファが2枚以上ある場合のフレームバッファ1枚あたりのマスク最大数
         */
        const val CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE: Int = 32
    }
}
