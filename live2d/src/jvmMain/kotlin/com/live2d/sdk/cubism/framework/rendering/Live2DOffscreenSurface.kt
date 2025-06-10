package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.ex.rendering.ALive2DOffscreenSurface
import com.live2d.sdk.cubism.framework.math.CubismVector2
import org.lwjgl.opengl.GL46.*
import java.nio.ByteBuffer


class Live2DOffscreenSurface : ALive2DOffscreenSurface() {
    override fun beginDraw(): Boolean {
        if (renderTexture == null) {
            return false
        }

        glBindFramebuffer(
            GL_FRAMEBUFFER,
            renderTexture!![0]
        )
        return true
    }

    override fun endDraw() {

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

    override fun createOffscreenSurface(displayBufferSize: CubismVector2) {
        destroyOffscreenSurface()

        val ret = IntArray(size = 1)

        colorBuffer = IntArray(size = 1)
        colorBuffer[0] = glGenTextures()

        glBindTexture(
            GL_TEXTURE_2D,
            colorBuffer[0]
        )
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            displayBufferSize.x.toInt(),
            displayBufferSize.y.toInt(),
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            null as ByteBuffer?
        )

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_S,
            GL_CLAMP_TO_EDGE
        );
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_T,
            GL_CLAMP_TO_EDGE
        );
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR
        );
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_LINEAR
        );
        glBindTexture(
            GL_TEXTURE_2D,
            0
        );

        Live2DRenderState.pushFrameBuffer {
            ret[0] = glGenFramebuffers();
            glBindFramebuffer(
                GL_FRAMEBUFFER,
                ret[0]
            );
            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D,
                this.colorBuffer[0],
                0
            )
        }

        this.renderTexture = IntArray(size = 1)
        this.renderTexture?.let { it[0] = ret[0] }
        bufferWidth = displayBufferSize.x.toInt()
        bufferHeight = displayBufferSize.y.toInt()

    }

    fun destroyOffscreenSurface() {
        if (renderTexture != null) {
            glDeleteFramebuffers(renderTexture!!);
            renderTexture = null;
        }
    }

    fun isValid() = renderTexture != null

    override fun isSameSize(bufferSize: CubismVector2): Boolean {
        val width = bufferSize.x.toInt()
        val height = bufferSize.y.toInt()
        return (width == bufferWidth) && (height == bufferHeight)
    }

    /**
     * texture as rendering target. It is called frame buffer.
     */
    var renderTexture: IntArray? = null

    /**
     * old frame buffer
     */
    private var oldFBO = IntArray(1)

    /**
     * width specified at Create() method
     */
    private var bufferWidth = 0

    /**
     * height specified at Create() method
     */
    private var bufferHeight = 0

//    /**
//     * Whether the color buffer is the one set by the argument
//     */
//    private var isColorBufferInherited = false
}