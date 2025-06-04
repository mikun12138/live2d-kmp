package com.live2d.sdk.cubism.core

object Live2D {
    val coreVersion: CubismCoreVersion by lazy {
        CubismCoreVersion(Live2DCoreImpl.getVersion())
    }
    val latestMocVersion: Int by lazy {
        Live2DCoreImpl.getLatestMocVersion()
    }

    fun coreLogFunction(logFunction: LogFunction) {
        Live2DCoreImpl.coreLogFunction(logFunction)
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
