/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.core.Live2DCubismCore
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogError
import java.text.ParseException

/**
 * Moc data manager class
 */
class CubismMoc {
    lateinit var moc: com.live2d.sdk.cubism.core.CubismMoc
    fun initModel(): CubismModel? {
        return moc.initModel()?.let {
            CubismModel().init(it)
        } ?: run {
            // TODO:: fix log
            cubismLogError("failed to init model: $this")
            null
        }
    }

    fun init(mocBytes: ByteArray, shouldCheckMocConsistency: Boolean = false): CubismMoc? {
        if (shouldCheckMocConsistency) {
            // .moc3の整合性を確認する。
            val consistency = Live2DCubismCore.hasMocConsistency(mocBytes)

            if (!consistency) {
                cubismLogError("Inconsistent MOC3.")
                return null
            }
        }

        try {
            moc = com.live2d.sdk.cubism.core.CubismMoc().init(mocBytes)
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }

        return this
    }

    fun close() {
        moc.close()
    }
}
