package me.mikun.live2d.render

import android.opengl.GLES10.glGetIntegerv
import android.opengl.GLES10.glViewport
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES20.GL_FRAMEBUFFER_BINDING
import android.opengl.GLES20.glBindFramebuffer
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL11.GL_VIEWPORT

object Live2DRenderState {
    fun pushViewPort(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        block: () -> Unit,
    ) {
        val lastViewport = IntBuffer.allocate(4)

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
        val lastFBO = IntBuffer.allocate(1)

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