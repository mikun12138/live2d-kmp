package me.mikun.live2d.render.renderer

import android.opengl.GLES20
import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.context.ALive2DModelClipContext
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext
import me.mikun.live2d.ex.rendering.context.Live2DDrawableContext
import me.mikun.live2d.render.Live2DRenderState
import me.mikun.live2d.render.Live2DShader
import me.mikun.live2d.render.context.Live2DModelClipContext
import me.mikun.live2d.render.context.Live2DModelRenderContext

class Live2DRendererPreClip : ALive2DRenderer.PreClip(
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
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST)
        GLES20.glDisable(GLES20.GL_STENCIL_TEST)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glColorMask(
            true,
            true,
            true,
            true
        )

        if (drawableContext.isCulling) {
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        } else {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        }
        GLES20.glFrontFace(GLES20.GL_CCW)

        if (drawableContext.vertex.indicesArray.isEmpty()) {
            return
        }
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            drawableContext.vertex.indicesArray.size,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )
    }


}