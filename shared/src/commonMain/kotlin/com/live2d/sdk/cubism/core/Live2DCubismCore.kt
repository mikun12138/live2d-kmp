package com.live2d.sdk.cubism.core

object Live2DCubismCore {
    var logger: ICubismLogger? = null
    val version: CubismCoreVersion by lazy {
        CubismCoreVersion(Live2DCubismCoreImpl.getVersion())
    }
    var latestMocVersion: Int = -1
        get() {
            if (field < 0) {
                field = Live2DCubismCoreImpl.getLatestMocVersion()
            }

            return field
        }
        private set

    fun getMocVersion(mocBinary: ByteArray): Int {
        return Live2DCubismCoreImpl.getMocVersion(mocBinary)
    }

    fun hasMocConsistency(mocBinary: ByteArray): Boolean {
        val isValid: Int = Live2DCubismCoreImpl.hasMocConsistency(mocBinary)
        return isValid != 0
    }

    object MocVersion {
        const val UNKNOWN: Int = 0
        const val V30: Int = 1
        const val V33: Int = 2
        const val V40: Int = 3
        const val V42: Int = 4
        const val V50: Int = 5
    }
}
