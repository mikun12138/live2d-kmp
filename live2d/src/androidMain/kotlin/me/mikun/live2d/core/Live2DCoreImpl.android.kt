package me.mikun.live2d.core

actual object Live2DCoreImpl {

    val clazz: Class<*> by lazy {
        Class.forName("com.live2d.sdk.cubism.core.Live2DCubismCoreJNI")
    }

    val clazzCubismParameters: Class<*> by lazy {
        Class.forName("com.live2d.sdk.cubism.core.CubismParameters")
    }

    val clazzCubismModel: Class<*> by lazy {
        Class.forName("com.live2d.sdk.cubism.core.CubismModel")
    }

    private fun <T> justInvoke(funName: String, vararg args: Pair<Class<*>, *>): T {
        val method = clazz.getDeclaredMethod(
            funName,
            *args.map { it.first }.toTypedArray()
        ).apply {
            isAccessible = true
        }
        return method.invoke(
            null,
            *args.map { it.second }.toTypedArray()
        ) as T
    }

    fun CubismParameters.fromCore(): CubismParameters {
        clazzCubismParameters.getDeclaredField("count").also {

        }

    }

    fun CubismModel.toCore(): com.live2d.sdk.cubism.core.CubismModel {
        return com.live2d.sdk.cubism.core.CubismModel().also {
            val field = clazzCubismModel.getDeclaredField("nativeModelHandle")
            field.isAccessible = true
            field.set(it, this.nativeHandle)

            val field1 = clazzCubismModel.getDeclaredField("parameters")
            field1.isAccessible = true
            this.parameters = field1.get(it) as CubismParameters
        }
    }

    actual fun getVersion(): Int {
        return justInvoke("getVersion")
    }

    actual fun getLatestMocVersion(): Int {
        return justInvoke("getLatestMocVersion")
    }

    actual fun getMocVersion(mocData: ByteArray): Int {
        return justInvoke(
            "getMocVersion",
            ByteArray::class.java to mocData
        )
    }

    actual fun hasMocConsistency(mocData: ByteArray): Int {
        return justInvoke(
            "hasMocConsistency",
            ByteArray::class.java to mocData
        )
    }

    actual fun instantiateMoc(mocData: ByteArray): Long {
        return justInvoke(
            "instantiateMoc",
            ByteArray::class.java to mocData
        )
    }

    actual fun destroyMoc(mocHandle: Long) {
        return justInvoke(
            "destroyMoc",
            java.lang.Long.TYPE to mocHandle
        )
    }

    actual fun instantiateModel(mocHandle: Long): Long {
        return justInvoke(
            "instantiateModel",
            java.lang.Long.TYPE to mocHandle
        )
    }

    actual fun destroyModel(modelHandle: Long) {
        return justInvoke(
            "destroyModel",
            java.lang.Long.TYPE to modelHandle
        )
    }

    actual fun updateModel(modelHandle: Long) {
        return justInvoke(
            "updateModel",
            java.lang.Long.TYPE to modelHandle
        )
    }

    actual fun resetDrawableDynamicFlags(modelHandle: Long) {
        return justInvoke(
            "resetDrawableDynamicFlags",
            java.lang.Long.TYPE to modelHandle
        )
    }

    actual fun syncToNativeModel(model: CubismModel) {
//        return justInvoke(
//            "syncToNativeModel",
//            com.live2d.sdk.cubism.core.CubismModel::class.java to model.toCore()
//        )
    }

    actual fun syncFromNativeModel(model: CubismModel) {
//        return justInvoke(
//            "syncFromNativeModel",
//            com.live2d.sdk.cubism.core.CubismModel::class.java to model.toCore()
//        )
    }

    actual fun initializeJavaModelWithNativeModel(model: CubismModel) {
        return justInvoke(
            "initializeJavaModelWithNativeModel",
            com.live2d.sdk.cubism.core.CubismModel::class.java to model.toCore()
        )
    }

    actual fun coreLogFunction(logFunction: LogFunction) {
        // TODO::
        println("me.mikun.live2d.core.Live2DCoreImpl.coreLogFunction called")
    }

}
