package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.ex.rendering.ALive2DTexture
import com.live2d.sdk.cubism.framework.model.Live2DModel
import org.lwjgl.opengl.GL46.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

class Live2DTexture : ALive2DTexture {
    constructor(
        model: Live2DModel,
        textureIndex: Int,
        bytes: ByteArray,
    ) : super(
        model,
        textureIndex,
        MemoryStack.stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            val channels = stack.mallocInt(1)
            STBImage.stbi_load_from_memory(
                ByteBuffer.allocateDirect(bytes.size).put(bytes).flip(),
                width,
                height,
                channels,
                4
            ).let { buffer ->
                glGenTextures().apply {
                    glBindTexture(
                        GL_TEXTURE_2D,
                        this
                    )
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MIN_FILTER,
                        GL_NEAREST
                    )
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MAG_FILTER,
                        GL_NEAREST
                    )
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_WRAP_S,
                        GL_CLAMP_TO_BORDER
                    )
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_WRAP_T,
                        GL_CLAMP_TO_BORDER
                    )
                    glTexParameterfv(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_BORDER_COLOR,
                        floatArrayOf(
                            0.0f,
                            0.0f,
                            0.0f,
                            0.0f
                        )
                    )
                    glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGBA,
                        width.get(),
                        height.get(),
                        0,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        buffer
                    )
                    glGenerateMipmap(GL_TEXTURE_2D)
                }
            }
        },
        isPremultipliedAlpha = false
    )
}
