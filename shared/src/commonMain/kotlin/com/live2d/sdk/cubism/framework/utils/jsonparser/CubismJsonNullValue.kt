/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.utils.jsonparser

/**
 * This class expresses a JSON null Value.
 * It has no fields and methods.
 */
internal class CubismJsonNullValue : ACubismJsonValue() {
    init {
        stringBuffer = "NullValue"
    }

    public override fun getString(defaultValue: String?, indent: String?): String? {
        return stringBuffer
    }

    override val isNull: Boolean
        get() = true

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as CubismJsonNullValue

        return if (stringBuffer != null) (stringBuffer == that.stringBuffer) else that.stringBuffer == null
    }

    override fun hashCode(): Int {
        return if (stringBuffer != null) stringBuffer.hashCode() else 0
    }
}
