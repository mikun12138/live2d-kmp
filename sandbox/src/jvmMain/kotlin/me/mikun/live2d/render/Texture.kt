package me.mikun.live2d.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class Texture {
    val id: Int
    val isPremultipliedAlpha: Boolean = false

    private constructor(bytes: ByteArray) {
        id = MemoryStack.stackPush().use { stack ->
            val width = stack.mallocInt(1)
            val height = stack.mallocInt(1)
            val channels = stack.mallocInt(1)
            STBImage.stbi_load_from_memory(
                MemoryUtil.memAlloc(bytes.size).put(bytes).flip(),
                width,
                height,
                channels,
                4
            ).let { buffer ->
                GL11.glGenTextures().apply {
                    GL11.glBindTexture(
                        GL11.GL_TEXTURE_2D,
                        this
                    )
                    GL11.glTexParameteri(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_MIN_FILTER,
                        GL11.GL_NEAREST
                    )
                    GL11.glTexParameteri(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_MAG_FILTER,
                        GL11.GL_NEAREST
                    )
                    GL11.glTexParameteri(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_WRAP_S,
                        GL13.GL_CLAMP_TO_BORDER
                    )
                    GL11.glTexParameteri(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_WRAP_T,
                        GL13.GL_CLAMP_TO_BORDER
                    )
                    GL11.glTexParameterfv(
                        GL11.GL_TEXTURE_2D,
                        GL11.GL_TEXTURE_BORDER_COLOR,
                        floatArrayOf(
                            0.0f,
                            0.0f,
                            0.0f,
                            0.0f
                        )
                    )
                    GL11.glTexImage2D(
                        GL11.GL_TEXTURE_2D,
                        0,
                        GL11.GL_RGBA,
                        width.get(),
                        height.get(),
                        0,
                        GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE,
                        buffer
                    )
                    GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
                }
            }
        }
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