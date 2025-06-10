package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.ex.rendering.ALive2DOffscreenSurface
import com.live2d.sdk.cubism.ex.rendering.ALive2DRenderer
import com.live2d.sdk.cubism.ex.rendering.ALive2DTexture
import com.live2d.sdk.cubism.ex.rendering.ClipContext
import com.live2d.sdk.cubism.ex.rendering.DrawableContext
import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_OFFSET
import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_STEP
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismVector2
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.type.csmRectF
import org.lwjgl.opengl.GL30.glMapBufferRange
import org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT
import org.lwjgl.opengl.GL46.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL46.GL_BLEND
import org.lwjgl.opengl.GL46.GL_CCW
import org.lwjgl.opengl.GL46.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL46.GL_CULL_FACE
import org.lwjgl.opengl.GL46.GL_DEPTH_TEST
import org.lwjgl.opengl.GL46.GL_ELEMENT_ARRAY_BUFFER
import org.lwjgl.opengl.GL46.GL_FLOAT
import org.lwjgl.opengl.GL46.GL_MAP_PERSISTENT_BIT
import org.lwjgl.opengl.GL46.GL_MAP_WRITE_BIT
import org.lwjgl.opengl.GL46.GL_SCISSOR_TEST
import org.lwjgl.opengl.GL46.GL_STENCIL_TEST
import org.lwjgl.opengl.GL46.GL_TRIANGLES
import org.lwjgl.opengl.GL46.GL_UNSIGNED_SHORT
import org.lwjgl.opengl.GL46.glBindBuffer
import org.lwjgl.opengl.GL46.glBindVertexArray
import org.lwjgl.opengl.GL46.glBufferStorage
import org.lwjgl.opengl.GL46.glClear
import org.lwjgl.opengl.GL46.glClearColor
import org.lwjgl.opengl.GL46.glColorMask
import org.lwjgl.opengl.GL46.glDisable
import org.lwjgl.opengl.GL46.glDrawElements
import org.lwjgl.opengl.GL46.glEnable
import org.lwjgl.opengl.GL46.glEnableVertexAttribArray
import org.lwjgl.opengl.GL46.glFrontFace
import org.lwjgl.opengl.GL46.glGenBuffers
import org.lwjgl.opengl.GL46.glGenVertexArrays
import org.lwjgl.opengl.GL46.glVertexAttribPointer
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.max
import kotlin.math.min

