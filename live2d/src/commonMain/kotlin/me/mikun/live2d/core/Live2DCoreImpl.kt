package me.mikun.live2d.core

expect object Live2DCoreImpl {

    fun getVersion(): Int

    fun getLatestMocVersion(): Int

    fun getMocVersion(mocData: ByteArray): Int

    fun hasMocConsistency(mocData: ByteArray): Int

    fun instantiateMoc(mocData: ByteArray): Long

    fun destroyMoc(mocHandle: Long)

    fun instantiateModel(mocHandle: Long): Long

    fun destroyModel(modelHandle: Long)

    fun updateModel(modelHandle: Long)

    fun resetDrawableDynamicFlags(modelHandle: Long)

    fun syncToNativeModel(model: CubismModel)

    fun syncFromNativeModel(model: CubismModel)

    fun initializeJavaModelWithNativeModel(model: CubismModel)

    fun coreLogFunction(logFunction: LogFunction)

}

