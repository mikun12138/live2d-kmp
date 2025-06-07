package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Live2DModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Drawableの情報を格納するバッファをキャッシュし保持するクラス。
 */
class CubismDrawableInfoCachesHolder(model: Live2DModel) {

    fun setUpVertexArray(drawableIndex: Int, drawableVertices: FloatArray): FloatBuffer {
        val vertexArray = vertexArrayCaches[drawableIndex]
        vertexArray!!.clear()
        vertexArray!!.put(drawableVertices)
        vertexArray!!.position(0)

        return vertexArray!!
    }

    fun setUpUvArray(drawableIndex: Int, drawableVertexUvs: FloatArray): FloatBuffer {
        val uvArray = uvArrayCaches[drawableIndex]
        uvArray!!.clear()
        uvArray!!.put(drawableVertexUvs)
        uvArray!!.position(0)

        return uvArray!!
    }

    fun setUpIndexArray(drawableIndex: Int, drawableIndices: ShortArray): ShortBuffer {
        val indexArray = indexArrayCaches[drawableIndex]
        indexArray!!.clear()
        indexArray!!.put(drawableIndices)
        indexArray!!.position(0)

        return indexArray!!
    }

    /**
     * Drawableの頂点のキャッシュ配列
     */
    private val vertexArrayCaches: Array<FloatBuffer?>

    /**
     * DrawableのUV情報のキャッシュ配列
     */
    private val uvArrayCaches: Array<FloatBuffer?>

    /**
     * Drawableの頂点に対するポリゴンの対応番号のキャッシュ配列
     */
    private val indexArrayCaches: Array<ShortBuffer?>

    init {
        val drawableCount: Int = model.drawableCount
        val renderOrder: IntArray = model.model.drawables.renderOrders
        val sortedDrawableIndexList = IntArray(drawableCount)

        // Sort the index by drawing order
        for (i in 0..<drawableCount) {
            val order = renderOrder[i]
            sortedDrawableIndexList[order] = i
        }
        vertexArrayCaches = arrayOfNulls<FloatBuffer>(drawableCount)
        uvArrayCaches = arrayOfNulls<FloatBuffer>(drawableCount)
        indexArrayCaches = arrayOfNulls<ShortBuffer>(drawableCount)

        for (i in 0..<drawableCount) {
            val drawableIndex = sortedDrawableIndexList[i]

            // Vertex Array
            run {
                val vertexArray: FloatArray = model.getDrawableVertexPositions(drawableIndex)!!
                val bb = ByteBuffer.allocateDirect(vertexArray.size * 4)
                bb.order(ByteOrder.nativeOrder())
                val buffer = bb.asFloatBuffer()
                vertexArrayCaches[drawableIndex] = buffer
            }

            // UV Array
            run {
                val uvArray: FloatArray = model.getDrawableVertexUVs(drawableIndex)!!
                val bb = ByteBuffer.allocateDirect(uvArray.size * 4)
                bb.order(ByteOrder.nativeOrder())
                val buffer = bb.asFloatBuffer()
                uvArrayCaches[drawableIndex] = buffer
            }

            // Index Array
            run {
                val indexArray: ShortArray = model.getDrawableIndices(drawableIndex)!!
                val bb = ByteBuffer.allocateDirect(indexArray.size * 4)
                bb.order(ByteOrder.nativeOrder())
                val buffer = bb.asShortBuffer()
                indexArrayCaches[drawableIndex] = buffer
            }
        }
    }
}