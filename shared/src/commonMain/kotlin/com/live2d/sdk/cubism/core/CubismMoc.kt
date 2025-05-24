package com.live2d.sdk.cubism.core

import java.text.ParseException

class CubismMoc {
    var nativeHandle: Long = 0
        private set
    val models: MutableList<CubismModel?> = mutableListOf()

    fun init(mocBinary: ByteArray): CubismMoc {
        val mocHandle: Long = Live2DCubismCoreImpl.instantiateMoc(mocBinary)
        if (mocHandle == 0L) {
            throw ParseException("moc data is Invalid.", 0)
        } else {
            nativeHandle = mocHandle
        }
        return this
    }

    fun close() {
        if (this.nativeHandle != 0L) {
            check(this.models.isEmpty()) { "Instantiated models are not destroyed yet!!" }
            Live2DCubismCoreImpl.destroyMoc(this.nativeHandle)
            this.nativeHandle = 0L
        }
    }

    fun initModel(): CubismModel? {
        this.throwIfAlreadyReleased()
        val model = CubismModel().init(this)
        if (model == null) {
            return null
        } else {
            this.models.add(model)
            return model
        }
    }

    fun deleteAssociation(model: CubismModel?) {
        this.models.remove(model)
    }

    private fun throwIfAlreadyReleased() {
        check(this.nativeHandle != 0L) { "This Model is Already Released." }
    }
}
