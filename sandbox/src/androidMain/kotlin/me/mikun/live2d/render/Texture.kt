package me.mikun.live2d.render

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

class Texture {
    val id: Int
    val isPremultipliedAlpha: Boolean = false

    private constructor(bytes: ByteArray) {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        id = textureIds[0]

        if (id == 0) {
            throw RuntimeException("Error generating texture name.")
        }

        val bitmap = BitmapFactory.decodeByteArray(
            bytes, 0, bytes.size,
            BitmapFactory.Options().apply {
                inPremultiplied = false
            }
        )

        GLES20.glBindTexture(
            GLES20.GL_TEXTURE_2D,
            id
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
        GLUtils.texImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            bitmap,
            0
        )
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
    }

    companion object {
        val textures: MutableMap<Int, Texture> = mutableMapOf()
        fun create(textureIndex: Int, bytes: ByteArray): Texture {
            return textures.getOrPut(textureIndex) {
                Texture(bytes)
            }
        }
    }
}