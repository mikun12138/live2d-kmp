package com.live2d.sdk.cubism.core

class CubismModel {
    val parameterViews: Array<CubismParameterView>
    val partViews: Array<CubismPartView>
    val drawableViews: Array<CubismDrawableView>

    lateinit var canvasInfo: CubismCanvasInfo
        private set
    lateinit var parameters: CubismParameters
        private set
    lateinit var parts: CubismParts
        private set
    lateinit var drawables: CubismDrawables
        private set

    val nativeHandle: Long

    internal constructor(nativeHandle: Long) {
        this.nativeHandle = nativeHandle

        Live2DCubismCoreImpl.initializeJavaModelWithNativeModel(this)

        this.parameterViews = Array(this.parameters.count) {
            CubismParameterView(it, this.parameters)
        }
        this.partViews = Array(this.parts.count) {
            CubismPartView(it, this.parts)
        }
        this.drawableViews = Array(this.drawables.count) {
            CubismDrawableView(it, this.drawables)
        }
    }


    fun update() {
        this.throwIfAlreadyReleased()
        Live2DCubismCoreImpl.syncToNativeModel(this)
        Live2DCubismCoreImpl.updateModel(this.nativeHandle)
        Live2DCubismCoreImpl.syncFromNativeModel(this)
    }

    fun resetDrawableDynamicFlags() {
        this.throwIfAlreadyReleased()
        Live2DCubismCoreImpl.resetDrawableDynamicFlags(this.nativeHandle)
    }


    fun findParameterView(id: String): CubismParameterView? {
        repeat(this.parameters.count) {
            if (this.parameters.ids[it].equals(id)) {
                return this.parameterViews[it]
            }
        }
        return null
    }

    fun findPartView(id: String): CubismPartView? {
        repeat(this.parts.count) {
            if (this.parts.ids[it].equals(id)) {
                return this.partViews[it]
            }
        }
        return null
    }

    fun findDrawableView(id: String): CubismDrawableView? {
        repeat(this.drawables.count) {
            if (this.drawables.ids[it].equals(id)) {
                return this.drawableViews[it]
            }
        }
        return null
    }

    private fun throwIfAlreadyReleased() {
        check(this.nativeHandle != 0L) { "This Model is Already Released." }
    }
}
