package com.live2d.sdk.cubism.framework.utils.jsonparser

abstract class ACubismJsonValue {
    /**
     * If the class implemented this interface is `CubismJsonObject`, returns the value corresponding to the key.
     *
     *
     * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
     *
     * @param key the key of the JSON Object
     * @return the value of the JSON Object
     */
    open fun get(key: String?): ACubismJsonValue {
        val nullValue: ACubismJsonValue =
            CubismJsonNullValue()
        nullValue.setErrorNotForClientCall(JsonError.TYPE_MISMATCH.message)

        return nullValue
    }

    /**
     * If the class implemented this interface is `CubismJsonArray`, returns the value corresponding to the index of the argument.
     *
     *
     * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
     *
     * @param index index of the element to return
     * @return the value corresponding to the index
     */
    open fun get(index: Int): ACubismJsonValue {
        val errorValue: ACubismJsonValue =
            CubismJsonErrorValue()
        errorValue.setErrorNotForClientCall(JsonError.TYPE_MISMATCH.message)

        return errorValue
    }

    /**
     * Returns the JSON Value's string expression.
     *
     * @return the JSON Value's string expression
     */
    abstract fun getString(defaultValue: String?, indent: String?): String?

    fun getString(defaultValue: String?): String? {
        return getString(defaultValue, "")
    }

    val string: String?
        get() = getString("", "")

    open val map: MutableMap<CubismJsonString, ACubismJsonValue>?
        /**
         * If the class implemented this interface is `CubismJsonObject`, returns the map of `CubismJsonString` and `ICubismJsonValue`.
         *
         *
         * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
         *
         * @return the map of strings and values
         */
        get() = null

    open val list: MutableList<ACubismJsonValue?>
        /**
         * If the class implemented this interface is `CubismJsonArray`, returns the list of `ICubismJsonValue`.
         *
         *
         * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
         *
         * @return the list of `ICubismJsonValue`
         */
        get() = mutableListOf()

    open val keys: MutableList<CubismJsonString>
        /**
         * If the class implemented this interface is `CubismJsonObject`, returns the set of the keys(`CubismJsonString`).
         *
         *
         * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
         *
         * @return the set of the keys(`CubismJsonString`)
         */
        get() = mutableListOf()

    /**
     * If the class implemented this interface is `CubismJsonObject` and `CubismJsonArray`, returns the number of JSON Value that they hold.
     *
     *
     * If this is implemented in `CubismJsonString`, returns the length of the string.
     *
     *
     * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
     *
     * @return the number of JSON Value that they hold or the length of the string
     */
    open fun size(): Int {
        return 0
    }

    /**
     * If the class implemented this interface is `CubismJsonNumber`, returns the value cast to an integer value.
     *
     *
     * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
     *
     * @return the value cast to an integer value
     */
    open fun toInt(): Int {
        return 0
    }

    /**
     * If the class implemented this interface is `CubismJsonNumber`, returns the value cast to a float value.
     *
     *
     * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
     *
     * @return the value cast to an float value
     */
    open fun toFloat(): Float {
        return 0.0f
    }

    /**
     * If the class implemented this interface is `CubismJsonBoolean`, returns the boolean value it holds
     *
     *
     * If this is implemented in other concrete class, `UnsupportedOperationException` is thrown.
     *
     * @return the boolean value that the class has
     */
    open fun toBoolean(): Boolean {
        return false
    }

    open val isError: Boolean
        get() = false

    open val isNull: Boolean
        /**
         * If the class implemented this interface is `CubismJsonNullValue`, returns `true`.
         *
         *
         * If this is implemented in other concrete class, returns `false`.
         *
         * @return If this JSON Value is Null Value, returns true
         */
        get() = false

    /**
     * Valueにエラー値をセットする。
     *
     * @param errorMsg エラーメッセージ
     * @return JSON Null Value
     */
    open fun setErrorNotForClientCall(errorMsg: String?): ACubismJsonValue {
        stringBuffer = errorMsg
        return CubismJsonNullValue()
    }

    /**
     * エラーメッセージ定義
     */
    protected enum class JsonError(val message: String) {
        TYPE_MISMATCH("Error: type mismatch"),
        INDEX_OUT_OF_BOUNDS("Error: index out of bounds");

    }

    protected var stringBuffer: String? = null
}