class Live2DRenderer(
    model: Live2DModel,
    offScreenBufferCount: Int,
) : ALive2DRenderer(
    model,
    offScreenBufferCount,
) {

    override val offscreenSurfaces: Array<ALive2DOffscreenSurface> = Array(offScreenSurfacesCount) {
        Live2DOffscreenSurface().apply {
            createOffscreenSurface(
                CubismVector2(512.0f, 512.0f)
            )
        }
    }

    val textureArray = Array<Live2DTexture>(model.drawableCount) {

    }
    class VertexArray {
        var vao: Int = -1
        var vboPosition: Int = -1
        lateinit var positionsBuffer: FloatBuffer
        var vboTexCoord: Int = -1
        lateinit var texCoordsBuffer: FloatBuffer
        var ebo: Int = -1
        var indicesBuffer: ShortBuffer? = null
    }

    val drawableVertexArrayArray: Array<VertexArray> = Array(model.drawableCount) {
        val drawableContext = drawableContextArray[it]
        VertexArray().apply {
            vao = glGenVertexArrays()

            glBindVertexArray(vao)
            drawableContext.vertex.apply {
                vboPosition = glGenBuffers()
                glBindBuffer(GL_ARRAY_BUFFER, vboPosition)
                glBufferStorage(
                    GL_ARRAY_BUFFER,
                    positionsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )
                positionsBuffer = glMapBufferRange(
                    GL_ARRAY_BUFFER,
                    0,
                    positionsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )!!.asFloatBuffer()
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
                glEnableVertexAttribArray(0)
            }

            drawableContext.vertex.apply {
                vboTexCoord = glGenBuffers()
                glBindBuffer(GL_ARRAY_BUFFER, vboTexCoord)
                glBufferStorage(
                    GL_ARRAY_BUFFER,
                    texCoordsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )
                texCoordsBuffer = glMapBufferRange(
                    GL_ARRAY_BUFFER,
                    0,
                    texCoordsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )!!.asFloatBuffer()
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
                glEnableVertexAttribArray(1)
            }

            drawableContext.vertex.apply {
                ebo = glGenBuffers()
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
                if (indicesArray.isNotEmpty()) {
                    glBufferStorage(
                        GL_ELEMENT_ARRAY_BUFFER,
                        (indicesArray.size * Short.SIZE_BYTES).toLong(),
                        GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                    )
                    indicesBuffer = glMapBufferRange(
                        GL_ELEMENT_ARRAY_BUFFER,
                        0,
                        indicesArray.size * Short.SIZE_BYTES.toLong(),
                        GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                    )!!.asShortBuffer()
                }
            }

            glBindVertexArray(0)
        }
    }

    /*
        lazy cache
    */
    private val clipContext_2_drawableContextList: Map<ClipContext, List<DrawableContext>> by lazy {
        clipContextList.associateWith { clipContext -> drawableContextArray.filter { it.clipContext === clipContext } }
    }

    override fun setupMask() {
        drawableVertexArrayArray.forEachIndexed { index, vertexArray ->
            with(drawableContextArray[index].vertex) {
                vertexArray.positionsBuffer
                    .clear()
                vertexArray.positionsBuffer
                    .put(positionsArray)
                    .position(0)

                vertexArray.texCoordsBuffer
                    .clear()
                vertexArray.texCoordsBuffer
                    .put(texCoordsArray)
                    .position(0)

                vertexArray.indicesBuffer
                    ?.clear()
                vertexArray.indicesBuffer
                    ?.put(indicesArray)
                    ?.position(0)
            }
        }
        Live2DRenderState.pushViewPort(
            0,
            0,
            512,
            512,
        ) {
            Live2DRenderState.pushFrameBuffer {

                clipContext_2_drawableContextList.forEach { (clipContext, drawableContextList) ->
                    run {
                        check(
                            clipContext.calcClippedDrawTotalBounds(
                                drawableContextList
                            )
                        )

                        clipContext.createMatrixForMask()
                        clipContext.createMatrixForDraw()
                    }
                }

                clipContextList.groupBy { it.bufferIndex }
                    .forEach { (bufferIndex, clipContextList) ->
                        offscreenSurfaces[bufferIndex].let {
                            it.draw {
                                glClearColor(
                                    1.0f,
                                    1.0f,
                                    1.0f,
                                    1.0f
                                )
                                glClear(GL_COLOR_BUFFER_BIT)

                                clipContextList.forEach { clipContext ->
                                    for (maskIndex in clipContext.maskIndexArray) {
                                        val drawableContext = drawableContextArray[maskIndex]

                                        if (!drawableContext.vertexPositionDidChange) continue

                                        currentClipContextForSetupMask = clipContext
                                        drawMesh(
                                            drawableContext
                                        )
                                    }
                                }
                            }
                        }
                    }
            }


        }

    }

    lateinit var currentClipContextForSetupMask: ClipContext

    override fun draw() {
        val sortedDrawableContextArray = drawableContextArray.sortedWith(
            compareBy { it.renderOrder }
        )

        sortedDrawableContextArray.forEach { drawableContext ->
            drawMesh(drawableContext)
        }
    }

    fun drawMesh(
        drawableContext: DrawableContext,
    ) {

        when (state) {
            State.SETUP_MASK -> {
                Live2DShader.setupMask(
                    this,
                    drawableContext,
                    currentClipContextForSetupMask
                )
            }

            State.DRAW -> {
                drawableContext.clipContext?.let {
                    Live2DShader.drawMasked(
                        this,
                        drawableContext
                    )
                } ?: run {
                    Live2DShader.drawSimple(
                        this,
                        drawableContext
                    )
                }
            }
        }

        glDisable(GL_SCISSOR_TEST)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)
        glColorMask(
            true,
            true,
            true,
            true
        )

        if (drawableContext.isCulling) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }
        glFrontFace(GL_CCW)
        glDrawElements(
            GL_TRIANGLES,
            drawableContext.vertex.indicesArray.size,
            GL_UNSIGNED_SHORT,
            0
        )
    }
}

