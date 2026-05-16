package me.mikun.live2d.render

import org.lwjgl.opengl.GL46.*

object Live2DRenderState {
    fun pushViewPort(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        block: () -> Unit,
    ) {
        val lastViewport = IntArray(4)

        glGetIntegerv(
            GL_VIEWPORT,
            lastViewport,
        )

        glViewport(
            x,
            y,
            width,
            height,
        )

        block()

        glViewport(
            lastViewport[0],
            lastViewport[1],
            lastViewport[2],
            lastViewport[3],
        )
    }

    fun pushFrameBuffer(
        block: () -> Unit
    ) {
        val lastFBO = IntArray(1)

        glGetIntegerv(
            GL_FRAMEBUFFER_BINDING,
            lastFBO,
        )

        block()

        glBindFramebuffer(
            GL_FRAMEBUFFER,
            lastFBO[0]
        )
    }


}