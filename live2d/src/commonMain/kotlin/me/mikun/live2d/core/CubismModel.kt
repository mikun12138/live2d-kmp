package me.mikun.live2d.core

class CubismModel {

    lateinit var canvasInfo: CubismCanvasInfo
        internal set
    lateinit var parameters: CubismParameters
        internal set
    lateinit var parts: CubismParts
        internal set
    lateinit var drawables: CubismDrawables
        internal set

    internal val nativeHandle: Long

    internal constructor(nativeHandle: Long) {
        this.nativeHandle = nativeHandle

        Live2DCoreImpl.initializeJavaModelWithNativeModel(this)
    }


    fun update() {
        this.throwIfAlreadyReleased()
        Live2DCoreImpl.syncToNativeModel(this)
        Live2DCoreImpl.updateModel(this.nativeHandle)
        Live2DCoreImpl.syncFromNativeModel(this)
    }

    fun resetDrawableDynamicFlags() {
        this.throwIfAlreadyReleased()
        Live2DCoreImpl.resetDrawableDynamicFlags(this.nativeHandle)
    }

    private fun throwIfAlreadyReleased() {
        check(this.nativeHandle != 0L) { "This Model is Already Released." }
    }
}
