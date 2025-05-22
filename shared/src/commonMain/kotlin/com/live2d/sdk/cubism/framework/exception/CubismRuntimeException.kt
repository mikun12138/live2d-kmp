/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.exception

/**
 * Cubism SDK専用の実行時例外
 */
open class CubismRuntimeException : RuntimeException {
    /**
     * エラーメッセージを持つ実行時例外を構築する
     *
     * @param msg エラーメッセージ
     */
    constructor(msg: String?) : super(msg)

    /**
     * Creates exception with the specified message and cause.
     *
     * @param msg error message describeing what happened.
     * @param cause cause root exception that caused this exception to be thrown.
     */
    constructor(msg: String?, cause: Throwable?) : super(msg, cause)

    /**
     * Create exception with the specified cause. Consider using [.CubismRuntimeException] instead if you can describe what happened.
     *
     * @param cause cause root exception that caused this exception to be thrown.
     */
    constructor(cause: Throwable?) : super(cause)
}
