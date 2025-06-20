package me.mikun.live2d

import me.mikun.live2d.framework.math.CubismMatrix44
import me.mikun.live2d.ex.model.ALive2DUserModel
import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.Live2DDrawableContext
import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.opengl.GL11.GL_RGBA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_TEXTURE_BORDER_COLOR
import org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameterfv
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.opengl.GL30.glMapBufferRange
import org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT
import org.lwjgl.opengl.GL46.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL46.GL_BLEND
import org.lwjgl.opengl.GL46.GL_CCW
import org.lwjgl.opengl.GL46.GL_CULL_FACE
import org.lwjgl.opengl.GL46.GL_DEPTH_TEST
import org.lwjgl.opengl.GL46.GL_ELEMENT_ARRAY_BUFFER
import org.lwjgl.opengl.GL46.GL_FLOAT
import org.lwjgl.opengl.GL46.GL_MAP_PERSISTENT_BIT
import org.lwjgl.opengl.GL46.GL_MAP_WRITE_BIT
import org.lwjgl.opengl.GL46.GL_SCISSOR_TEST
import org.lwjgl.opengl.GL46.GL_STENCIL_TEST
import org.lwjgl.opengl.GL46.GL_TRIANGLES
import org.lwjgl.opengl.GL46.GL_UNSIGNED_SHORT
import org.lwjgl.opengl.GL46.glBindBuffer
import org.lwjgl.opengl.GL46.glBindVertexArray
import org.lwjgl.opengl.GL46.glBufferStorage
import org.lwjgl.opengl.GL46.glColorMask
import org.lwjgl.opengl.GL46.glDisable
import org.lwjgl.opengl.GL46.glDrawElements
import org.lwjgl.opengl.GL46.glEnable
import org.lwjgl.opengl.GL46.glEnableVertexAttribArray
import org.lwjgl.opengl.GL46.glFrontFace
import org.lwjgl.opengl.GL46.glGenBuffers
import org.lwjgl.opengl.GL46.glGenVertexArrays
import org.lwjgl.opengl.GL46.glVertexAttribPointer
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Live2DRenderer(
    userModel: ALive2DUserModel,
    offScreenBufferCount: Int,
) : ALive2DRenderer.PreClip(
    userModel,
    offScreenBufferCount,
    pushViewportFun = Live2DRenderState::pushViewPort,
    pushFrameBufferFun = Live2DRenderState::pushFrameBuffer,
) {

    lateinit var mvp: FloatArray

    override val offscreenSurfaces: Array<Live2DOffscreenSurface> = Array(offScreenBufferCount) {
        Live2DOffscreenSurface().apply {
            createOffscreenSurface(
                512.0f, 512.0f
            )
        }
    }

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

    val drawableTextureArray: Array<Texture> = Array(userModel.model.drawableCount) {
        val drawableContext = drawableContextArray[it]
        Texture.create(
            drawableContext.textureIndex,
            userModel.textures[drawableContext.textureIndex]
        )
    }

    class VertexArray {
        var vao: Int = -1
        var vboPosition: Int = -1
        lateinit var positionsBuffer: FloatBuffer
        var vboTexCoord: Int = -1
        lateinit var texCoordsBuffer: FloatBuffer
        var ebo: Int = -1
        var indicesBuffer: ShortBuffer? = null
    }

    val drawableVertexArrayArray: Array<VertexArray> = Array(userModel.model.drawableCount) {
        val drawableContext = drawableContextArray[it]
        VertexArray().apply {
            vao = glGenVertexArrays()

            glBindVertexArray(vao)
            drawableContext.vertex.apply {
                vboPosition = glGenBuffers()
                glBindBuffer(GL_ARRAY_BUFFER, vboPosition)
                glBufferStorage(
                    GL_ARRAY_BUFFER,
                    positionsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )
                positionsBuffer = glMapBufferRange(
                    GL_ARRAY_BUFFER,
                    0,
                    positionsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )!!.asFloatBuffer()
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
                glEnableVertexAttribArray(0)
            }

            drawableContext.vertex.apply {
                vboTexCoord = glGenBuffers()
                glBindBuffer(GL_ARRAY_BUFFER, vboTexCoord)
                glBufferStorage(
                    GL_ARRAY_BUFFER,
                    texCoordsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )
                texCoordsBuffer = glMapBufferRange(
                    GL_ARRAY_BUFFER,
                    0,
                    texCoordsArray.size * Float.SIZE_BYTES.toLong(),
                    GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                )!!.asFloatBuffer()
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
                glEnableVertexAttribArray(1)
            }

            drawableContext.vertex.apply {
                ebo = glGenBuffers()
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
                if (indicesArray.isNotEmpty()) {
                    glBufferStorage(
                        GL_ELEMENT_ARRAY_BUFFER,
                        (indicesArray.size * Short.SIZE_BYTES).toLong(),
                        GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                    )
                    indicesBuffer = glMapBufferRange(
                        GL_ELEMENT_ARRAY_BUFFER,
                        0,
                        indicesArray.size * Short.SIZE_BYTES.toLong(),
                        GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT
                    )!!.asShortBuffer()
                }
            }

            glBindVertexArray(0)
        }
    }


    fun frame(mvp: FloatArray) {
        this.mvp = mvp
        doFrame()
    }

    override fun doUpdateData() {
        super.doUpdateData()
        drawableVertexArrayArray.forEachIndexed { index, vertexArray ->
            with(drawableContextArray[index].vertex) {
                vertexArray.positionsBuffer
                    .clear()
                vertexArray.positionsBuffer
                    .put(positionsArray)
                    .position(0)

                vertexArray.texCoordsBuffer
                    .clear()
                vertexArray.texCoordsBuffer
                    .put(texCoordsArray)
                    .position(0)

                vertexArray.indicesBuffer
                    ?.clear()
                vertexArray.indicesBuffer
                    ?.put(indicesArray)
                    ?.position(0)
            }
        }
    }

    override fun setupMaskDraw(
        drawableContext: Live2DDrawableContext,
        clipContextForSetupMask: ClipContext,
    ) {
        Live2DShader.setupMask(
            this,
            drawableContext,
            clipContextForSetupMask
        )

        drawMesh(
            drawableContext
        )
    }

    override fun simpleDraw(
        drawableContext: Live2DDrawableContext,
    ) {
        Live2DShader.drawSimple(
            this,
            drawableContext
        )

        drawMesh(
            drawableContext
        )
    }

    override fun maskDraw(
        drawableContext: Live2DDrawableContext,
    ) {
        Live2DShader.drawMasked(
            this,
            drawableContext
        )

        drawMesh(
            drawableContext
        )
    }

    private fun drawMesh(
        drawableContext: Live2DDrawableContext,
    ) {
        glDisable(GL_SCISSOR_TEST)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)
        glColorMask(
            true,
            true,
            true,
            true
        )

        if (drawableContext.isCulling) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }
        glFrontFace(GL_CCW)

        glDrawElements(
            GL_TRIANGLES,
            drawableContext.vertex.indicesArray.size,
            GL_UNSIGNED_SHORT,
            0
        )
    }
}
