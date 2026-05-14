package me.mikun.sandbox

import android.opengl.GLES20.GL_CLAMP_TO_EDGE
import android.opengl.GLES20.GL_COLOR_ATTACHMENT0
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_RGBA
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TEXTURE_WRAP_T
import android.opengl.GLES20.GL_UNSIGNED_BYTE
import android.opengl.GLES20.glBindFramebuffer
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDeleteFramebuffers
import android.opengl.GLES20.glFramebufferTexture2D
import android.opengl.GLES20.glGenFramebuffers
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glTexImage2D
import android.opengl.GLES20.glTexParameteri
import me.mikun.live2d.ex.rendering.ALive2DOffscreenSurface
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

    override fun clear(
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

    fun createOffscreenSurface(width: Float, height: Float) {
        destroyOffscreenSurface()

        val ret = IntArray(size = 1)

        colorBuffer = IntArray(size = 1)

        glGenTextures(1, colorBuffer, 0)

        glBindTexture(
            GL_TEXTURE_2D,
            colorBuffer[0]
        )
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width.toInt(),
            height.toInt(),
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            null as ByteBuffer?
        )

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_S,
            GL_CLAMP_TO_EDGE
        )
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_WRAP_T,
            GL_CLAMP_TO_EDGE
        )
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR
        )
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_LINEAR
        )
        glBindTexture(
            GL_TEXTURE_2D,
            0
        )

        Live2DRenderState.pushFrameBuffer {
            glGenFramebuffers(1, ret, 0)

            glBindFramebuffer(
                GL_FRAMEBUFFER,
                ret[0]
            )

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
        this@Live2DOffscreenSurface.width = width.toInt()
        this@Live2DOffscreenSurface.height = height.toInt()

    }

    fun destroyOffscreenSurface() {
        if (renderTexture != null) {
            glDeleteFramebuffers(1, renderTexture!!, 0)
            renderTexture = null
        }
    }

    var colorBuffer: IntArray = IntArray(1)

    var renderTexture: IntArray? = null

    private var width = 0
    private var height = 0
}