fun ClipContext.createMatrixForMask() {
    CubismMatrix44.create().apply {
        loadIdentity()
        // Layout0..1を、-1..1に変換
        translateRelative(-1.0f, -1.0f)
        scaleRelative(2.0f, 2.0f)

        // view to Layout0..1
        translateRelative(
            layoutBounds.x,
            layoutBounds.y
        )
        scaleRelative(
            layoutBounds.width / allClippedDrawRect.width,
            layoutBounds.height / allClippedDrawRect.height
        )
        translateRelative(
            -allClippedDrawRect.x,
            -allClippedDrawRect.y
        )
    }.also {
        matrixForMask.setMatrix(it)
    }
}

fun ClipContext.createMatrixForDraw() {
    CubismMatrix44.create().apply {
        loadIdentity()

        translateRelative(
            layoutBounds.x,
            layoutBounds.y
        )
        scaleRelative(
            layoutBounds.width / allClippedDrawRect.width,
            layoutBounds.height / allClippedDrawRect.height
        )
        translateRelative(
            -allClippedDrawRect.x,
            -allClippedDrawRect.y
        )
    }.also {
        matrixForDraw.setMatrix(it)
    }
}

fun ClipContext.calcClippedDrawTotalBounds(
    drawableContextList: List<DrawableContext>,
): Boolean {
    var clippedDrawTotalMinX = Float.Companion.MAX_VALUE
    var clippedDrawTotalMinY = Float.Companion.MAX_VALUE
    var clippedDrawTotalMaxX = -Float.Companion.MAX_VALUE
    var clippedDrawTotalMaxY = -Float.Companion.MAX_VALUE

    for (drawableContext in drawableContextList) {

        var minX = Float.Companion.MAX_VALUE
        var minY = Float.Companion.MAX_VALUE
        var maxX = -Float.Companion.MAX_VALUE
        var maxY = -Float.Companion.MAX_VALUE

        val loop = drawableContext.vertex.count * VERTEX_STEP
        var pi = VERTEX_OFFSET
        while (pi < loop) {
            val x = drawableContext.vertex.positionsArray[pi]
            val y = drawableContext.vertex.positionsArray[pi + 1]
            minX = min(minX, x)
            maxX = max(maxX, x)
            minY = min(minY, y)
            maxY = max(maxY, y)
            pi += VERTEX_STEP
        }

        if (minX == Float.Companion.MAX_VALUE) {
            continue
        }

        clippedDrawTotalMinX = min(clippedDrawTotalMinX, minX)
        clippedDrawTotalMaxX = max(clippedDrawTotalMaxX, maxX)
        clippedDrawTotalMinY = min(clippedDrawTotalMinY, minY)
        clippedDrawTotalMaxY = max(clippedDrawTotalMaxY, maxY)
    }

    if (clippedDrawTotalMinX == Float.Companion.MAX_VALUE) {
        allClippedDrawRect = csmRectF()
        return false
    } else {
        val w = clippedDrawTotalMaxX - clippedDrawTotalMinX
        val h = clippedDrawTotalMaxY - clippedDrawTotalMinY
        allClippedDrawRect = csmRectF(
            clippedDrawTotalMinX,
            clippedDrawTotalMinY,
            w,
            h
        )
        return true
    }
}
