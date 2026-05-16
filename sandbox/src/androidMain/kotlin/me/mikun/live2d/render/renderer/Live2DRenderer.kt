package me.mikun.live2d.render.renderer

import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_CCW
import android.opengl.GLES20.GL_CULL_FACE
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.GL_SCISSOR_TEST
import android.opengl.GLES20.GL_STENCIL_TEST
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_SHORT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glColorMask
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glDrawElements
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glFrontFace
import android.opengl.GLES20.glStencilFunc
import android.opengl.GLES20.glStencilMask
import android.opengl.GLES20.glStencilOp
import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext
import me.mikun.live2d.ex.rendering.context.Live2DDrawableContext
import me.mikun.live2d.render.Live2DShader
import me.mikun.live2d.render.context.Live2DModelRenderContext
import javax.microedition.khronos.opengles.GL10.GL_ALWAYS
import javax.microedition.khronos.opengles.GL10.GL_EQUAL
import javax.microedition.khronos.opengles.GL10.GL_KEEP
import javax.microedition.khronos.opengles.GL10.GL_NOTEQUAL
import javax.microedition.khronos.opengles.GL10.GL_REPLACE
import javax.microedition.khronos.opengles.GL10.GL_STENCIL_BUFFER_BIT

class Live2DRenderer : ALive2DRenderer.JustDraw() {

    fun frame(
        renderContext: ALive2DModelRenderContext,
    ) {
        draw(renderContext)
    }

    fun draw(
        renderContext: ALive2DModelRenderContext,
    ) {
        val sortedDrawableContextArray = renderContext.drawableContextArray.sortedWith(
            compareBy { it.renderOrder }
        )

        sortedDrawableContextArray.forEach { drawableContext ->
            if (!drawableContext.isVisible) return@forEach

            if (drawableContext.masks.isNotEmpty()) {
                glClear(GL_STENCIL_BUFFER_BIT)

                glEnable(GL_STENCIL_TEST)
                glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)

                glColorMask(false, false, false, false)
                glStencilFunc(GL_ALWAYS, 1, 0xFF)
                glStencilMask(0xFF)
                drawableContext.masks.forEach { maskIndex ->
                    simpleDraw(
                        renderContext,
                        renderContext.drawableContextArray[maskIndex]
                    )
                }

                glColorMask(true, true, true, true)
                glStencilFunc(if (drawableContext.isInvertedMask) GL_NOTEQUAL else GL_EQUAL, 1, 0xFF)
                glStencilMask(0x00)
                simpleDraw(
                    renderContext,
                    drawableContext
                )
                glStencilMask(0xFF)

                glDisable(GL_STENCIL_TEST)

            } else {
                simpleDraw(
                    renderContext,
                    drawableContext
                )
            }
        }
    }

    override fun simpleDraw(
        renderContext: ALive2DModelRenderContext,
        drawableContext: Live2DDrawableContext,
    ) {
        Live2DShader.drawSimple(
            renderContext as Live2DModelRenderContext,
            drawableContext
        )

        drawMesh(
            drawableContext
        )
    }

    private fun drawMesh(
        drawableContext: Live2DDrawableContext,
    ) {
        glDisable(GL_SCISSOR_TEST)
//        glDisable(GL_STENCIL_TEST)
        glDisable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)

        if (drawableContext.isCulling) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }
        glFrontFace(GL_CCW)

        if (drawableContext.vertex.indicesArray.isEmpty()) {
            return
        }
        glDrawElements(
            GL_TRIANGLES,
            drawableContext.vertex.indicesArray.size,
            GL_UNSIGNED_SHORT,
            0
        )
    }

}

