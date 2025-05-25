///*
// * Copyright(c) Live2D Inc. All rights reserved.
// *
// * Use of this source code is governed by the Live2D Open Software license
// * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
// */
//package com.live2d.sdk.cubism.framework.utils.jsonparser
//
///**
// * This class expresses JSON Tokens.
// */
//internal class CubismJsonToken {
//    /**
//     * Token types
//     */
//    enum class TokenType {
//        NUMBER,  // ex) 0, 1.0, -1.2e+3
//        STRING,  // ex) "test"
//        BOOLEAN,  // 'true' or 'false'
//        LBRACE,  // '{'
//        RBRACE,  // '}'
//        LSQUARE_BRACKET,  // '['
//        RSQUARE_BRACKET,  // ']'
//        COMMA,
//        COLON,
//        NULL,  // JSON null value
//    }
//
//    /**
//     * Construct a CubismJsonToken by specifying only the token type.
//     *
//     * @param type token type
//     */
//    constructor(type: TokenType?) {
//        tokenType = type
//    }
//
//    /**
//     * Construct a string token.
//     *
//     * @param value string value
//     */
//    constructor(value: String?) {
//        tokenType = TokenType.STRING
//        stringValue = value
//    }
//
//    /**
//     * Construct a number token.
//     *
//     * @param value number value
//     */
//    constructor(value: Double) {
//        tokenType = TokenType.NUMBER
//        numberValue = value
//    }
//
//
//    /**
//     * Construct a boolean token.
//     *
//     * @param value boolean value
//     */
//    constructor(value: Boolean) {
//        tokenType = TokenType.BOOLEAN
//        booleanValue = value
//    }
//
//    /**
//     * Construct a null token.
//     */
//    constructor() {
//        tokenType = TokenType.NULL
//    }
//
//    /**
//     * Get the type of token.
//     *
//     * @return token type
//     */
//    /**
//     * Token type
//     */
//    val tokenType: TokenType?
//    /**
//     * Get string value.
//     *
//     * @return string value
//     */
//    /**
//     * String value
//     */
//    var stringValue: String? = null
//    /**
//     * Get number value.
//     *
//     * @return number value
//     */
//    /**
//     * Number value
//     */
//    var numberValue: Double = 0.0
//    /**
//     * Get boolean value.
//     *
//     * @return boolean value
//     */
//    /**
//     * Boolean value
//     */
//    var booleanValue: Boolean = false
//        private set
//}
