/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

interface ICubismClippingManager {

    companion object {
        /**
         * 実験時に1チャンネルの場合は1、RGBだけの場合は3、アルファも含める場合は4
         * 颜色通道数
         */
        const val COLOR_CHANNEL_COUNT: Int = 4

        /**
         * 通常のフレームバッファ1枚あたりのマスク最大数
         * 每个 framebuffer 的 clipping_mask 最大数
         */
        const val CLIPPING_MASK_MAX_COUNT_ON_DEFAULT: Int = 36

        /**
         * フレームバッファが2枚以上ある場合のフレームバッファ1枚あたりのマスク最大数
         * 当有两个或多个 framebuffer 时，每个 framebuffer 的 clipping_mask 最大数
         */
        const val CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE: Int = 32

        /**
         * クリッピングマスクのバッファサイズ（初期値：256）
         */
        const val CLIPPING_MASK_BUFFER_SIZE_X: Float = 256.0f
        const val CLIPPING_MASK_BUFFER_SIZE_Y: Float = 256.0f
    }
}
