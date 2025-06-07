package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismVector2
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.rendering.Renderer.State
import com.live2d.sdk.cubism.framework.type.csmRectF
import com.live2d.sdk.cubism.util.IState
import com.live2d.sdk.cubism.util.StateContext
import com.live2d.sdk.cubism.util.switchStateTo
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

expect fun Renderer.Companion.create(
    model: Live2DModel,
    offScreenBufferCount: Int,
): Renderer

abstract class Renderer : StateContext<Renderer, State> {
    override var state: State = State.SETUP_MASK

    val textures: MutableMap<Int, ALive2DTexture>
    val isPremultipliedAlpha: Boolean

    var mvp: CubismMatrix44 = CubismMatrix44.create()


    val drawableContextArray: Array<DrawableContext>
    val offscreenSurfaces: Array<ACubismOffscreenSurface>

    val clipContext_2_drawableIndexList: MutableMap<ClipContext, MutableList<Int>> = mutableMapOf()

    constructor(
        model: Live2DModel,
        offScreenBufferCount: Int,
    ) {
        textures = model.textures
        isPremultipliedAlpha = model.isPremultipliedAlpha

        drawableContextArray = Array(model.drawableCount) {
            DrawableContext(model, it)
        }

        offscreenSurfaces = Array(offScreenBufferCount) {
            ACubismOffscreenSurface.create().apply {
                createOffscreenSurface(
                    CubismVector2(512.0f, 512.0f)
                )
            }
        }

        repeat(model.drawableCount) { index ->
            val drawableMask = model.getDrawableMask(index)!!
            if (drawableMask.isNotEmpty()) {
                val clipContext = clipContext_2_drawableIndexList.keys.find {
                    it.maskIndexArray.size == drawableMask.size
                            && it.maskIndexArray.all { drawableMask.contains(it) }
                } ?: run {
                    ClipContext(drawableMask).also {
                        clipContext_2_drawableIndexList.put(it, mutableListOf())
                    }
                }
                clipContext.let {
                    clipContext_2_drawableIndexList[it]!!.add(index)
                    drawableContextArray[index].clipContext = it
                }

            }
        }
    }

    fun frame(mvp: CubismMatrix44) {
        this.mvp.setMatrix(mvp)
        drawableContextArray.forEach {
            it.update()
        }
        this switchStateTo State.SETUP_MASK
        setupMask()
        this switchStateTo State.DRAW
        draw()
    }

    abstract fun setupMask()

    private fun draw() {
        val sortedDrawableContextArray = drawableContextArray.sortedWith(
            compareBy { it.renderOrder }
        )

        sortedDrawableContextArray.forEach { drawableContext ->
            drawMesh(drawableContext)
        }
    }

    abstract fun drawMesh(
        drawableContext: DrawableContext,
    )

    enum class State(
        override val onEnter: (Renderer, State) -> Unit = { _, _ -> },
        override val onExit: (Renderer, State) -> Unit = { _, _ -> },
    ) : IState<Renderer, State> {
        SETUP_MASK,
        DRAW
    }

    companion object

}

class DrawableContext(
    val model: Live2DModel,
    val index: Int,
) {
    val renderOrder = model.getDrawableRenderOrder(index)

    val textureIndex = model.getDrawableTextureIndex(index)

    val isCulling = !model.getDrawableIsDoubleSided(index)

    val blendMode: CubismBlendMode = model.getDrawableBlendMode(index)
    val isInvertedMask = model.getDrawableInvertedMask(index)

    val vertex: Vertex = Vertex(model, index)
    var isVisible = false
    var vertexPositionDidChange = false
    var opacity = 1.0f
    lateinit var baseColor: CubismTextureColor
    lateinit var multiplyColor: CubismTextureColor
    lateinit var screenColor: CubismTextureColor

    var clipContext: ClipContext? = null

    fun update() {
        vertex.update()
        isVisible = model.getDrawableDynamicFlagIsVisible(index)
        vertexPositionDidChange = model.getDrawableDynamicFlagVertexPositionsDidChange(index)
        opacity = model.getDrawableOpacity(index)
        baseColor = model.getModelColorWithOpacity(opacity)
        multiplyColor = model.getDrawableMultiplyColors(index)!!.let {
            CubismTextureColor(
                it[0],
                it[1],
                it[2],
                it[3],
            )
        }
        screenColor = model.getDrawableScreenColors(index)!!.let {
            CubismTextureColor(
                it[0],
                it[1],
                it[2],
                it[3],
            )
        }


    }

    class Vertex(
        val model: Live2DModel,
        val index: Int,
    ) {
        val count = model.getDrawableVertexCount(index)
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

/*
    对应framebuffer中的一个区域
 */
class ClipContext(
    val maskIndexArray: IntArray,
) {


    var bufferIndex = 0
    val layoutBounds: csmRectF = csmRectF()
    var layoutChannelIndex = 0

    val allClippedDrawRect: csmRectF = csmRectF()

    val matrixForMask: CubismMatrix44 = CubismMatrix44.create()

    val matrixForDraw: CubismMatrix44 = CubismMatrix44.create()

    companion object {
        val CHANNEL_FLAGS = arrayOf(
            CubismTextureColor(
                r = 1.0f,
                g = 0.0f,
                b = 0.0f,
                a = 0.0f,
            ),
            CubismTextureColor(
                r = 0.0f,
                g = 1.0f,
                b = 0.0f,
                a = 0.0f,
            ),
            CubismTextureColor(
                r = 0.0f,
                g = 0.0f,
                b = 1.0f,
                a = 0.0f,
            ),
            CubismTextureColor(
                r = 0.0f,
                g = 0.0f,
                b = 0.0f,
                a = 1.0f,
            )
        )
    }

}