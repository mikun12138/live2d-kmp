package com.live2d.sdk.cubism.core

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
