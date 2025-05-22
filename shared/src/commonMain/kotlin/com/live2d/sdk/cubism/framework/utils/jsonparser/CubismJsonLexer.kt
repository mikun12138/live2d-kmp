/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.utils.jsonparser

import com.live2d.sdk.cubism.framework.exception.CubismJsonParseException
import java.io.IOException
import java.util.Arrays

/**
 * This class offers a function of JSON lexer.
 */
internal class CubismJsonLexer(json: String) {
    @get:Throws(CubismJsonParseException::class, IOException::class)
    val nextToken: CubismJsonToken
        /**
         * Get a next token.
         */
        get() {
            // Skip blank characters
            while (isWhiteSpaceChar(nextChar)) {
                updateNextChar()
            }

            // null文字で埋める
            Arrays.fill(parsedTokonBuffer, 0, bufferIndex, '\u0000')
            bufferIndex = 0

            // A Number token
            // A process when beginning at minus sign
            if (nextChar == '-') {
                append('-')
                updateNextChar()

                if (Character.isDigit(nextChar)) {
                    buildNumber()
                    val numberStr = String(parsedTokonBuffer, 0, bufferIndex)
                    NUMBER.numberValue =
                        numberStr.toDouble()

                    return NUMBER
                } else {
                    throw CubismJsonParseException(
                        "Number's format is incorrect.",
                        this.currentLineNumber
                    )
                }
            } else if (Character.isDigit(nextChar)) {
                buildNumber()
                val numberStr = String(parsedTokonBuffer, 0, bufferIndex)
                NUMBER.numberValue =
                    numberStr.toDouble()

                return NUMBER
            } else if (nextChar == 't') {
                append(nextChar)
                updateNextChar()

                for (i in 0..2) {
                    append(nextChar)
                    updateNextChar()
                }

                // If "value" does not create true value, send an exception.
                val trueString = String(parsedTokonBuffer, 0, bufferIndex)
                if (trueString != "true") {
                    throw CubismJsonParseException(
                        "Boolean's format or spell is incorrect.",
                        this.currentLineNumber
                    )
                }
                return TRUE
            } else if (nextChar == 'f') {
                append(nextChar)
                updateNextChar()

                for (i in 0..3) {
                    append(nextChar)
                    updateNextChar()
                }

                // If the value does not equals to "false" value, send the exception.
                val falseString = String(parsedTokonBuffer, 0, bufferIndex)
                if (falseString != "false") {
                    throw CubismJsonParseException(
                        "Boolean's format or spell is incorrect.",
                        this.currentLineNumber
                    )
                }
                return FALSE
            } else if (nextChar == 'n') {
                append(nextChar)
                updateNextChar()

                for (i in 0..2) {
                    append(nextChar)
                    updateNextChar()
                }

                // If the JSON value does not equal to the "null" value, send an exception.
                val nullString = String(parsedTokonBuffer, 0, bufferIndex)
                if (nullString != "null") {
                    throw CubismJsonParseException(
                        "JSON Null's format or spell is incorrect.",
                        this.currentLineNumber
                    )
                }
                return NULL
            } else if (nextChar == '{') {
                updateNextChar()
                return LBRACE
            } else if (nextChar == '}') {
                updateNextChar()
                return RBRACE
            } else if (nextChar == '[') {
                updateNextChar()
                return LSQUARE_BRACKET
            } else if (nextChar == ']') {
                updateNextChar()
                return RSQUARE_BRACKET
            } else if (nextChar == '"') {
                updateNextChar()

                // Until closing by double quote("), it is continued to read.
                while (nextChar != '"') {
                    // Consider a escape sequence.
                    if (nextChar == '\\') {
                        updateNextChar()
                        buildEscapedString()
                    } else {
                        append(nextChar)
                    }
                    updateNextChar()
                }
                updateNextChar()
                STRING.stringValue =
                    String(parsedTokonBuffer, 0, bufferIndex)

                return STRING
            } else if (nextChar == ':') {
                updateNextChar()
                return COLON
            } else if (nextChar == ',') {
                updateNextChar()
                return COMMA
            }
            throw CubismJsonParseException(
                "The JSON is not closed properly, or there is some other malformed form.",
                this.currentLineNumber
            )
        }

