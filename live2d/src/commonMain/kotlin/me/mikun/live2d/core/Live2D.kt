package me.mikun.live2d.core

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

class CubismCoreVersion internal constructor(val versionNumber: Int) {
    val major: Int = versionNumber ushr 24 and 255
    val minor: Int = versionNumber ushr 16 and 255
    val patch: Int = versionNumber and 255

    override fun toString(): String {
        return buildString {
            append(major.toString().padStart(2, '0'))
            append(".")
            append(minor.toString().padStart(2, '0'))
            append(".")
            append(patch.toString().padStart(4, '0'))
            append(" (")
            append(versionNumber)
            append(")")
        }
    }
}
