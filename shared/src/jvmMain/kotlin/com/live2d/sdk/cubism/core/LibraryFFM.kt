package com.live2d.sdk.cubism.core

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle

abstract class LibraryFFM(
    val methodHandle: MethodHandle
) {
    constructor(
        funName: String,
        descriptor: FunctionDescriptor,
    ) : this(
        linker.downcallHandle(
            lookup.find(funName).orElseThrow(),
            descriptor,
        )
    )

    companion object {
        internal val linker: Linker = Linker.nativeLinker()
        internal val lookup: SymbolLookup = SymbolLookup.loaderLookup()
    }
}

fun Arena.allocateByteArray(var0: ByteArray): MemorySegment {
    val size = var0.size * ValueLayout.JAVA_BYTE.byteSize()
    return this
        .allocate(size)
        .copyFrom(MemorySegment.ofArray(var0))
}

fun Arena.allocateFloatArray(var0: FloatArray): MemorySegment {
    val size = var0.size * ValueLayout.JAVA_FLOAT.byteSize()
    return this
        .allocate(size)
        .copyFrom(MemorySegment.ofArray(var0))
}

fun Arena.allocateAlignedByteArray(
    var0: ByteArray,
    alignment: Long
): MemorySegment {
    val size = var0.size * ValueLayout.JAVA_BYTE.byteSize()
    return this
        .allocate(
            size,
            alignment
        )
        .copyFrom(MemorySegment.ofArray(var0))
}
