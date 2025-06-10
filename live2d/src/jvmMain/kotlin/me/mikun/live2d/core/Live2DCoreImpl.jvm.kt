package me.mikun.live2d.core

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import kotlin.text.set

actual object Live2DCoreImpl {

    actual fun getVersion(): Int {
        return Live2DCubismCoreFFM.getVersion()
    }

    actual fun getLatestMocVersion(): Int {
        return Live2DCubismCoreFFM.getLatestMocVersion()
    }

    actual fun getMocVersion(mocData: ByteArray): Int {
        return Live2DCubismCoreFFM.csmGetMocVersion(mocData)
    }

    actual fun hasMocConsistency(mocData: ByteArray): Int {
        return Live2DCubismCoreFFM.csmHasMocConsistency(mocData)
    }

    actual fun instantiateMoc(mocData: ByteArray): Long {
        return Live2DCubismCoreFFM.csmReviveMocInPlace(mocData)
    }

    actual fun destroyMoc(mocHandle: Long) {
        // TODO::
    }

    actual fun instantiateModel(mocHandle: Long): Long {
        return Live2DCubismCoreFFM.csmInitializeModelInPlace(
            mocHandle,
            Live2DCubismCoreFFM.csmGetSizeofModel(mocHandle)
        )
    }

    actual fun destroyModel(modelHandle: Long) {
        // TODO::
    }

    actual fun updateModel(modelHandle: Long) {
        Live2DCubismCoreFFM.csmUpdateModel(modelHandle)
    }

    actual fun resetDrawableDynamicFlags(modelHandle: Long) {
        Live2DCubismCoreFFM.csmResetDrawableDynamicFlags.invoke(modelHandle)
    }


    actual fun syncToNativeModel(model: CubismModel) {
        run {
            val nativeParamSegment =
                Live2DCubismCoreFFM.csmGetParameterValues(model.nativeHandle).reinterpret(
                    Live2DCubismCoreFFM.csmGetParameterCount(model.nativeHandle) * ValueLayout.JAVA_FLOAT.byteSize()
                )
            MemorySegment.copy(
                model.parameters.values, 0,
                nativeParamSegment, ValueLayout.JAVA_FLOAT, 0,
                model.parameters.values.size
            )
        }

        run {
            val nativeParamSegment =
                Live2DCubismCoreFFM.csmGetPartOpacities(model.nativeHandle).reinterpret(
                    Live2DCubismCoreFFM.csmGetPartCount(model.nativeHandle) * ValueLayout.JAVA_FLOAT.byteSize()
                )
            MemorySegment.copy(
                model.parts.opacities, 0,
                nativeParamSegment, ValueLayout.JAVA_FLOAT, 0,
                model.parts.opacities.size
            )
        }
    }

    actual fun syncFromNativeModel(model: CubismModel) {
        Live2DCubismCoreFFM.csmGetParameterCount(model.nativeHandle).let { parameterCount ->
            Live2DCubismCoreFFM.csmGetParameterValues(model.nativeHandle).also { values ->
                val values =
                    values.reinterpret(parameterCount * ValueLayout.JAVA_FLOAT.byteSize())
                for (i in 0 until parameterCount) {
                    model.parameters.values[i] =
                        values.getAtIndex(
                            ValueLayout.JAVA_FLOAT,
                            i.toLong()
                        )
                }
            }
        }

        Live2DCubismCoreFFM.csmGetPartCount(model.nativeHandle).let { parameterCount ->
            Live2DCubismCoreFFM.csmGetPartOpacities(model.nativeHandle).also { values ->
                val values =
                    values.reinterpret(parameterCount * ValueLayout.JAVA_FLOAT.byteSize())
                for (i in 0 until parameterCount) {
                    model.parts.opacities[i] =
                        values.getAtIndex(
                            ValueLayout.JAVA_FLOAT,
                            i.toLong()
                        )
                }
            }
        }

        Live2DCubismCoreFFM.csmGetDrawableCount(model.nativeHandle).let { drawableCount ->

            Live2DCubismCoreFFM.csmGetDrawableConstantFlags(model.nativeHandle).also { flags ->
                val flags = flags.reinterpret(drawableCount * ValueLayout.JAVA_BYTE.byteSize())
                for (i in 0 until drawableCount) {
                    model.drawables.constantFlags[i] =
                        flags.getAtIndex(
                            ValueLayout.JAVA_BYTE,
                            i.toLong()
                        )
                }
            }

            Live2DCubismCoreFFM.csmGetDrawableDynamicFlags(model.nativeHandle).also { flags ->
                val flags = flags.reinterpret(drawableCount * ValueLayout.JAVA_BYTE.byteSize())
                for (i in 0 until drawableCount) {
                    model.drawables.dynamicFlags[i] =
                        flags.getAtIndex(
                            ValueLayout.JAVA_BYTE,
                            i.toLong()
                        )
                }
            }

            Live2DCubismCoreFFM.csmGetDrawableDrawOrders(model.nativeHandle)
                .also { drawOrders ->
                    val textureIndices =
                        drawOrders.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                    for (i in 0 until drawableCount) {
                        model.drawables.drawOrders[i] =
                            textureIndices.getAtIndex(
                                ValueLayout.JAVA_INT,
                                i.toLong()
                            )
                    }
                }

            Live2DCubismCoreFFM.csmGetDrawableRenderOrders(model.nativeHandle)
                .also { renderOrders ->
                    val textureIndices =
                        renderOrders.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                    for (i in 0 until drawableCount) {
                        model.drawables.renderOrders[i] =
                            textureIndices.getAtIndex(
                                ValueLayout.JAVA_INT,
                                i.toLong()
                            )
                    }
                }

            Live2DCubismCoreFFM.csmGetDrawableOpacities(model.nativeHandle).also { opacities ->
                val textureIndices =
                    opacities.reinterpret(drawableCount * ValueLayout.JAVA_FLOAT.byteSize())
                for (i in 0 until drawableCount) {
                    model.drawables.opacities[i] =
                        textureIndices.getAtIndex(
                            ValueLayout.JAVA_FLOAT,
                            i.toLong()
                        )
                }

            }

            val vertexCountsList = mutableListOf<Int>()
            Live2DCubismCoreFFM.csmGetDrawableVertexCounts(model.nativeHandle)
                .also { vertexCounts ->
                    val vertexCounts =
                        vertexCounts.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                    for (i in 0 until drawableCount) {
                        model.drawables.vertexCounts[i] =
                            vertexCounts.getAtIndex(
                                ValueLayout.JAVA_INT,
                                i.toLong()
                            ).also {
                                vertexCountsList.add(it)
                            }
                    }
                }


            Live2DCubismCoreFFM.csmGetDrawableVertexPositions(model.nativeHandle)
                .also { vertexPositions ->
                    val m0 =
                        vertexPositions.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until drawableCount) {
                        val vertexPosition = m0.getAtIndex(
                            ValueLayout.ADDRESS,
                            i.toLong()
                        )

                        vertexPosition.reinterpret(vertexCountsList.get(i) * ValueLayout.JAVA_FLOAT.byteSize() * 2)
                            .let {
                                model.drawables.vertexPositions[i] =
                                    FloatArray(size = vertexCountsList.get(i) * 2)
                                for (j in 0 until vertexCountsList.get(i)) {
                                    model.drawables.vertexPositions[i]!![j * 2] =
                                        it.getAtIndex(
                                            ValueLayout.JAVA_FLOAT,
                                            j * 2L
                                        )
                                    model.drawables.vertexPositions[i]!![j * 2 + 1] =
                                        it.getAtIndex(
                                            ValueLayout.JAVA_FLOAT,
                                            j * 2L + 1L
                                        )
                                }
                            }
                    }
                }

            Live2DCubismCoreFFM.csmGetDrawableMultiplyColors(model.nativeHandle)
                .also { multiplyColors ->
                    val multiplyColors =
                        multiplyColors.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize() * 4)
                    for (i in 0 until drawableCount) {
                        model.drawables.multiplyColors[i] = FloatArray(size = 4)
                        for (j in 0 until 4) {
                            model.drawables.multiplyColors[i]!![j] = multiplyColors.getAtIndex(
                                ValueLayout.JAVA_FLOAT,
                                i * 4 + j.toLong()
                            )
                        }
                    }
                }
        }
    }

    actual fun initializeJavaModelWithNativeModel(model: CubismModel) {
        val outSizeInPixels = Arena.global().allocate(2 * ValueLayout.JAVA_FLOAT.byteSize())
        val outOriginInPixels = Arena.global().allocate(2 * ValueLayout.JAVA_FLOAT.byteSize())
        val outPixelsPerUnit = Arena.global().allocate(1 * ValueLayout.JAVA_FLOAT.byteSize())

        Live2DCubismCoreFFM.csmReadCanvasInfo(
            model.nativeHandle,
            outSizeInPixels,
            outOriginInPixels,
            outPixelsPerUnit
        )

        model::class.java.getDeclaredField("canvasInfo").apply {
            isAccessible = true
            set(
                model,
                CubismCanvasInfo(
                    floatArrayOf(
                        outSizeInPixels.getAtIndex(
                            ValueLayout.JAVA_FLOAT,
                            0
                        ),
                        outSizeInPixels.getAtIndex(
                            ValueLayout.JAVA_FLOAT,
                            1
                        )
                    ),
                    floatArrayOf(
                        outOriginInPixels.getAtIndex(
                            ValueLayout.JAVA_FLOAT,
                            0
                        ),
                        outOriginInPixels.getAtIndex(
                            ValueLayout.JAVA_FLOAT,
                            1
                        )
                    ),
                    outPixelsPerUnit.getAtIndex(
                        ValueLayout.JAVA_FLOAT,
                        0
                    )
                )
            )
        }

//            print(
//                """
//                    canvasInfo: {
//                        sizeInPixels: ${model.canvasInfo.sizeInPixels.toList()}
//                        originInPixels: ${model.canvasInfo.originInPixels.toList()}
//                        pixelsPerUnit: ${model.canvasInfo.pixelsPerUnit}
//                    }
//                """.trimIndent()
//            )

        Live2DCubismCoreFFM.csmGetParameterCount(model.nativeHandle).let { parameterCount ->
            CubismParameters(parameterCount).also { cubismParameters ->

                Live2DCubismCoreFFM.csmGetParameterIds(model.nativeHandle).also { ids ->
                    val m0 = ids.reinterpret(parameterCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until parameterCount) {
                        cubismParameters.ids[i] =
                            m0.getAtIndex(
                                ValueLayout.ADDRESS,
                                i.toLong()
                            ).reinterpret(Long.MAX_VALUE).getUtf8String(0)
                    }
                }

                Live2DCubismCoreFFM.csmGetParameterTypes(model.nativeHandle).also { types ->
                    val types = types.reinterpret(parameterCount * ValueLayout.JAVA_INT.byteSize())
                    for (i in 0 until parameterCount) {
                        cubismParameters.types[i] =
                            CubismParameters.ParameterType.toType(
                                types.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                )
                            )
                    }
                }

                Live2DCubismCoreFFM.csmGetParameterMinimumValues(model.nativeHandle)
                    .also { minimumValues ->
                        val minimumValues =
                            minimumValues.reinterpret(parameterCount * ValueLayout.JAVA_FLOAT.byteSize())
                        for (i in 0 until parameterCount) {
                            cubismParameters.minimumValues[i] =
                                minimumValues.getAtIndex(
                                    ValueLayout.JAVA_FLOAT,
                                    i.toLong()
                                )
                        }
                    }

                Live2DCubismCoreFFM.csmGetParameterMaximumValues(model.nativeHandle)
                    .also { maximumValues ->
                        val maximumValues =
                            maximumValues.reinterpret(parameterCount * ValueLayout.JAVA_FLOAT.byteSize())
                        for (i in 0 until parameterCount) {
                            cubismParameters.maximumValues[i] =
                                maximumValues.getAtIndex(
                                    ValueLayout.JAVA_FLOAT,
                                    i.toLong()
                                )
                        }
                    }

                Live2DCubismCoreFFM.csmGetParameterDefaultValues(model.nativeHandle)
                    .also { defaultValues ->
                        val defaultValues =
                            defaultValues.reinterpret(parameterCount * ValueLayout.JAVA_FLOAT.byteSize())
                        for (i in 0 until parameterCount) {
                            cubismParameters.defaultValues[i] =
                                defaultValues.getAtIndex(
                                    ValueLayout.JAVA_FLOAT,
                                    i.toLong()
                                )
                        }
                    }

                val keyCountsList = mutableListOf<Int>()
                Live2DCubismCoreFFM.csmGetParameterKeyCounts(model.nativeHandle).also { keyCounts ->
                    val keyCounts =
                        keyCounts.reinterpret(parameterCount * ValueLayout.JAVA_INT.byteSize())
                    for (i in 0 until parameterCount) {
                        cubismParameters.keyCounts[i] =
                            keyCounts.getAtIndex(
                                ValueLayout.JAVA_INT,
                                i.toLong()
                            ).also {
                                keyCountsList.add(it)
                            }
                    }
                }

                Live2DCubismCoreFFM.csmGetParameterKeyValues(model.nativeHandle).also { keyValues ->
                    val m0 = keyValues.reinterpret(parameterCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until parameterCount) {
                        val memorySegment = m0.getAtIndex(
                            ValueLayout.ADDRESS,
                            i.toLong()
                        )

                        memorySegment.reinterpret(keyCountsList.get(i) * ValueLayout.JAVA_FLOAT.byteSize())
                            .let {
                                cubismParameters.keyValues[i] = FloatArray(keyCountsList.get(i))
                                for (j in 0 until keyCountsList.get(i)) {
                                    cubismParameters.keyValues[i]!![j] =
                                        it.getAtIndex(
                                            ValueLayout.JAVA_FLOAT,
                                            j.toLong()
                                        )
                                }
                            }
                    }
                }

                Live2DCubismCoreFFM.csmGetParameterValues(model.nativeHandle).also { values ->
                    val values =
                        values.reinterpret(parameterCount * ValueLayout.JAVA_FLOAT.byteSize())
                    for (i in 0 until parameterCount) {
                        cubismParameters.values[i] =
                            values.getAtIndex(
                                ValueLayout.JAVA_FLOAT,
                                i.toLong()
                            )
                    }
                }
            }
        }.let { cubismParameters ->
            model::class.java.getDeclaredField("parameters").apply {
                isAccessible = true
                set(
                    model,
                    cubismParameters
                )
            }
        }

        Live2DCubismCoreFFM.csmGetPartCount(model.nativeHandle).let { partCount ->
            CubismParts(partCount).also { cubismParts ->
                Live2DCubismCoreFFM.csmGetPartIds(model.nativeHandle).also { partIds ->
                    val partIds = partIds.reinterpret(partCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until partCount) {
                        cubismParts.ids[i] =
                            partIds.getAtIndex(
                                ValueLayout.ADDRESS,
                                i.toLong()
                            ).reinterpret(Long.MAX_VALUE).getUtf8String(0)
                    }
                }

                Live2DCubismCoreFFM.csmGetPartParentPartIndices(model.nativeHandle)
                    .also { partParentIndices ->
                        val partParentIndices =
                            partParentIndices.reinterpret(partCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until partCount) {
                            cubismParts.parentPartIndices[i] =
                                partParentIndices.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                )
                        }
                    }

                Live2DCubismCoreFFM.csmGetPartOpacities(model.nativeHandle).also { opacities ->
                    val opacities =
                        opacities.reinterpret(partCount * ValueLayout.JAVA_FLOAT.byteSize())
                    for (i in 0 until partCount) {
                        cubismParts.opacities[i] =
                            opacities.getAtIndex(
                                ValueLayout.JAVA_FLOAT,
                                i.toLong()
                            )
                    }
                }
            }
        }.let { cubismParts ->
            model::class.java.getDeclaredField("parts").apply {
                isAccessible = true
                set(
                    model,
                    cubismParts
                )
            }
        }

        Live2DCubismCoreFFM.csmGetDrawableCount(model.nativeHandle).let { drawableCount ->
            CubismDrawables(drawableCount).also { cubismDrawables ->
                Live2DCubismCoreFFM.csmGetDrawableIds(model.nativeHandle).also { drawableIds ->
                    val partIds =
                        drawableIds.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until drawableCount) {
                        cubismDrawables.ids[i] =
                            partIds.getAtIndex(
                                ValueLayout.ADDRESS,
                                i.toLong()
                            ).reinterpret(Long.MAX_VALUE).getUtf8String(0)
                    }
                }

                Live2DCubismCoreFFM.csmGetDrawableConstantFlags(model.nativeHandle).also { flags ->
                    val flags = flags.reinterpret(drawableCount * ValueLayout.JAVA_BYTE.byteSize())
                    for (i in 0 until drawableCount) {
                        cubismDrawables.constantFlags[i] =
                            flags.getAtIndex(
                                ValueLayout.JAVA_BYTE,
                                i.toLong()
                            )
                    }
                }

                Live2DCubismCoreFFM.csmGetDrawableDynamicFlags(model.nativeHandle).also { flags ->
                    val flags = flags.reinterpret(drawableCount * ValueLayout.JAVA_BYTE.byteSize())
                    for (i in 0 until drawableCount) {
                        cubismDrawables.dynamicFlags[i] =
                            flags.getAtIndex(
                                ValueLayout.JAVA_BYTE,
                                i.toLong()
                            )
                    }
                }

                Live2DCubismCoreFFM.csmGetDrawableTextureIndices(model.nativeHandle)
                    .also { textureIndices ->
                        val textureIndices =
                            textureIndices.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until drawableCount) {
                            cubismDrawables.textureIndices[i] =
                                textureIndices.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                )
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableDrawOrders(model.nativeHandle)
                    .also { drawOrders ->
                        val textureIndices =
                            drawOrders.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until drawableCount) {
                            cubismDrawables.drawOrders[i] =
                                textureIndices.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                )
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableRenderOrders(model.nativeHandle)
                    .also { renderOrders ->
                        val textureIndices =
                            renderOrders.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until drawableCount) {
                            cubismDrawables.renderOrders[i] =
                                textureIndices.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                )
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableOpacities(model.nativeHandle).also { opacities ->
                    val textureIndices =
                        opacities.reinterpret(drawableCount * ValueLayout.JAVA_FLOAT.byteSize())
                    for (i in 0 until drawableCount) {
                        cubismDrawables.opacities[i] =
                            textureIndices.getAtIndex(
                                ValueLayout.JAVA_FLOAT,
                                i.toLong()
                            )
                    }
                }

                val maskCountsList = mutableListOf<Int>()
                Live2DCubismCoreFFM.csmGetDrawableMaskCounts(model.nativeHandle)
                    .also { maskCounts ->
                        val textureIndices =
                            maskCounts.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until drawableCount) {
                            cubismDrawables.maskCounts[i] =
                                textureIndices.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                ).also {
                                    maskCountsList.add(it)
                                }
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableMasks(model.nativeHandle).also { masks ->
                    val m0 = masks.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until drawableCount) {
                        val memorySegment = m0.getAtIndex(
                            ValueLayout.ADDRESS,
                            i.toLong()
                        )

                        memorySegment.reinterpret(maskCountsList.get(i) * ValueLayout.JAVA_INT.byteSize())
                            .let {
                                cubismDrawables.masks[i] = IntArray(size = maskCountsList.get(i))
                                for (j in 0 until maskCountsList.get(i)) {
                                    cubismDrawables.masks[i]!![j] =
                                        it.getAtIndex(
                                            ValueLayout.JAVA_INT,
                                            j.toLong()
                                        )
                                }
                            }
                    }
                }

                val vertexCountsList = mutableListOf<Int>()
                Live2DCubismCoreFFM.csmGetDrawableVertexCounts(model.nativeHandle)
                    .also { vertexCounts ->
                        val vertexCounts =
                            vertexCounts.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until drawableCount) {
                            cubismDrawables.vertexCounts[i] =
                                vertexCounts.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                ).also {
                                    vertexCountsList.add(it)
                                }
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableVertexPositions(model.nativeHandle)
                    .also { vertexPositions ->
                        val m0 =
                            vertexPositions.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize())
                        for (i in 0 until drawableCount) {
                            val vertexPosition = m0.getAtIndex(
                                ValueLayout.ADDRESS,
                                i.toLong()
                            )

                            vertexPosition.reinterpret(vertexCountsList.get(i) * ValueLayout.JAVA_FLOAT.byteSize() * 2)
                                .let {
                                    cubismDrawables.vertexPositions[i] =
                                        FloatArray(size = vertexCountsList.get(i) * 2)
                                    for (j in 0 until vertexCountsList.get(i)) {
                                        cubismDrawables.vertexPositions[i]!![j * 2] =
                                            it.getAtIndex(
                                                ValueLayout.JAVA_FLOAT,
                                                j * 2L
                                            )
                                        cubismDrawables.vertexPositions[i]!![j * 2 + 1] =
                                            it.getAtIndex(
                                                ValueLayout.JAVA_FLOAT,
                                                j * 2L + 1L
                                            )
                                    }
                                }
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableVertexUvs(model.nativeHandle).also { vertexUVs ->
                    val m0 =
                        vertexUVs.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until drawableCount) {
                        val vertexUV = m0.getAtIndex(
                            ValueLayout.ADDRESS,
                            i.toLong()
                        )

                        vertexUV.reinterpret(vertexCountsList.get(i) * ValueLayout.JAVA_FLOAT.byteSize() * 2)
                            .let {
                                cubismDrawables.vertexUvs[i] =
                                    FloatArray(size = vertexCountsList.get(i) * 2)
                                for (j in 0 until vertexCountsList.get(i)) {
                                    cubismDrawables.vertexUvs[i]!![j * 2] =
                                        it.getAtIndex(
                                            ValueLayout.JAVA_FLOAT,
                                            j * 2L
                                        )
                                    cubismDrawables.vertexUvs[i]!![j * 2 + 1] =
                                        it.getAtIndex(
                                            ValueLayout.JAVA_FLOAT,
                                            j * 2L + 1L
                                        )
                                }
                            }
                    }
                }

                val indicesCountsList = mutableListOf<Int>()
                Live2DCubismCoreFFM.csmGetDrawableIndexCounts(model.nativeHandle)
                    .also { indexCounts ->
                        val indexCounts =
                            indexCounts.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until drawableCount) {
                            cubismDrawables.indexCounts[i] =
                                indexCounts.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                ).also {
                                    indicesCountsList.add(it)
                                }
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableIndices(model.nativeHandle).also { indices ->
                    val m0 = indices.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize())
                    for (i in 0 until drawableCount) {
                        val indice = m0.getAtIndex(
                            ValueLayout.ADDRESS,
                            i.toLong()
                        )

                        indice.reinterpret(indicesCountsList.get(i) * ValueLayout.JAVA_SHORT.byteSize())
                            .let {
                                cubismDrawables.indices[i] =
                                    ShortArray(size = indicesCountsList.get(i))
                                for (j in 0 until indicesCountsList.get(i)) {
                                    cubismDrawables.indices[i]!![j] =
                                        it.getAtIndex(
                                            ValueLayout.JAVA_SHORT,
                                            j.toLong()
                                        )
                                }
                            }
                    }
                }

                Live2DCubismCoreFFM.csmGetDrawableMultiplyColors(model.nativeHandle)
                    .also { multiplyColors ->
                        val multiplyColors =
                            multiplyColors.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize() * 4)
                        for (i in 0 until drawableCount) {
                            cubismDrawables.multiplyColors[i] = FloatArray(size = 4)
                            for (j in 0 until 4) {
                                cubismDrawables.multiplyColors[i]!![j] = multiplyColors.getAtIndex(
                                    ValueLayout.JAVA_FLOAT,
                                    i * 4 + j.toLong()
                                )
                            }
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableScreenColors(model.nativeHandle)
                    .also { screenColors ->
                        val screenColors =
                            screenColors.reinterpret(drawableCount * ValueLayout.ADDRESS.byteSize() * 4)
                        for (i in 0 until drawableCount) {
                            cubismDrawables.screenColors[i] = FloatArray(size = 4)
                            for (j in 0 until 4) {
                                cubismDrawables.screenColors[i]!![j] = screenColors.getAtIndex(
                                    ValueLayout.JAVA_FLOAT,
                                    i * 4 + j.toLong()
                                )
                            }
                        }
                    }

                Live2DCubismCoreFFM.csmGetDrawableParentPartIndices(model.nativeHandle)
                    .also { parentPartIndices ->
                        val parentPartIndices =
                            parentPartIndices.reinterpret(drawableCount * ValueLayout.JAVA_INT.byteSize())
                        for (i in 0 until drawableCount) {
                            cubismDrawables.parentPartIndices[i] =
                                parentPartIndices.getAtIndex(
                                    ValueLayout.JAVA_INT,
                                    i.toLong()
                                )
                        }
                    }


            }
        }.let { cubismDrawables ->
            model::class.java.getDeclaredField("drawables").apply {
                isAccessible = true
                set(
                    model,
                    cubismDrawables
                )
            }

        }

    }

    actual fun coreLogFunction(logFunction: LogFunction) {
        CoreLogFunctionBridge.logFunction = logFunction
    }

    object CoreLogFunctionBridge {
        var logFunction: LogFunction = { println(it) }

        @JvmStatic
        fun print(memorySegment: MemorySegment) {
            logFunction(
                memorySegment.reinterpret(Long.MAX_VALUE).getUtf8String(0)
            )
        }
    }

}
