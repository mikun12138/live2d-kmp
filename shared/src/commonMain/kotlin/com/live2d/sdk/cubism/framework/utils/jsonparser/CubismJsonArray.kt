/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.utils.jsonparser

/**
 * This class expresses JSON Array
 */
internal class CubismJsonArray : ACubismJsonValue() {
    /**
     * Add a JSON Value.
     *
     * @param value JSON value
     */
    fun putValue(value: ACubismJsonValue?) {
        this.list.add(value)
    }

    override fun get(key: String?): ACubismJsonValue {
        return CubismJsonErrorValue().setErrorNotForClientCall(JsonError.TYPE_MISMATCH.message)
    }

    override fun get(index: Int): ACubismJsonValue {
        if (index < 0 || list.size <= index) {
            return CubismJsonErrorValue().setErrorNotForClientCall(JsonError.INDEX_OUT_OF_BOUNDS.message)
        }

        val value = this.list.get(index)

        if (value == null) {
            return CubismJsonNullValue()
        }

        return value
    }

    override fun getString(defaultValue: String?, indent: String?): String? {
        // バッファに格納されている文字列を空にする
        bufferForGetString.delete(0, bufferForGetString.length)

        bufferForGetString.append(indent)
        bufferForGetString.append("[\n")

        for (i in list.indices) {
            bufferForGetString.append(indent)
            bufferForGetString.append(" ")
            bufferForGetString.append(list.get(i)!!.getString(indent + " "))
            bufferForGetString.append("\n")
        }

        bufferForGetString.append(indent)
        bufferForGetString.append("]\n")

        stringBuffer = bufferForGetString.toString()
        return stringBuffer
    }

    override fun size(): Int {
        return list.size
    }

    override val isArray: Boolean
        get() = true

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as CubismJsonArray

        return this.list == that.list
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }

    /**
     * JSON Array value
     */
    override val list: MutableList<ACubismJsonValue?> = ArrayList()

    /**
     * `getString`で使用される文字列バッファ
     */
    private val bufferForGetString = StringBuffer(MINIMUM_CAPACITY)

    companion object {
        /**
         * `getString`メソッドで使われる一時的な文字列バッファの最小容量。
         */
        private const val MINIMUM_CAPACITY = 128
    }
}
