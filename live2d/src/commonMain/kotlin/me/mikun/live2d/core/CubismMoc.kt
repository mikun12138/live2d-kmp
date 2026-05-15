package me.mikun.live2d.core

class CubismMoc {
    internal var nativeHandle: Long = 0
        private set

    internal constructor(mocBinary: ByteArray, checkMocConsistency: Boolean = true) {
        if (checkMocConsistency) {
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

    private fun hasMocConsistency(mocBinary: ByteArray): Boolean {
        val isValid: Int = Live2DCoreImpl.hasMocConsistency(mocBinary)
        return isValid != 0
    }

    internal fun instantiateModel(): CubismModel {
        return CubismModel(
            Live2DCoreImpl.instantiateModel(nativeHandle)
        )
    }
}
