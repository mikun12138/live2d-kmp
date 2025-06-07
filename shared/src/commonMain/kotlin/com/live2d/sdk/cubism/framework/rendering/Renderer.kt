package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismVector2
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.type.csmRectF
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

expect fun Renderer.Companion.create(
    model: Live2DModel,
    offScreenBufferCount: Int,
): Renderer

abstract class Renderer {

    val drawableContextArray: Array<DrawableContext>
    val offscreenSurfaces: Array<ACubismOffscreenSurface>

    val clipContext_2_drawableIndexList: MutableMap<ClipContext, MutableList<Int>> = mutableMapOf()

    constructor(
        model: Live2DModel,
        offScreenBufferCount: Int,
    ) {
        drawableContextArray = Array(model.drawableCount) {
            DrawableContext(model, it)
        }

        offscreenSurfaces = Array(offScreenBufferCount) {
            ACubismOffscreenSurface.create().apply {
                CubismVector2(512.0f, 512.0f)
            }
        }

        repeat(model.drawableCount) { index ->
            val drawableMask = model.getDrawableMask(index)!!
            if (drawableMask.isNotEmpty()) {
                clipContext_2_drawableIndexList.keys.find {
                    it.maskIdArray.size == drawableMask.size
                            && it.maskIdArray.all { drawableMask.contains(it) }
                } ?: run {
                    ClipContext(drawableMask).also {
                        clipContext_2_drawableIndexList.put(it, mutableListOf())
                    }
                }.let {
                    clipContext_2_drawableIndexList[it]!!.add(index)
                    drawableContextArray[index].clipContext = it
                }

            }
        }
    }

    fun frame() {
        genMasks()
        draw()
    }

    abstract fun genMasks()

    fun draw() {
        val sortedDrawableContextArray = drawableContextArray.sortedWith(
            compareBy { it.renderOrder }
        )

        sortedDrawableContextArray.forEach { drawableContext ->
            drawableContext.update()


        }
    }

    abstract fun drawMesh(
        drawableContext: DrawableContext,
    )

    companion object

}

class DrawableContext(
    val model: Live2DModel,
    val index: Int,
) {
    val vertex: Vertex = Vertex(model, index)
    val renderOrder = model.getDrawableRenderOrder(this@DrawableContext.index)

    val textureIndex = model.getDrawableTextureIndex(this@DrawableContext.index)

    val isCulling = !model.getDrawableIsDoubleSided(this@DrawableContext.index)

    var isVisible: Boolean = false
    lateinit var clipContext: ClipContext

    fun update() {
        isVisible = model.getDrawableDynamicFlagIsVisible(this@DrawableContext.index)
    }

    class Vertex(
        val model: Live2DModel,
        val index: Int,
    ) {
        val positions: FloatBuffer =
            model.getDrawableVertexPositions(index).let {
                ByteBuffer.allocateDirect(it!!.size * Float.SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            }
        val texCoords: FloatBuffer =
            model.getDrawableVertexUVs(index).let {
                ByteBuffer.allocateDirect(it!!.size * Float.SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            }
        val indices: ShortBuffer =
            model.getDrawableIndices(index).let {
                ByteBuffer.allocateDirect(it!!.size * Short.SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer()
            }

        fun update() {
            positions.clear()
                .put(model.getDrawableVertexPositions(index))
                .position(0)
            texCoords.clear()
                .put(model.getDrawableVertexUVs(index))
                .position(0)
            indices.clear()
                .put(model.getDrawableIndices(index))
                .position(0)
        }
    }

}

class ClipContext(
    val maskIdArray: IntArray,
) {

    val bufferIndex = 0
    val layoutBounds: csmRectF = csmRectF()
    var layoutChannelIndex = 0

//    val allClippedDrawRect: csmRectF = csmRectF()

}