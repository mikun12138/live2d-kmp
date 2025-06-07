/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering


interface ILive2DClippingManager {
    companion object {

        /**
         * 実験時に1チャンネルの場合は1、RGBだけの場合は3、アルファも含める場合は4
         */
        var COLOR_CHANNEL_COUNT: Int = 4

        /**
         * 通常のフレームバッファ1枚あたりのマスク最大数
         */
        var CLIPPING_MASK_MAX_COUNT_ON_DEFAULT: Int = 36

        /**
         * フレームバッファが2枚以上ある場合のフレームバッファ1枚あたりのマスク最大数
         */
        var CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE: Int = 32
    }
}
