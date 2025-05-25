///*
// * Copyright(c) Live2D Inc. All rights reserved.
// *
// * Use of this source code is governed by the Live2D Open Software license
// * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
// */
//package com.live2d.sdk.cubism.framework.utils.jsonparser
//
//class CubismJsonString private constructor(value: String?) : ACubismJsonValue() {
//    override fun getString(defaultValue: String?, indent: String?): String? {
//        return stringBuffer
//    }
//
//    override fun isString(): Boolean {
//        return true
//    }
//
//    override fun equals(o: Any?): Boolean {
//        if (this === o) return true
//        if (o == null || javaClass != o.javaClass) return false
//
//        val that = o as CubismJsonString
//
//        return if (stringBuffer != null) (stringBuffer == that.stringBuffer) else that.stringBuffer == null
//    }
//
//    override fun hashCode(): Int {
//        return if (stringBuffer != null) stringBuffer.hashCode() else 0
//    }
//
//    init {
//        stringBuffer = value
//    }
//
//    companion object {
//        /**
//         * stringのインスタンスを得る
//         *
//         * @return stringのインスタンス
//         *
//         * @throws IllegalArgumentException If the given value is 'null'.
//         */
//        fun valueOf(value: String): CubismJsonString {
//            requireNotNull(value) { "The value is null." }
//            return CubismJsonString(value)
//        }
//    }
//}
