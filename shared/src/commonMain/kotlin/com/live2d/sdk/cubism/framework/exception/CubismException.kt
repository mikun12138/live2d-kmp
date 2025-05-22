/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.exception

/**
 * Cubism SDK専用の例外
 */
class CubismException
/**
 * エラーメッセージを持つ例外を構築する
 *
 * @param msg エラーメッセージ
 */
    (msg: String?) : Exception(msg)
