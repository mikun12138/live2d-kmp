/*
 *
 *  * Copyright(c) Live2D Inc. All rights reserved.
 *  *
 *  * Use of this source code is governed by the Live2D Open Software license
 *  * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 *
 */
package com.live2d.sdk.cubism.framework.utils.jsonparser

internal class CubismJsonErrorValue : ACubismJsonValue() {
    override fun getString(defaultValue: String?, indent: String?): String? {
        return stringBuffer
    }

    override val isError: Boolean
        get() = true

    override fun setErrorNotForClientCall(s: String?): ACubismJsonValue {
        this.stringBuffer = s
        return this
    }
}
