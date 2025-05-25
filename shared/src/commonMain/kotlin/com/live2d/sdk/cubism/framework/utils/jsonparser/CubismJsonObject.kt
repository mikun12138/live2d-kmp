///*
// * Copyright(c) Live2D Inc. All rights reserved.
// *
// * Use of this source code is governed by the Live2D Open Software license
// * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
// */
//package com.live2d.sdk.cubism.framework.utils.jsonparser
//
///**
// * This class expresses JSON Object.
// * If duplicate key is put, an error is not returned. However, the value corresponding duplicate key is overridden with the value corresponding the key which is defined later.
// */
//internal class CubismJsonObject : ACubismJsonValue() {
//    /**
//     * Put a pair of string and value
//     *
//     * @param string key(string)
//     * @param value value
//     */
//    fun putPair(string: CubismJsonString, value: ACubismJsonValue) {
//        this.map.put(string, value)
//
//        // Add to key list
//        keys.add(string)
//    }
//
//    override fun get(key: String?): ACubismJsonValue {
//        val str: CubismJsonString? = CubismJsonString.valueOf(key!!)
//        val value = this.map.get(str)
//
//        if (value == null) {
//            return CubismJsonNullValue()
//        }
//
//        return value
//    }
//
//    override fun get(index: Int): ACubismJsonValue {
//        return CubismJsonErrorValue().setErrorNotForClientCall(JsonError.TYPE_MISMATCH.message)
//    }
//
//    override fun getString(defaultValue: String?, indent: String?): String? {
//        // バッファをクリアする
//        bufferForGetString.delete(0, bufferForGetString.length)
//
//        bufferForGetString.append(indent)
//        bufferForGetString.append("{\n")
//
//        for (i in keys.indices) {
//            val key = keys.get(i)
//            val value = checkNotNull(this.map.get(key))
//
//            bufferForGetString.append(indent)
//            bufferForGetString.append(" ")
//            bufferForGetString.append(key)
//            bufferForGetString.append(" : ")
//            bufferForGetString.append(value.getString(indent + " "))
//            bufferForGetString.append("\n")
//        }
//
//        bufferForGetString.append(indent)
//        bufferForGetString.append("}\n")
//
//        stringBuffer = bufferForGetString.toString()
//        return stringBuffer
//    }
//
//
//    override fun size(): Int {
//        return map.size
//    }
//
//    override val isObject: Boolean
//        get() = true
//
//    override fun equals(o: Any?): Boolean {
//        if (this === o) return true
//        if (o !is CubismJsonObject) return false
//
//        val that = o
//
//        if (this.map != that.map) return false
//        return keys == that.keys
//    }
//
//    override fun hashCode(): Int {
//        var result = map.hashCode()
//        result = 31 * result + keys.hashCode()
//        return result
//    }
//
//    /**
//     * JSON Object value(map of JSON String and JSON Value)
//     */
//    override val map: MutableMap<CubismJsonString, ACubismJsonValue> =
//        HashMap()
//
//    /**
//     * List of keys to speed up access to the specified index
//     */
//    override val keys: MutableList<CubismJsonString> = ArrayList<CubismJsonString>()
//
//    /**
//     * `getString`で使用される文字列バッファ
//     */
//    private val bufferForGetString = StringBuffer(MINIMUM_CAPACITY)
//
//    companion object {
//        /**
//         * `getString`メソッドで使われる一時的な文字列バッファの最小容量。
//         */
//        private const val MINIMUM_CAPACITY = 128
//    }
//}
