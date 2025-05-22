/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.exception

/**
 * The exception class which reports that there is a syntax error in the parsed JSON string.
 */
class CubismJsonSyntaxErrorException : CubismJsonParseException {
    /**
     * Construct the exception which has the default message.
     */
    constructor() : super(DEFAULT_MESSAGE)

    /**
     * Construct the exception which has the specified message.
     */
    constructor(msg: String?) : super(DEFAULT_MESSAGE + msg)

    /**
     * Create exception with the specified message and the line number at syntax error.
     *
     * @param msg        error message describing what happened.
     * @param lineNumber line number at syntax error
     */
    constructor(msg: String?, lineNumber: Int) : super(DEFAULT_MESSAGE + msg, lineNumber)

    constructor(msg: String?, cause: Throwable?) : super(DEFAULT_MESSAGE + msg, cause)

    constructor(cause: Throwable?) : super(cause)

    companion object {
        private const val DEFAULT_MESSAGE = "SyntaxError: "
    }
}
