//package com.live2d.sdk.cubism.framework.rendering
//
//import com.live2d.sdk.cubism.framework.model.Model
//import org.lwjgl.opengl.GL15
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.FloatBuffer
//import java.nio.ShortBuffer
//
//class ModelMesh {
//    //    val vaoArray: IntArray
//    private val vertexArrayCaches: Array<FloatBuffer>
//
//    private val uvArrayCaches: Array<FloatBuffer>
//
//    private val indexArrayCaches: Array<ShortBuffer>
//
//    constructor(model: Model) {
//        val drawableCount: Int = model.drawableCount
//
////        vaoArray = IntArray(drawableCount)
//        vertexArrayCaches = Array<FloatBuffer>(drawableCount) { drawableIndex ->
//            val positions = model.model.drawableViews[drawableIndex].vertexPositions!!
//
//            ByteBuffer.allocateDirect(
//                positions.size * Float.SIZE_BYTES
//            )
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer()
//        }
//        uvArrayCaches = Array<FloatBuffer>(drawableCount) { drawableIndex ->
//            val uvs = model.model.drawableViews[drawableIndex].vertexUvs!!
//            ByteBuffer.allocateDirect(
//                uvs.size * Float.SIZE_BYTES
//            )
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer()
//        }
//        indexArrayCaches = Array<ShortBuffer>(drawableCount) { drawableIndex ->
//            val indices = model.model.drawableViews[drawableIndex].indices!!
//            ByteBuffer.allocateDirect(
//                indices.size * Short.SIZE_BYTES
//            )
//                .order(ByteOrder.nativeOrder())
//                .asShortBuffer()
//        }
//
//        var vbo: Int
//        repeat(drawableCount) { drawableIndex ->
////            vaoArray[drawableIndex] = glGenVertexArrays()
////            glBindVertexArray(vaoArray[drawableIndex])
//
//            run {
//                vbo = GL15.glGenBuffers()
//
//                val positions = model.model.drawableViews[drawableIndex].vertexPositions!!
//                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
//                GL15.glBufferData(
//                    GL15.GL_ARRAY_BUFFER,
//                    (positions.size * Float.SIZE_BYTES).toLong(),
//                    GL15.GL_DYNAMIC_DRAW
//                )
//            }
//
//            run {
//                vbo = GL15.glGenBuffers()
//
//                val uvs: FloatArray = model.model.drawableViews[drawableIndex].vertexUvs!!
//                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
//                GL15.glBufferData(
//                    GL15.GL_ARRAY_BUFFER,
//                    (uvs.size * Float.SIZE_BYTES).toLong(),
//                    GL15.GL_DYNAMIC_DRAW
//                )
//            }
//
//            run {
//                vbo = GL15.glGenBuffers()
//
//                val indices: ShortArray = model.model.drawableViews[drawableIndex].indices!!
//                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo)
//                GL15.glBufferData(
//                    GL15.GL_ARRAY_BUFFER,
//                    (indices.size * Short.SIZE_BYTES).toLong(),
//                    GL15.GL_STATIC_DRAW
//                )
//            }
//
////            glBindVertexArray(0)
//        }
//    }
//
//    fun updatePositions(drawableIndex: Int, drawableVertices: FloatArray): FloatBuffer {
//        return vertexArrayCaches[drawableIndex]
//            .clear()
//            .put(drawableVertices)
//            .position(0)
//    }
//
//    fun updateUvs(drawableIndex: Int, drawableVertexUvs: FloatArray): FloatBuffer {
//        return uvArrayCaches[drawableIndex]
//            .clear()
//            .put(drawableVertexUvs)
//            .position(0)
//    }
//
//    fun updateIndices(drawableIndex: Int, drawableIndices: ShortArray): ShortBuffer {
//        return indexArrayCaches[drawableIndex]
//            .clear()
//            .put(drawableIndices)
//            .position(0)
//    }
//
//}