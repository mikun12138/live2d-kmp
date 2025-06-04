package com.live2d.sdk.cubism.core

class CubismMoc {
    var nativeHandle: Long = 0
        private set

    constructor(mocBinary: ByteArray, checkMocConsistency: Boolean = false) {
        if (checkMocConsistency) {
            // .moc3の整合性を確認する。
            val consistency = hasMocConsistency(mocBinary)

            if (!consistency) {
                error("Inconsistent MOC3.")
            }
        }

        val mocHandle: Long = Live2DCubismCoreImpl.instantiateMoc(mocBinary)
        if (mocHandle == 0L) {
            error("moc data is Invalid.")
        } else {
            nativeHandle = mocHandle
        }
    }

    fun hasMocConsistency(mocBinary: ByteArray): Boolean {
        val isValid: Int = Live2DCubismCoreImpl.hasMocConsistency(mocBinary)
        return isValid != 0
    }

    fun instantiateModel(): CubismModel {
        return CubismModel(
            Live2DCubismCoreImpl.instantiateModel(nativeHandle)
        )
    }
}
