package com.live2d.sdk.cubism.framework.rendering

import org.lwjgl.opengl.GL46.*
import java.nio.ByteBuffer

actual fun ACubismOffscreenSurface.Companion.create(): ACubismOffscreenSurface {
    return CubismOffscreenSurface()
}

class CubismOffscreenSurface : ACubismOffscreenSurface() {

    override fun beginDraw() {
        if (renderTexture == null) {
            return
        }

        // 存储旧的 fbo
        glGetIntegerv(
            GL_FRAMEBUFFER_BINDING,
            oldFBO
        )

        glBindFramebuffer(GL_FRAMEBUFFER, renderTexture!![0])
    }

    override fun endDraw() {
        if (renderTexture == null) {
            return
        }

        glBindFramebuffer(GL_FRAMEBUFFER, oldFBO[0])
    }

    fun clear(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    ) {
        glClearColor(
            r,
            g,
            b,
            a
        )
        glClear(GL_COLOR_BUFFER_BIT)
    }

    // TODO:: check and recreate
    override fun createOffscreenSurface(x: Float, y: Float) {
        destroyOffscreenSurface()

        val ret = IntArray(size = 1)

        colorBuffer[0] = glGenTextures()

        glBindTexture(GL_TEXTURE_2D, colorBuffer[0])
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            x.toInt(),
            y.toInt(),
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            null as ByteBuffer?
        )

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        val tmpFBO = IntArray(1)
        glGetIntegerv(GL_FRAMEBUFFER_BINDING, tmpFBO)

        ret[0] = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, ret[0]);
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D,
            this.colorBuffer[0],
            0
        );
        glBindFramebuffer(GL_FRAMEBUFFER, tmpFBO[0]);

        this.renderTexture = IntArray(size = 1)
        this.renderTexture?.let { it[0] = ret[0] }
        bufferWidth = x.toInt()
        bufferHeight = y.toInt()

    }

    fun destroyOffscreenSurface() {
        if (renderTexture != null) {
            glDeleteFramebuffers(renderTexture!!);
            renderTexture = null;
        }
    }

    fun isSameSize(width: Float, height: Float): Boolean {
        return (width.toInt() == bufferWidth) && (height.toInt() == bufferHeight)
    }

    var renderTexture: IntArray? = null


    private var oldFBO: IntArray = IntArray(1)

    private var bufferWidth = 0

    private var bufferHeight = 0


//    /**
//     * Whether the color buffer is the one set by the argument
//     */
//    private var isColorBufferInherited = false
}