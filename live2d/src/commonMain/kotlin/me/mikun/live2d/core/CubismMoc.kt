package me.mikun.live2d.core

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

        val mocHandle: Long = Live2DCoreImpl.instantiateMoc(mocBinary)
        if (mocHandle == 0L) {
            error("moc data is Invalid.")
        } else {
            nativeHandle = mocHandle
        }
    }

    fun hasMocConsistency(mocBinary: ByteArray): Boolean {
        val isValid: Int = Live2DCoreImpl.hasMocConsistency(mocBinary)
        return isValid != 0
    }

    fun instantiateModel(): CubismModel {
        return CubismModel(
            Live2DCoreImpl.instantiateModel(nativeHandle)
        )
    }
}
