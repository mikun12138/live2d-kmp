package com.live2d.sdk.cubism.core

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.file.Path
import kotlin.io.path.Path

/*
    v3
    TODO:: update to v4
 */

/* ----- *
 * TYPES *
 * ----- */

/** Cubism moc */
class CsmMoc

/** Cubism model */
class CsmModel
/** Cubism version identifier. */
typealias csmVersion = Int

/**
 * Alignment constraints.
 */

/** Necessary alignment for mocs (in bytes). */
val csmAlignofMoc = 64L

/** Necessary alignment for models (in bytes). */
val csmAlignofModel = 16L

/**
 * Bit masks for non-dynamic drawable flags.
 */

/** Additive blend mode mask. */
val csmBlendAdditive = 1 shl 0

/** Multiplicative blend mode mask. */
val csmBlendMultiplicative = 1 shl 1

/** Double-sidedness mask. */
val csmIsDoubleSided = 1 shl 2

/** Clipping mask inversion mode mask. */
val csmIsInvertedMask = 1 shl 3

/**
 * Bit masks for dynamic drawable flags.
 */

/** Flag set when visible. */
val csmIsVisible = 1 shl 0

/** Flag set when visibility did change. */
val csmVisibilityDidChange = 1 shl 1

/** Flag set when opacity did change. */
val csmOpacityDidChange = 1 shl 2

/** Flag set when draw order did change. */
val csmDrawOrderDidChange = 1 shl 3

/** Flag set when render order did change. */
val csmRenderOrderDidChange = 1 shl 4

/** Flag set when vertex positions did change. */
val csmVertexPositionsDidChange = 1 shl 5

/** Flag set when blend color did change. */
val csmBlendColorDidChange = 1 shl 6

/** Bitfield. */
typealias csmFlags = Char

/**
 * moc3 file format version.
 */

/** unknown */
val csmMocVersion_Unknown = 0

/** moc3 file version 3.0.00 - 3.2.07 */
val csmMocVersion_30 = 1

/** moc3 file version 3.3.00 - 3.3.03 */
val csmMocVersion_33 = 2

/** moc3 file version 4.0.00 - 4.1.05 */
val csmMocVersion_40 = 3

/** moc3 file version 4.2.00 - 4.2.04 */
val csmMocVersion_42 = 4

/** moc3 file version 5.0.00 - */
val csmMocVersion_50 = 5

/**
 * Parameter types.
 */

/** Normal parameter. */
val csmParameterType_Normal = 0

/** Parameter for blend shape. */
val csmParameterType_BlendShape = 1

/** Parameter type. */
typealias csmParameterType = Int

/** 2 component vector. */
class csmVector2 {
    var x: Float = 0f
    var y: Float = 0f
}

/** 4 component vector. */
class csmVector4 {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var w: Float = 0f
}

sealed class Live2DCubismCoreFFM : LibraryFFM {
    constructor(
        funName: String,
        descriptor: FunctionDescriptor,
    ) : super(
        funName,
        descriptor
    )

    @Suppress("UnsafeDynamicallyLoadedCode")
    companion object {
        fun load(
            pathString: String
        ) {
            // TODO:: 区分下架构
            System.load(
                Path(pathString, "Live2DCubismCore.dll").toString()
            )
        }
    }

    /* ------- *
     * VERSION *
     * ------- */

    /**
     * Queries Core version.
     *
     * @return  Core version.
     */
    object getVersion : Live2DCubismCoreFFM(
        "csmGetVersion",
        FunctionDescriptor.of(ValueLayout.JAVA_INT)
    ), () -> Int {
        override fun invoke(): Int {
            return this.methodHandle.invokeExact() as Int
        }
    }

    /**
     * Gets Moc file supported latest version.
     *
     * @return csmMocVersion (Moc file latest format version).
     */
    object getLatestMocVersion : Live2DCubismCoreFFM(
        "csmGetLatestMocVersion",
        FunctionDescriptor.of(ValueLayout.JAVA_INT)
    ), () -> Int {
        override fun invoke(): Int {
            return this.methodHandle.invokeExact() as Int
        }
    }

