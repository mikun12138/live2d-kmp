/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.id

/**
 * The name of parameters, parts and Drawable is held in this class.
 */
class CubismId {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val cubismId = o as CubismId

        return this.string == cubismId.string
    }

    override fun hashCode(): Int {
        return string.hashCode()
    }

    /**
     * Constructor
     *
     * @param id A ID name
     * @throws IllegalArgumentException if an argument is null
     */
    internal constructor(id: String) {
        requireNotNull(id) { "id is null." }
        this.string = id
    }

    /**
     * Get ID name
     */
    /**
     * ID name
     */
    val string: String
}
