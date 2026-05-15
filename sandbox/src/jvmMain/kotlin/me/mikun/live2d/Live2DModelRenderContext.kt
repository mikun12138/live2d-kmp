package me.mikun.live2d

import me.mikun.live2d.ex.model.ALive2DUserModel
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext
import me.mikun.live2d.framework.utils.math.CubismMatrix44
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glBindBuffer
import org.lwjgl.opengl.GL15.glGenBuffers
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.lwjgl.opengl.GL30.glMapBufferRange
import org.lwjgl.opengl.GL44.GL_MAP_COHERENT_BIT
import org.lwjgl.opengl.GL44.GL_MAP_PERSISTENT_BIT
import org.lwjgl.opengl.GL44.glBufferStorage

class Live2DModelRenderContext(
    val userModel: ALive2DUserModel,
): ALive2DModelRenderContext(
    userModel
) {
    var mvp: CubismMatrix44 = CubismMatrix44()

    val drawableTextureArray: Array<Texture> = Array(userModel.model.drawableCount) {
        val drawableContext = drawableContextArray[it]
        Texture.create(
            drawableContext.textureIndex,
            userModel.textures[drawableContext.textureIndex]
        )
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

    override fun update() {
        super.update()

        val matrix = CubismMatrix44().apply {
            loadIdentity()
            scale(
                1080.0f / 1920.0f,
                1.0f
            )
        }
        CubismMatrix44.multiply(
            userModel.modelMatrix!!.tr,
            matrix.tr,
            matrix.tr
        )

        mvp = matrix

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
}