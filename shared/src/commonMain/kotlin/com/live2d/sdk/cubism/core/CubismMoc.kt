package com.live2d.sdk.cubism.core

import java.text.ParseException

class CubismMoc private constructor() : AutoCloseable {
    val models: MutableList<CubismModel?> = ArrayList()
    var nativeHandle: Long = 0
        private set

    override fun close() {
        if (this.nativeHandle != 0L) {
            check(this.models.isEmpty()) { "Instantiated models are not destroyed yet!!" }
            Live2DCubismCoreImpl.destroyMoc(this.nativeHandle)
            this.nativeHandle = 0L
        }
    }

    fun instantiateModel(): CubismModel? {
        this.throwIfAlreadyReleased()
        val model: CubismModel? = CubismModel.instantiateModel(this)
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

    companion object {
        @Throws(ParseException::class)
        fun instantiate(mocBinary: ByteArray): CubismMoc {
            val mocHandle: Long = Live2DCubismCoreImpl.instantiateMoc(mocBinary)
            if (mocHandle == 0L) {
                throw ParseException("moc data is Invalid.", 0)
            } else {
                val moc = CubismMoc()
                moc.nativeHandle = mocHandle
                return moc
            }
        }
    }
}
