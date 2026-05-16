package me.mikun.live2d.render.context

import android.opengl.GLES20.GL_ARRAY_BUFFER
import android.opengl.GLES20.GL_DYNAMIC_DRAW
import android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_STATIC_DRAW
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glBufferData
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGenBuffers
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES30.glBindVertexArray
import android.opengl.GLES30.glGenVertexArrays
import me.mikun.live2d.ex.model.ALive2DUserModel
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext
import me.mikun.live2d.framework.utils.math.CubismMatrix44
import me.mikun.live2d.render.Texture
import me.mikun.live2d.render.VertexArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Live2DModelRenderContext(
    val userModel: ALive2DUserModel,
): ALive2DModelRenderContext(
    userModel
) {
    var mvp: CubismMatrix44 = CubismMatrix44()

    val drawableTextureArray: Array<Texture> by lazy {
        Array(userModel.model.drawableCount) {
            val drawableContext = drawableContextArray[it]
            Texture.Companion.create(
                drawableContext.textureIndex,
                userModel.textures[drawableContext.textureIndex]
            )
        }
    }

    val drawableVertexArrayArray: Array<VertexArray> by lazy {
        Array(userModel.model.drawableCount) {
            val drawableContext = drawableContextArray[it]
            VertexArray().apply {
                glGenVertexArrays(1, vaos, 0)

                vao = vaos[0]

                glBindVertexArray(vao)
                glGenBuffers(2, vbos, 0)
                drawableContext.vertex.apply {
                    vboPosition = vbos[0]

                    val dataSize = (positionsArray.size * 4).toLong()

                    glBindBuffer(GL_ARRAY_BUFFER, vboPosition)
                    positionsBuffer = ByteBuffer.allocateDirect(dataSize.toInt())
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
                    positionsBuffer.put(0, positionsArray)
                    glBufferData(
                        GL_ARRAY_BUFFER,
                        dataSize.toInt(),
                        positionsBuffer,
                        GL_DYNAMIC_DRAW
                    )

                    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
                    glEnableVertexAttribArray(0)
                }

                drawableContext.vertex.apply {
                    vboTexCoord = vbos[1]
                    val texSize = (texCoordsArray.size * 4).toLong()

                    glBindBuffer(GL_ARRAY_BUFFER, vboTexCoord)
                    texCoordsBuffer = ByteBuffer.allocateDirect(texSize.toInt())
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
                    texCoordsBuffer.put(0, texCoordsArray)
                    glBufferData(
                        GL_ARRAY_BUFFER,
                        texSize.toInt(),
                        texCoordsBuffer,
                        GL_DYNAMIC_DRAW
                    )

                    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
                    glEnableVertexAttribArray(1)
                }

                drawableContext.vertex.apply {
                    glGenBuffers(1, ebos, 0)
                    ebo = ebos[0]
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)

                    if (indicesArray.isNotEmpty()) {
                        val dataSize = (indicesArray.size * 2) // Short

                        indicesBuffer = ByteBuffer.allocateDirect(dataSize.toInt())
                            .order(ByteOrder.nativeOrder())
                            .asShortBuffer()
                        indicesBuffer!!.put(0, indicesArray)

                        glBufferData(
                            GL_ELEMENT_ARRAY_BUFFER,
                            dataSize,
                            indicesBuffer,
                            GL_STATIC_DRAW
                        )
                    }
                }

                glBindVertexArray(0)
            }
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