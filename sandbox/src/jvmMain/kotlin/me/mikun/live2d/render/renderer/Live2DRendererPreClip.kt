package me.mikun.live2d.render.renderer

import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.context.ALive2DModelClipContext
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext
import me.mikun.live2d.ex.rendering.context.Live2DDrawableContext
import me.mikun.live2d.render.context.Live2DModelClipContext
import me.mikun.live2d.render.context.Live2DModelRenderContext
import me.mikun.live2d.render.Live2DRenderState
import me.mikun.live2d.render.Live2DShader
import org.lwjgl.opengl.GL11

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
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        GL11.glDisable(GL11.GL_STENCIL_TEST)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glColorMask(
            true,
            true,
            true,
            true
        )

        if (drawableContext.isCulling) {
            GL11.glEnable(GL11.GL_CULL_FACE)
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE)
        }
        GL11.glFrontFace(GL11.GL_CCW)

        GL11.glDrawElements(
            GL11.GL_TRIANGLES,
            drawableContext.vertex.indicesArray.size,
            GL11.GL_UNSIGNED_SHORT,
            0
        )
    }

}