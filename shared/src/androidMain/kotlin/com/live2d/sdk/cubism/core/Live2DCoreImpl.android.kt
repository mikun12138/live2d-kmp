package com.live2d.sdk.cubism.core

actual object Live2DCoreImpl {

    actual fun getVersion(): Int {
        TODO()
    }

    actual fun getLatestMocVersion(): Int {
        TODO()
    }

    actual fun getMocVersion(mocData: ByteArray): Int {
        TODO()
    }

    actual fun hasMocConsistency(mocData: ByteArray): Int {
        TODO()
    }

    actual fun instantiateMoc(mocData: ByteArray): Long {
        TODO()
    }

    actual fun destroyMoc(mocHandle: Long) {
        TODO()
    }

    actual fun instantiateModel(mocHandle: Long): Long {
        TODO()
    }

    actual fun destroyModel(modelHandle: Long) {
        TODO()
    }

    actual fun updateModel(modelHandle: Long) {
        TODO()
    }

    actual fun resetDrawableDynamicFlags(modelHandle: Long) {
        TODO()
    }


    actual fun syncToNativeModel(model: CubismModel) {
        TODO()
    }

    actual fun syncFromNativeModel(model: CubismModel) {
        TODO()
    }

    actual fun initializeJavaModelWithNativeModel(model: CubismModel) {
        TODO()
    }

    actual fun coreLogFunction(logFunction: LogFunction) {
        TODO()
    }

}