    /**
     * Build number string.
     *
     * @throws CubismJsonParseException the exception at failing to parse
     */
    @Throws(CubismJsonParseException::class)
    private fun buildNumber() {
        if (nextChar == '0') {
            append(nextChar)
            updateNextChar()
            buildDoubleOrExpNumber()
        } else {
            append(nextChar)
            updateNextChar()

            // Repeat processes until appearing a character except dot, exponential expression or number.
            while (Character.isDigit(nextChar)) {
                append(nextChar)
                updateNextChar()
            }
            buildDoubleOrExpNumber()
        }
    }

    /**
     * Build double or exponential number.
     *
     * @throws CubismJsonParseException the exception at failing to parse
     */
    @Throws(CubismJsonParseException::class)
    private fun buildDoubleOrExpNumber() {
        // If the next character is dot, floating point number is created.
        if (nextChar == '.') {
            buildDoubleNumber()
        }
        // If there is an e or E, it is considered an exponential expression.
        if (nextChar == 'e' || nextChar == 'E') {
            buildExponents()
        }
    }

    /**
     * Return floating point number as strings(StringBuilder).
     *
     * @throws CubismJsonParseException the exception at failing to parse
     */
    @Throws(CubismJsonParseException::class)
    private fun buildDoubleNumber() {
        append('.')
        updateNextChar()

        // If the character following dot sign is not a number, an exception is thrown.
        if (!Character.isDigit(nextChar)) {
            throw CubismJsonParseException("Number's format is incorrect.", this.currentLineNumber)
        }
        do {
            append(nextChar)
            updateNextChar()
        } while (Character.isDigit(nextChar))
    }

    /**
     * Build a number string used an exponential expression.
     *
     * @throws CubismJsonParseException the exception at failing to parse
     */
    @Throws(CubismJsonParseException::class)
    private fun buildExponents() {
        append(nextChar)
        updateNextChar()

        // Handle cases where a number is preceded by a sign.
        if (nextChar == '+') {
            append(nextChar)
            updateNextChar()
        } else if (nextChar == '-') {
            append(nextChar)
            updateNextChar()
        }
        // If the character is not a number or a sign, an exception is thrown.
        if (!Character.isDigit(nextChar)) {
            throw CubismJsonParseException(
                String(
                    parsedTokonBuffer,
                    0,
                    bufferIndex
                ) + "\n: " + "Exponent value's format is incorrect.",
                this.currentLineNumber
            )
        }

        do {
            append(nextChar)
            updateNextChar()
        } while (Character.isDigit(nextChar))
    }

    /**
     * Build a string used an escape sequence.
     *
     * @throws CubismJsonParseException the exception at failing to parse
     */
    @Throws(CubismJsonParseException::class)
    private fun buildEscapedString() {
        when (nextChar) {
            '"', '\\', '/' -> append(nextChar)
            'b' -> append('\b')
            'f' -> append('\f')
            'n' -> append('\n')
            'r' -> append('\r')
            't' -> append('\t')
            'u' -> {
                // バッファをクリアする
                bufferForHexadecimalString.delete(0, 16)

                bufferForHexadecimalString.append('\\')
                bufferForHexadecimalString.append('u')
                run {
                    var i = 0
                    while (i < 4) {
                        updateNextChar()
                        bufferForHexadecimalString.append(nextChar)
                        i++
                    }
                }
                // Check whether it is hex number. If there is a problem, an exception is thrown.
                val tmp = bufferForHexadecimalString.toString()
                if (!tmp.matches("\\\\u[a-fA-F0-9]{4}".toRegex())) {
                    throw CubismJsonParseException(
                        bufferForHexadecimalString.toString() + "\n: " + "The unicode notation is incorrect.",
                        this.currentLineNumber
                    )
                }

                var i = 0
                while (i < tmp.length) {
                    append(tmp.get(i))
                    i++
                }
            }
        }
    }

    /**
     * Whether a character is white space character.
     *
     * @param c checked character
     * @return If the character is white space character, return true
     */
    private fun isWhiteSpaceChar(c: Char): Boolean {
        return (c == ' ' || c == '\r' || c == '\n' || c == '\t')
    }

