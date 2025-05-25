///*
// * Copyright(c) Live2D Inc. All rights reserved.
// *
// * Use of this source code is governed by the Live2D Open Software license
// * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
// */
//package com.live2d.sdk.cubism.framework.utils.jsonparser
//
///**
// * This class expresses a boolean value.
// */
//internal class CubismJsonBoolean private constructor(
//    /**
//     * A boolean value
//     */
//    private val value: Boolean
//) : ACubismJsonValue() {
//    override fun getString(defaultValue: String?, indent: String?): String? {
//        return stringBuffer
//    }
//
//    override fun toBoolean(): Boolean {
//        return value
//    }
//
//    override fun toBoolean(defaultValue: Boolean): Boolean {
//        return value
//    }
//
//    override val isBoolean: Boolean
//        get() = true
//
//    override fun equals(o: Any?): Boolean {
//        if (this === o) return true
//        if (o == null || javaClass != o.javaClass) return false
//
//        val that = o as CubismJsonBoolean
//
//        return value == that.value
//    }
//
//    override fun hashCode(): Int {
//        return (if (value) 1 else 0)
//    }
//
//    /**
//     * Private constructor
//     *
//     * @param value a boolean value
//     */
//    init {
//        stringBuffer = value.toString()
//    }
//
//    companion object {
//        /**
//         * Get a boolean instance
//         *
//         * @param value a boolean value
//         * @return a JSON boolean value
//         */
//        fun valueOf(value: Boolean): CubismJsonBoolean {
//            return CubismJsonBoolean(value)
//        }
//    }
//}