    /**
     * Gets Moc file format version.
     *
     * @param  address  Address of moc.
     * @param  size     Size of moc (in bytes).
     *
     * @return csmMocVersion
     */
    object csmGetMocVersion : Live2DCubismCoreFFM(
        "csmGetMocVersion",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT,
        )
    ), (ByteArray) -> Int {
        override fun invoke(mocByteArray: ByteArray): Int {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    it.allocateByteArray(mocByteArray),
                    mocByteArray.size,
                ) as Int
            }
        }
    }

    /**
     * Checks consistency of a moc.
     *
     * @param  address  Address of unrevived moc. The address must be aligned to 'csmAlignofMoc'.
     * @param  size     Size of moc (in bytes).
     *
     * @return  '1' if Moc is valid; '0' otherwise.
     */
    object csmHasMocConsistency : Live2DCubismCoreFFM(
        "csmHasMocConsistency",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT,
        )
    ), (ByteArray) -> Int {
        override fun invoke(mocByteArray: ByteArray): Int {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    it.allocateAlignedByteArray(
                        mocByteArray,
                        csmAlignofMoc
                    ),
                    mocByteArray.size,
                ) as Int
            }
        }
    }

    /* ------- *
     * LOGGING *
     * ------- */

    /**
     * Queries log handler.
     *
     * @return  Log handler.
     */
    // TODO::
    object csmGetLogFunction

    /**
     * Sets log handler.
     *
     * @param  handler  Handler to use.
     */
    object csmSetLogFunction : Live2DCubismCoreFFM(
        "csmSetLogFunction",
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS,
        )
    ), ((String) -> Unit) -> Unit {
        override fun invoke(var0: (String) -> Unit) {
            return this.methodHandle.invokeExact(

            ) as Unit
//            val logHander = MethodHandles.lookup().findStatic(
//                ICubismLoggerBridge::class.java,
//                "print",
//                MethodType.methodType(
//                    Void.TYPE,
//                    MemorySegment::class.java,
//                )
//            )
//            val logFunctionPointer = linker.upcallStub(
//                logHander,
//                FunctionDescriptor.ofVoid(
//                    ValueLayout.ADDRESS,
//                ),
//                Arena.global()
//            )
//
//            Arena.ofConfined().use {
//                return this.methodHandle.invokeExact(
//                    logFunctionPointer
//                ) as Unit
//            }
        }
    }

    /* --- *
     * MOC *
     * --- */

    /**
     * Tries to revive a moc from bytes in place.
     *
     * @param  address  Address of unrevived moc. The address must be aligned to 'csmAlignofMoc'.
     * @param  size     Size of moc (in bytes).
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmReviveMocInPlace : Live2DCubismCoreFFM(
        "csmReviveMocInPlace",
        FunctionDescriptor.of(
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT,
        )
    ), (ByteArray) -> Long {
        override fun invoke(mocByteArray: ByteArray): Long {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    Arena.global().allocateAlignedByteArray(
                        mocByteArray,
                        csmAlignofMoc
                    ),
                    mocByteArray.size,
                ) as Long
            }
        }
    }

    /* ----- *
     * MODEL *
     * ----- */

    /**
     * Queries size of a model in bytes.
     *
     * @param  moc  Moc to query.
     *
     * @return  Valid size on success; '0' otherwise.
     */
    object csmGetSizeofModel : Live2DCubismCoreFFM(
        "csmGetSizeofModel",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> Int {
        override fun invoke(mocHandle: Long): Int {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(mocHandle),
                ) as Int
            }
        }
    }

    /**
     * Tries to instantiate a model in place.
     *
     * @param  moc      Source moc.
     * @param  address  Address to place instance at. Address must be aligned to 'csmAlignofModel'.
     * @param  size     Size of memory block for instance (in bytes).
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmInitializeModelInPlace : Live2DCubismCoreFFM(
        "csmInitializeModelInPlace",
        FunctionDescriptor.of(
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT,
        )
    ), (Long, Int) -> Long {
        override fun invoke(
            mocHandle: Long,
            size: Int,
        ): Long {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(mocHandle),
                    Arena.global().allocate(
                        size.toLong(),
                        csmAlignofModel
                    ),
                    size,
                ) as Long
            }
        }
    }

    /**
     * Updates a model.
     *
     * @param  model  Model to update.
     */
    object csmUpdateModel : Live2DCubismCoreFFM(
        "csmUpdateModel",
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS,
        )
    ), (Long) -> Unit {
        override fun invoke(modelHandle: Long) {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as Unit
            }
        }
    }
    /* ------ *
     * CANVAS *
     * ------ */

    /**
     * Reads info on a model canvas.
     *
     * @param  model              Model query.
     *
     * @param  outSizeInPixels    Canvas dimensions.
     * @param  outOriginInPixels  Origin of model on canvas.
     * @param  outPixelsPerUnit   Aspect used for scaling pixels to units.
     */
    object csmReadCanvasInfo : Live2DCubismCoreFFM(
        "csmReadCanvasInfo",
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long, MemorySegment, MemorySegment, MemorySegment) -> Unit {
        override fun invoke(
            modelHandle: Long,
            outSizeInPixels: MemorySegment,
            outOriginInPixels: MemorySegment,
            outPixelsPerUnit: MemorySegment,
        ) {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                    outSizeInPixels,
                    outOriginInPixels,
                    outPixelsPerUnit,
                ) as Unit
            }
        }
    }
    /* ---------- *
     * PARAMETERS *
     * ---------- */

    /**
     * Gets number of parameters.
     *
     * @param[in]  model  Model to query.
     *
     * @return  Valid count on success; '-1' otherwise.
     */
    object csmGetParameterCount : Live2DCubismCoreFFM(
        "csmGetParameterCount",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> Int {
        override fun invoke(modelHandle: Long): Int {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as Int
            }
        }
    }

    /**
     * Gets parameter IDs.
     * All IDs are null-terminated ANSI strings.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterIds : Live2DCubismCoreFFM(
        "csmGetParameterIds",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets parameter types.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterTypes : Live2DCubismCoreFFM(
        "csmGetParameterTypes",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets minimum parameter values.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterMinimumValues : Live2DCubismCoreFFM(
        "csmGetParameterMinimumValues",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets maximum parameter values.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterMaximumValues : Live2DCubismCoreFFM(
        "csmGetParameterMaximumValues",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets default parameter values.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterDefaultValues : Live2DCubismCoreFFM(
        "csmGetParameterDefaultValues",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets read/write parameter values buffer.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterValues : Live2DCubismCoreFFM(
        "csmGetParameterValues",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets number of key values of each parameter.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterKeyCounts : Live2DCubismCoreFFM(
        "csmGetParameterKeyCounts",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets key values of each parameter.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetParameterKeyValues : Live2DCubismCoreFFM(
        "csmGetParameterKeyValues",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /* ----- *
     * PARTS *
     * ----- */

    /**
     * Gets number of parts.
     *
     * @param  model  Model to query.
     *
     * @return  Valid count on success; '-1' otherwise.
     */
    object csmGetPartCount : Live2DCubismCoreFFM(
        "csmGetPartCount",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> Int {
        override fun invoke(modelHandle: Long): Int {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as Int
            }
        }
    }

    /**
     * Gets parts IDs.
     * All IDs are null-terminated ANSI strings.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetPartIds : Live2DCubismCoreFFM(
        "csmGetPartIds",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets read/write part opacities buffer.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetPartOpacities : Live2DCubismCoreFFM(
        "csmGetPartOpacities",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets part's parent part indices.
     *
     * @param   model   Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetPartParentPartIndices : Live2DCubismCoreFFM(
        "csmGetPartParentPartIndices",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /* --------- *
     * DRAWABLES *
     * --------- */

    /**
     * Gets number of drawables.
     *
     * @param  model  Model to query.
     *
     * @return  Valid count on success; '-1' otherwise.
     */
    object csmGetDrawableCount : Live2DCubismCoreFFM(
        "csmGetDrawableCount",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> Int {
        override fun invoke(modelHandle: Long): Int {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as Int
            }
        }
    }

    /**
     * Gets drawable IDs.
     * All IDs are null-terminated ANSI strings.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableIds : Live2DCubismCoreFFM(
        "csmGetDrawableIds",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets constant drawable flags.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableConstantFlags : Live2DCubismCoreFFM(
        "csmGetDrawableConstantFlags",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets dynamic drawable flags.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableDynamicFlags : Live2DCubismCoreFFM(
        "csmGetDrawableDynamicFlags",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets drawable texture indices.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableTextureIndices : Live2DCubismCoreFFM(
        "csmGetDrawableTextureIndices",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets drawable draw orders.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableDrawOrders : Live2DCubismCoreFFM(
        "csmGetDrawableDrawOrders",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets drawable render orders.
     * The higher the order, the more up front a drawable is.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0'otherwise.
     */
    object csmGetDrawableRenderOrders : Live2DCubismCoreFFM(
        "csmGetDrawableRenderOrders",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets drawable opacities.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableOpacities : Live2DCubismCoreFFM(
        "csmGetDrawableOpacities",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets numbers of masks of each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableMaskCounts : Live2DCubismCoreFFM(
        "csmGetDrawableMaskCounts",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets mask indices of each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableMasks : Live2DCubismCoreFFM(
        "csmGetDrawableMasks",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets number of vertices of each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableVertexCounts : Live2DCubismCoreFFM(
        "csmGetDrawableVertexCounts",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets vertex position data of each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; a null pointer otherwise.
     */
    object csmGetDrawableVertexPositions : Live2DCubismCoreFFM(
        "csmGetDrawableVertexPositions",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets texture coordinate data of each drawables.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableVertexUvs : Live2DCubismCoreFFM(
        "csmGetDrawableVertexUvs",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets number of triangle indices for each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableIndexCounts : Live2DCubismCoreFFM(
        "csmGetDrawableIndexCounts",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets triangle index data for each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableIndices : Live2DCubismCoreFFM(
        "csmGetDrawableIndices",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets multiply color data for each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableMultiplyColors : Live2DCubismCoreFFM(
        "csmGetDrawableMultiplyColors",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets screen color data for each drawable.
     *
     * @param  model  Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableScreenColors : Live2DCubismCoreFFM(
        "csmGetDrawableScreenColors",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_LONG,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    modelHandle,
                ) as MemorySegment
            }
        }
    }

    /**
     * Gets drawable's parent part indices.
     *
     * @param   model   Model to query.
     *
     * @return  Valid pointer on success; '0' otherwise.
     */
    object csmGetDrawableParentPartIndices : Live2DCubismCoreFFM(
        "csmGetDrawableParentPartIndices",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        )
    ), (Long) -> MemorySegment {
        override fun invoke(modelHandle: Long): MemorySegment {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as MemorySegment
            }
        }
    }

    /**
     * Resets all dynamic drawable flags.
     *
     * @param  model  Model containing flags.
     */
    object csmResetDrawableDynamicFlags : Live2DCubismCoreFFM(
        "csmResetDrawableDynamicFlags",
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS,
        )
    ), (Long) -> Unit {
        override fun invoke(modelHandle: Long) {
            Arena.ofConfined().use {
                return this.methodHandle.invokeExact(
                    MemorySegment.ofAddress(modelHandle),
                ) as Unit
            }
        }
    }
}