    /**
     * Read a next character
     */
    private fun updateNextChar() {
        // 文字を全部読んだら、次の文字をnull文字にセットしてreturnする
        if (charIndex >= jsonCharsLength) {
            nextChar = '\u0000'
            return
        }

        nextChar = jsonChars[charIndex]
        charIndex++

        // 改行コードがあれば行数をインクリメントする
        if (nextChar == '\n') {
            this.currentLineNumber++
        }
    }

    /**
     * Tokonのパース用の文字列バッファに、引数で指定された文字リテラルを追加する。
     *
     * @param c 追加する文字リテラル
     */
    private fun append(c: Char) {
        // Tokenをパースするためのバッファがいっぱいになったら、バッファサイズを2倍にする
        if (bufferLength == bufferIndex) {
            bufferLength *= 2
            val tmp = CharArray(bufferLength)
            System.arraycopy(parsedTokonBuffer, 0, tmp, 0, bufferIndex)

            parsedTokonBuffer = tmp
        }
        parsedTokonBuffer[bufferIndex] = c
        bufferIndex++
    }

    /**
     * パースするJSON文字列
     */
    private val jsonChars: CharArray

    /**
     * 現在読んでいる文字のインデックス
     */
    private var charIndex = 0

    /**
     * パースするJSON文字列の文字数
     */
    private val jsonCharsLength: Int
    /**
     * Return current line number.
     *
     * @return current line number
     */
    /**
     * 行数。改行文字が出てくるたびにインクリメントされる。
     */
    var currentLineNumber: Int = 1
        private set

    /**
     * the next character
     */
    private var nextChar = ' '

    /**
     * トークンのパース時に使用されるバッファ
     */
    private var parsedTokonBuffer: CharArray

    /**
     * `parsedTokonBuffer`の最後尾のインデックス
     */
    private var bufferIndex = 0

    /**
     * `parsedTokonBuffer`の容量
     */
    private var bufferLength = MINIMUM_CAPACITY

    /**
     * Package-private constructor
     *
     * @param json string of JSON
     */
    init {
        // 上位層で、nullだったら例外を出しているため、
        // 引数がnullであることは考えられない
        checkNotNull(json)

        // char配列に変換する
        jsonChars = json.toCharArray()
        jsonCharsLength = jsonChars.size

        // トークン解析用のバッファを初期化
        // 初期容量は128。128文字を超えるトークンが出現するならばその都度拡張する。
        parsedTokonBuffer = CharArray(MINIMUM_CAPACITY)
    }

    companion object {
        /**
         * `buildEscapedString`の16進数の文字コードをパースする箇所で使用されるバッファ。
         */
        private val bufferForHexadecimalString = StringBuffer()

        // Tokenを都度生成せずに定数として保持する
        /**
         * 左波カッコ'{'のトークン
         */
        private val LBRACE = CubismJsonToken(CubismJsonToken.TokenType.LBRACE)

        /**
         * 右波カッコ'}'のトークン
         */
        private val RBRACE = CubismJsonToken(CubismJsonToken.TokenType.RBRACE)

        /**
         * 左角カッコ'['のトークン
         */
        private val LSQUARE_BRACKET = CubismJsonToken(CubismJsonToken.TokenType.LSQUARE_BRACKET)

        /**
         * 左角カッコ'['のトークン
         */
        private val RSQUARE_BRACKET = CubismJsonToken(CubismJsonToken.TokenType.RSQUARE_BRACKET)

        /**
         * コロン':'のトークン
         */
        private val COLON = CubismJsonToken(CubismJsonToken.TokenType.COLON)

        /**
         * カンマ','のトークン
         */
        private val COMMA = CubismJsonToken(CubismJsonToken.TokenType.COMMA)

        /**
         * 真偽値'true'のトークン
         */
        private val TRUE = CubismJsonToken(true)

        /**
         * 真偽値'false'のトークン
         */
        private val FALSE = CubismJsonToken(false)

        /**
         * 'null'のトークン
         */
        private val NULL = CubismJsonToken()

        // 中の値を書き換えて使用する
        /**
         * 文字列のトークン
         */
        private val STRING = CubismJsonToken("")

        /**
         * 数値のトークン
         */
        private val NUMBER = CubismJsonToken(0.0)

        /**
         * jsonChars配列の初期サイズ。
         * これを超えるトークンが出現した場合はサイズを2倍に拡張する。
         */
        private const val MINIMUM_CAPACITY = 128
    }
}
