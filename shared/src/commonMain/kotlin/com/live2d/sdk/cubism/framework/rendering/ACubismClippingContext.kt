/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.rendering.android.CubismClippingContextAndroid
import com.live2d.sdk.cubism.framework.type.csmRectF

/**
 * クリッピングについての設定を保持するクラス
 * サブクラスに環境依存のフィールドを保持する。
 */
abstract class ACubismClippingContext(
    /**
     * このマスクを管理しているマネージャーのインスタンス
     */
    val manager: ACubismClippingManager,
    /**
     * クリッピングマスクのIDの配列
     * 蒙版id列表
     */
    val clippingIdList: IntArray,
    /**
     * クリッピングマスクの数
     */
    val clippingIdCount: Int
) {
    /**
     * 現在の描画状態でマスクの準備が必要ならtrue
     */
    // use only isUsingHighPrecisionMask == true
    // TODO:: sb
    var isUsing: Boolean = false

    /**
     * RGBAのいずれのチャンネルにこのクリップを配置するか（0:R, 1:G, 2:B, 3:A）
     */
    var layoutChannelIndex: Int = 0

    /**
     * マスク用チャンネルのどの領域にマスクを入れるか(View座標-1..1, UVは0..1に直す)
     */
    lateinit var layoutBounds: csmRectF

    /**
     * このクリッピングで、クリッピングされる全ての描画オブジェクトの囲み矩形（毎回更新）
     */
    lateinit var allClippedDrawRect: csmRectF

    /**
     * マスクの位置計算結果を保持する行列
     */
    val matrixForMask: CubismMatrix44 = CubismMatrix44.create()

    /**
     * 描画オブジェクトの位置計算結果を保持する行列
     */
    val matrixForDraw: CubismMatrix44 = CubismMatrix44.create()
    /**
     * このマスクが割り当てられるレンダーテクスチャ（フレームバッファ）やカラーバッファのインデックス
     */
    var bufferIndex: Int = 0

    companion object {
        /**
         * 渡されたレンダラーの種類に基づき、適切なクリッピングコンテキストを生成する。
         *
         * @param type                    生成するレンダラーの種類
         * @param manager                 このクリッピングコンテキストを保持するマネージャーのインスタンス
         * @param clippingDrawableIndices クリッピングマスクのIDの配列
         * @param clipCount               クリッピングマスクの数
         *
         * @return 生成したクリッピングコンテキストのインスタンス
         */
        fun createClippingContext(
            type: RendererType,
            manager: ICubismClippingManager?,
            clippingDrawableIndices: IntArray?,
            clipCount: Int
        ): ACubismClippingContext {
            when (type) {
                RendererType.ANDROID -> return CubismClippingContextAndroid(
                    manager,
                    clippingDrawableIndices,
                    clipCount
                )

                RendererType.UNKNOWN -> throw IllegalArgumentException("Failed to create a clipping context. The specified renderer type may be incorrect.")
                else -> throw IllegalArgumentException("Failed to create a clipping context. The specified renderer type may be incorrect.")
            }
        }
    }
}
