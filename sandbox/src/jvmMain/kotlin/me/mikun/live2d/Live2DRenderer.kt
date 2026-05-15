package me.mikun.live2d

import me.mikun.live2d.ex.rendering.context.ALive2DModelClipContext
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext
import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.context.Live2DDrawableContext
import org.lwjgl.opengl.GL46.GL_BLEND
import org.lwjgl.opengl.GL46.GL_CCW
import org.lwjgl.opengl.GL46.GL_CULL_FACE
import org.lwjgl.opengl.GL46.GL_DEPTH_TEST
import org.lwjgl.opengl.GL46.GL_SCISSOR_TEST
import org.lwjgl.opengl.GL46.GL_STENCIL_TEST
import org.lwjgl.opengl.GL46.GL_TRIANGLES
import org.lwjgl.opengl.GL46.GL_UNSIGNED_SHORT
import org.lwjgl.opengl.GL46.glColorMask
import org.lwjgl.opengl.GL46.glDisable
import org.lwjgl.opengl.GL46.glDrawElements
import org.lwjgl.opengl.GL46.glEnable
import org.lwjgl.opengl.GL46.glFrontFace

class Live2DRenderer : ALive2DRenderer.PreClip(
    pushViewportFun = Live2DRenderState::pushViewPort,
    pushFrameBufferFun = Live2DRenderState::pushFrameBuffer,
) {

    override fun setupMaskDraw(
        renderContext: ALive2DModelRenderContext,
        drawableContext: Live2DDrawableContext,
        drawableClipContext: ClipContext,
    ) {
        Live2DShader.setupMask(
            renderContext as Live2DModelRenderContext,
            drawableContext,
            drawableClipContext
        )

        drawMesh(
            drawableContext
        )
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

    override fun maskDraw(
        renderContext: ALive2DModelRenderContext,
        clipContext: ALive2DModelClipContext,
        drawableContext: Live2DDrawableContext,
    ) {
        Live2DShader.drawMasked(
            renderContext as Live2DModelRenderContext,
            clipContext as Live2DModelClipContext,
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

class TestRenderer: ALive2DRenderer.JustDraw() {
    override fun maskDraw(
        renderContext: ALive2DModelRenderContext,
        drawableContext: Live2DDrawableContext,
    ) {
        val offscreenSurfaces: Array<Live2DOffscreenSurface> = Array(8) {
            Live2DOffscreenSurface().apply {
                createOffscreenSurface(
                    512.0f, 512.0f
                )
            }
        }

        Live2DShader.drawMasked(
            renderContext as Live2DModelRenderContext,
            drawableContext
        )

        drawMesh(
            drawableContext
        )
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

