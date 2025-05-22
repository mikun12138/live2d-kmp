package com.live2d.sdk.cubism.core

actual object Live2DCubismCoreImpl {

    actual fun getVersion(): Int = 0

    actual fun getLatestMocVersion(): Int {
        TODO("Not yet implemented")
    }

    actual fun getMocVersion(mocData: ByteArray): Int {
        TODO("Not yet implemented")
    }

    actual fun hasMocConsistency(mocData: ByteArray): Int {
        TODO("Not yet implemented")
    }

    actual fun instantiateMoc(mocData: ByteArray): Long {
        TODO("Not yet implemented")
    }

    actual fun destroyMoc(mocHandle: Long) {
        TODO("Not yet implemented")
    }

    actual fun instantiateModel(mocHandle: Long): Long {
        TODO("Not yet implemented")
    }

    actual fun destroyModel(modelHandle: Long) {
        TODO("Not yet implemented")
    }

    actual fun updateModel(modelHandle: Long) {
        TODO("Not yet implemented")
    }

    actual fun resetDrawableDynamicFlags(modelHandle: Long) {
        TODO("Not yet implemented")
    }

    actual fun syncToNativeModel(model: CubismModel) {
        TODO("Not yet implemented")
    }

    actual fun syncFromNativeModel(model: CubismModel) {
        TODO("Not yet implemented")
    }

    actual fun initializeJavaModelWithNativeModel(model: CubismModel) {
        TODO("Not yet implemented")
    }
}

