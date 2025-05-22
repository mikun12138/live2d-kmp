package com.live2d.sdk.cubism.core

class CubismModel {
    lateinit var parameterViews: Array<CubismParameterView>
        private set
    lateinit var partViews: Array<CubismPartView>
        private set
    lateinit var drawableViews: Array<CubismDrawableView>
        private set

    val canvasInfo: CubismCanvasInfo = null!!
    val parameters: CubismParameters = null!!
    val parts: CubismParts = null!!
    val drawables: CubismDrawables = null!!
    private lateinit var moc: CubismMoc
    var nativeHandle: Long = 0
        private set

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

    fun close() {
        if (this.nativeHandle != 0L) {
            Live2DCubismCoreImpl.destroyModel(this.nativeHandle)
            this.nativeHandle = 0L
            this.moc.deleteAssociation(this)
        }
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

    private fun initialize() {
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

    private fun throwIfAlreadyReleased() {
        check(this.nativeHandle != 0L) { "This Model is Already Released." }
    }

    companion object {
        fun instantiateModel(moc: CubismMoc): CubismModel {
            requireNotNull(moc != null) { "moc is null" }
            require(moc.nativeHandle != 0L) { "moc is already released." }
            val modelHandle: Long = Live2DCubismCoreImpl.instantiateModel(moc.nativeHandle)
            check(modelHandle != 0L) { "Instantiate model is failed." }
            val model = CubismModel()
            model.nativeHandle = modelHandle
            model.moc = moc
            model.initialize()
            return model
        }
    }
}
