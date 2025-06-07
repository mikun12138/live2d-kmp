package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_OFFSET
import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_STEP
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.type.csmRectF
import org.lwjgl.opengl.GL46.*
import kotlin.math.max
import kotlin.math.min

actual fun ALive2DRenderer.Companion.create(
    model: Live2DModel,
    offScreenBufferCount: Int,
): ALive2DRenderer {
    return Live2DRenderer(
        model,
        offScreenBufferCount
    )
}

class Live2DRenderer(
    model: Live2DModel,
    offScreenBufferCount: Int,
) : ALive2DRenderer(
    model,
    offScreenBufferCount,
) {

    /*
        lazy cache
    */
    private val clipContext_2_drawableContextList: Map<ClipContext, List<DrawableContext>> by lazy {
        clipContextList.associateWith { clipContext -> drawableContextArray.filter { it.clipContext === clipContext } }
    }

    override fun setupMask() {

        glViewport(
            0,
            0,
            512,
            512,
        )
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
                    it.beginDraw()

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
                    it.endDraw()
                }
            }

        glViewport(
            Live2DRendererProfile.lastViewport[0],
            Live2DRendererProfile.lastViewport[1],
            Live2DRendererProfile.lastViewport[2],
            Live2DRendererProfile.lastViewport[3],
        )
    }

    override fun draw() {
        val sortedDrawableContextArray = drawableContextArray.sortedWith(
            compareBy { it.renderOrder }
        )

        sortedDrawableContextArray.forEach { drawableContext ->
            drawMesh(drawableContext)
        }
    }

    lateinit var currentClipContextForSetupMask: ClipContext

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
            drawableContext.vertex.indices
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
            val x = drawableContext.vertex.positions[pi]
            val y = drawableContext.vertex.positions[pi + 1]
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
