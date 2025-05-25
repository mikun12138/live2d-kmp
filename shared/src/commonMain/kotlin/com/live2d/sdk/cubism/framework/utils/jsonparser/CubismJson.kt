///*
// * Copyright(c) Live2D Inc. All rights reserved.
// *
// * Use of this source code is governed by the Live2D Open Software license
// * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
// */
//package com.live2d.sdk.cubism.framework.utils.jsonparser
//
//import com.live2d.sdk.cubism.framework.exception.CubismJsonParseException
//import com.live2d.sdk.cubism.framework.exception.CubismJsonSyntaxErrorException
//import java.io.IOException
//
///**
// * This class has some functions related to JSON.
// */
//class CubismJson
///**
// * Private constructor
// */
//private constructor() {
//    /**
//     * Parse JSON string.
//     *
//     * @param buffer JSON byte data
//     */
//    private fun parse(buffer: ByteArray) {
//        try {
//            val json = String(buffer, charset("UTF-8"))
//            lexer = CubismJsonLexer(json)
//
//            token = lexer!!.nextToken
//            root = createValue()
//        } catch (e: IOException) {
//            throw CubismJsonParseException(
//                "It seems that an error has occured in the input/output processing",
//                e
//            )
//        }
//    }
//
//    /**
//     * Construct a JSON value.
//     *
//     * @return JSON Value
//     */
//    @Throws(CubismJsonParseException::class, IOException::class)
//    private fun createValue(): ACubismJsonValue {
//        // JSON Object
//        if (token!!.tokenType == CubismJsonToken.TokenType.LBRACE) {
//            objectNestingLevel++
//            val `object` = createObject()
//
//            // If parsing is midway, the next token is read.
//            if (objectNestingLevel != 0 || arrayNestingLevel != 0) {
//                token = lexer!!.nextToken
//            }
//            return `object`
//        } else if (token!!.tokenType == CubismJsonToken.TokenType.LSQUARE_BRACKET) {
//            arrayNestingLevel++
//            val array: CubismJsonArray = createArray()
//
//            // If parsing is midway, the next token is read.
//            if (objectNestingLevel != 0 || arrayNestingLevel != 0) {
//                token = lexer!!.nextToken
//            }
//            return array
//        } else if (token!!.tokenType == CubismJsonToken.TokenType.NUMBER) {
//            val number: CubismJsonNumber = CubismJsonNumber.valueOf(token!!.numberValue)
//
//            if (objectNestingLevel != 0 || arrayNestingLevel != 0) {
//                token = lexer!!.nextToken
//            }
//            return number
//        } else if (token!!.tokenType == CubismJsonToken.TokenType.STRING) {
//            val string: CubismJsonString = CubismJsonString.valueOf(token!!.stringValue!!)
//
//            if (objectNestingLevel != 0 || arrayNestingLevel != 0) {
//                token = lexer!!.nextToken
//            }
//            return string
//        } else if (token!!.tokenType == CubismJsonToken.TokenType.BOOLEAN) {
//            val bool: CubismJsonBoolean = CubismJsonBoolean.valueOf(token!!.booleanValue)
//
//            if (objectNestingLevel != 0 || arrayNestingLevel != 0) {
//                token = lexer!!.nextToken
//            }
//            return bool
//        } else if (token!!.tokenType == CubismJsonToken.TokenType.NULL) {
//            val nullValue = CubismJsonNullValue()
//
//            if (objectNestingLevel != 0 || arrayNestingLevel != 0) {
//                token = lexer!!.nextToken
//            }
//            return nullValue
//        } else {
//            throw CubismJsonSyntaxErrorException(
//                "Incorrect JSON format.",
//                lexer!!.currentLineNumber - 1
//            )
//        }
//    }
//
//    /**
//     * Construct a JSON object
//     *
//     * @return JSON Object
//     */
//    @Throws(CubismJsonParseException::class, IOException::class)
//    private fun createObject(): CubismJsonObject {
//        val `object` = CubismJsonObject()
//
//        token = lexer!!.nextToken
//
//        // If the next token is braces, this object is regarded as empty object
//        if (token!!.tokenType == CubismJsonToken.TokenType.RBRACE) {
//            objectNestingLevel--
//            return `object`
//        } else {
//            // Continue reading until closed by '}'
//            // If the format is not "string : value (, string : value, ...)", an exception is thrown.
//            while (true) {
//                val string: CubismJsonString?
//                val value: ACubismJsonValue?
//
//                // Construct a string value
//                if (token!!.tokenType == CubismJsonToken.TokenType.STRING) {
//                    string = CubismJsonString.valueOf(token!!.stringValue!!)
//                } else {
//                    throw CubismJsonSyntaxErrorException(
//                        "JSON Object's format is incorrect.",
//                        lexer!!.currentLineNumber
//                    )
//                }
//
//                token = lexer!!.nextToken
//
//                // If it is not divided by colon, an exception is thrown.
//                if (token!!.tokenType != CubismJsonToken.TokenType.COLON) {
//                    throw CubismJsonSyntaxErrorException(
//                        "JSON Object's format is incorrect.",
//                        lexer!!.currentLineNumber
//                    )
//                }
//
//                token = lexer!!.nextToken
//                value = createValue()
//
//                // Put a pair of string and value into object
//                `object`.putPair(string, value)
//
//                // If the next token is comma, reading is continued. If the next token is '}', it is done to "break".
//                if (token!!.tokenType == CubismJsonToken.TokenType.RBRACE) {
//                    objectNestingLevel--
//                    break
//                } else if (token!!.tokenType == CubismJsonToken.TokenType.COMMA) {
//                    token = lexer!!.nextToken
//                } else {
//                    throw CubismJsonSyntaxErrorException(
//                        "JSON Object's format is incorrect.",
//                        lexer!!.currentLineNumber - 1
//                    )
//                }
//            }
//        }
//        return `object`
//    }
//
//    /**
//     * Construct a JSON array.
//     *
//     * @return JSON Array
//     *
//     * @throws CubismJsonParseException an exception related to parsing
//     */
//    @Throws(CubismJsonParseException::class, IOException::class)
//    private fun createArray(): CubismJsonArray {
//        val array: CubismJsonArray = CubismJsonArray()
//
//        token = lexer!!.nextToken
//
//        // If the next token is square brackets, this array is regarded as empty array.
//        if (token!!.tokenType == CubismJsonToken.TokenType.RSQUARE_BRACKET) {
//            arrayNestingLevel--
//            return array
//        } else {
//            // Continue reading until closed by ']'
//            // If the format is not "value (, value, ...)", an exception is thrown.
//            while (true) {
//                val value: ACubismJsonValue?
//
//                // Construct a value
//                value = createValue()
//                // Put the value into array
//                array.putValue(value)
//
//                // If the next token is comma, reading is continued. If the next token is ']', it is done to "break".
//                if (token!!.tokenType == CubismJsonToken.TokenType.RSQUARE_BRACKET) {
//                    arrayNestingLevel--
//                    break
//                } else if (token!!.tokenType == CubismJsonToken.TokenType.COMMA) {
//                token = lexer!!.nextToken
//                } else {
//                    throw CubismJsonSyntaxErrorException(
//                        "JSON Array's format is incorrect.",
//                        lexer!!.currentLineNumber - 1
//                    )
//                }
//            }
//        }
//        return array
//    }
//
//    /**
//     * Get a root of a parsed JSON.
//     *
//     * @return JSON root
//     */
//    /**
//     * JSON root
//     */
//    lateinit var root: ACubismJsonValue
//        private set
//
//    /**
//     * JSON lexer
//     */
//    private var lexer: CubismJsonLexer? = null
//
//    /**
//     * JSON token
//     */
//    private var token: CubismJsonToken? = null
//
//    /**
//     * A nest level of JSON object. If left brace is appeared, nest level increases by 1, and if right brace is done, it decreases by 1.
//     */
//    private var objectNestingLevel = 0
//
//    private var arrayNestingLevel = 0
//
//    companion object {
//        /**
//         * Creates the JSON object.
//         *
//         * @param buffer byte data of the JSON
//         * @return JSON object
//         *
//         * @throws IllegalArgumentException If the argument is null
//         */
//        fun create(buffer: ByteArray): CubismJson {
//            require(!(buffer == null || buffer.size == 0)) { "Parsed JSON data is empty." }
//
//            val json = CubismJson()
//            json.parse(buffer)
//
//            return json
//        }
//    }
//}
