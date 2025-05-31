/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.exception

/**
 * CubismJsonに関連する例外
 */
open class CubismJsonParseException : CubismRuntimeException {
    /**
     * エラーメッセージを持たない例外を構築する
     */
    constructor() : super(DEFAULT_MESSAGE)

    /**
     * 指定されたエラーメッセージを持つ例外を構築する
     *
     * @param msg エラーメッセージ
     */
    constructor(msg: String?) : super(DEFAULT_MESSAGE + msg)

    constructor(
        msg: String?,
        lineNumber: Int
    ) : super("line " + lineNumber + "\n" + DEFAULT_MESSAGE + msg)


    constructor(msg: String?, cause: Throwable?) : super(DEFAULT_MESSAGE + msg, cause)

    constructor(cause: Throwable?) : super(cause)

    companion object {
        private const val DEFAULT_MESSAGE = "Failed to parse the JSON data. "
    }
}
