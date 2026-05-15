package me.mikun.sandbox

import android.opengl.GLES20.GL_ARRAY_BUFFER
import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_DST_COLOR
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_ONE_MINUS_SRC_COLOR
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE1
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.GL_ZERO
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFuncSeparate
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glDetachShader
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetProgramInfoLog
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES30
import android.opengl.GLES30.glBindVertexArray
import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.CubismBlendMode
import me.mikun.live2d.ex.rendering.context.Live2DDrawableContext
import me.mikun.live2d.framework.utils.math.bottom
import me.mikun.live2d.framework.utils.math.right
import me.mikun.live2d.framework.utils.math.csmRectF

object Live2DShader {

    private fun setupVertexArray(
        vertex: Live2DDrawableContext.Vertex,
        vertexArray: GLRenderer.VertexArray,
    ) {
        with(vertex) {
            glBindVertexArray(
                vertexArray.vao
            )
            run {
                /*
                    position
                 */
                run {
                    GLES30.glBindBuffer(GL_ARRAY_BUFFER, vertexArray.vboPosition)
                    GLES30.glBufferSubData(
                        GL_ARRAY_BUFFER,
                        0,
                        vertex.positionsArray.size * 4,
                        vertexArray.positionsBuffer
                    )
                }
                /*
                    uv
                 */
                run {
                    GLES30.glBindBuffer(GL_ARRAY_BUFFER, vertexArray.vboTexCoord)
                    GLES30.glBufferSubData(
                        GL_ARRAY_BUFFER,
                        0,
                        vertex.texCoordsArray.size * 4,
                        vertexArray.texCoordsBuffer
                    )
                }
                /*
                    indices
                 */
                run {

                }
            }
        }
    }

    fun drawSimple(
        renderer: GLRenderer,
        drawableContext: Live2DDrawableContext,
    ) {
        val texture = renderer.drawableTextureArray[drawableContext.index]
        with(
            when {
                texture.isPremultipliedAlpha -> CubismShaderSet.MASKED_INVERTED_PREMULTIPLIED_ALPHA
                else -> CubismShaderSet.SIMPLE
            }
        ) {
            glUseProgram(shaderProgram.id)

            setupVertexArray(
                drawableContext.vertex,
                renderer.drawableVertexArrayArray[drawableContext.vertex.index]
            )

            /*
                modelMatrix (其实是mvp
             */
            run {
                glUniformMatrix4fv(
                    uniform(Uniform.MATRIX),
                    1,
                    false,
                    renderer.mvp.tr,
                    0
                )
            }
            /*
                texture0
            */
            run {
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(
                    GL_TEXTURE_2D,
                    texture.id
                )
                glUniform1i(
                    uniform(Uniform.TEXTURE0),
                    0
                )
            }
            /*
                baseColor multiplyColor screenColor
            */
            run {
                val baseColor = drawableContext.baseColor
                glUniform4f(
                    uniform(Uniform.BASE_COLOR),
                    baseColor.r,
                    baseColor.g,
                    baseColor.b,
                    baseColor.a
                )
            }
            run {
                val multiplyColor = drawableContext.multiplyColor
                glUniform4f(
                    uniform(Uniform.MULTIPLY_COLOR),
                    multiplyColor.r,
                    multiplyColor.g,
                    multiplyColor.b,
                    multiplyColor.a
                )
            }
            run {
                val screenColor = drawableContext.screenColor
                glUniform4f(
                    uniform(Uniform.SCREEN_COLOR),
                    screenColor.r,
                    screenColor.g,
                    screenColor.b,
                    screenColor.a
                )
            }

            var srcColor = 0
            var dstColor = 0
            var srcAlpha = 0
            var dstAlpha = 0

            when (drawableContext.blendMode) {
                CubismBlendMode.ADDITIVE -> {
                    srcColor = GL_ONE
                    dstColor = GL_ONE
                    srcAlpha = GL_ZERO
                    dstAlpha = GL_ONE
                }

                CubismBlendMode.MULTIPLICATIVE -> {
                    srcColor = GL_DST_COLOR
                    dstColor = GL_ONE_MINUS_SRC_ALPHA
                    srcAlpha = GL_ZERO
                    dstAlpha = GL_ONE

                }

                else -> {
                    srcColor = GL_ONE
                    dstColor = GL_ONE_MINUS_SRC_ALPHA
                    srcAlpha = GL_ONE
                    dstAlpha = GL_ONE_MINUS_SRC_ALPHA

                }
            }

            glBlendFuncSeparate(
                srcColor,
                dstColor,
                srcAlpha,
                dstAlpha
            )

        }

    }

    fun drawMasked(
        renderer: GLRenderer,
        drawableContext: Live2DDrawableContext,
    ) {
        val texture = renderer.drawableTextureArray[drawableContext.index]
        with(
            run {
                val isInvertedMask = drawableContext.isInvertedMask
                val isPremultipliedAlpha = texture.isPremultipliedAlpha

                return@run when {
                    !isInvertedMask && !isPremultipliedAlpha -> CubismShaderSet.MASKED
                    isInvertedMask && !isPremultipliedAlpha -> CubismShaderSet.MASKED_INVERTED
                    !isInvertedMask && isPremultipliedAlpha -> CubismShaderSet.MASKED_PREMULTIPLIED_ALPHA
                    isInvertedMask && isPremultipliedAlpha -> CubismShaderSet.MASKED_INVERTED_PREMULTIPLIED_ALPHA
                    else -> error("wrong shader setting!")
                }
            }
        ) {
            glUseProgram(shaderProgram.id)
            setupVertexArray(
                drawableContext.vertex,
                renderer.drawableVertexArrayArray[drawableContext.vertex.index]
            )

            run {
                val clipContext = renderer.drawableClipContextList[drawableContext.index]!!
                /*
                    texture1
                 */
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(
                    GL_TEXTURE_2D,
                    renderer.offscreenSurfaces[clipContext.bufferIndex].colorBuffer[0]
                )
                glUniform1i(
                    uniform(Uniform.TEXTURE1),
                    1
                )

                /*
                    clipMatrix
                 */
                // set up a matrix to convert View-coordinates to ClippingContext coordinates
                glUniformMatrix4fv(
                    uniform(Uniform.CLIP_MATRIX),
                    1,
                    false,
                    clipContext.matrixForDraw.tr,
                    0
                )

                /*
                    colorChannel
                 */
                run {
                    val colorChannel = clipContext.colorChannel
                    glUniform4f(
                        uniform(Uniform.CHANNEL_FLAG),
                        colorChannel.r,
                        colorChannel.g,
                        colorChannel.b,
                        colorChannel.a
                    )
                }
            }

            /*
                modelMatrix (其实是mvp
             */
            run {
                glUniformMatrix4fv(
                    uniform(Uniform.MATRIX),
                    1,
                    false,
                    renderer.mvp.tr,
                    0
                )
            }
            /*
                texture0
            */
            run {
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(
                    GL_TEXTURE_2D,
                    texture.id
                )
                glUniform1i(
                    uniform(Uniform.TEXTURE0),
                    0
                )
            }
            /*
                baseColor multiplyColor screenColor
            */
            run {
                val baseColor = drawableContext.baseColor
                glUniform4f(
                    uniform(Uniform.BASE_COLOR),
                    baseColor.r,
                    baseColor.g,
                    baseColor.b,
                    baseColor.a
                )
            }
            run {
                val multiplyColor = drawableContext.multiplyColor
                glUniform4f(
                    uniform(Uniform.MULTIPLY_COLOR),
                    multiplyColor.r,
                    multiplyColor.g,
                    multiplyColor.b,
                    multiplyColor.a
                )
            }
            run {
                val screenColor = drawableContext.screenColor
                glUniform4f(
                    uniform(Uniform.SCREEN_COLOR),
                    screenColor.r,
                    screenColor.g,
                    screenColor.b,
                    screenColor.a
                )
            }

            var srcColor = 0
            var dstColor = 0
            var srcAlpha = 0
            var dstAlpha = 0

            when (drawableContext.blendMode) {
                CubismBlendMode.ADDITIVE -> {
                    srcColor = GL_ONE
                    dstColor = GL_ONE
                    srcAlpha = GL_ZERO
                    dstAlpha = GL_ONE
                }

                CubismBlendMode.MULTIPLICATIVE -> {
                    srcColor = GL_DST_COLOR
                    dstColor = GL_ONE_MINUS_SRC_ALPHA
                    srcAlpha = GL_ZERO
                    dstAlpha = GL_ONE

                }

                else -> {
                    srcColor = GL_ONE
                    dstColor = GL_ONE_MINUS_SRC_ALPHA
                    srcAlpha = GL_ONE
                    dstAlpha = GL_ONE_MINUS_SRC_ALPHA

                }
            }

            glBlendFuncSeparate(
                srcColor,
                dstColor,
                srcAlpha,
                dstAlpha
            )

        }
    }

    fun setupMask(
        renderer: GLRenderer,
        drawableContext: Live2DDrawableContext,
        clipContext: ALive2DRenderer.PreClip.ClipContext,
    ) {
        val texture = renderer.drawableTextureArray[drawableContext.index]

        with(CubismShaderSet.SETUP_MASK) {
            glUseProgram(shaderProgram.id)
            setupVertexArray(
                drawableContext.vertex,
                renderer.drawableVertexArrayArray[drawableContext.vertex.index]
            )
            /*
                clipMatrix
            */
            run {
                glUniformMatrix4fv(
                    uniform(Uniform.CLIP_MATRIX),
                    1,
                    false,
                    clipContext.matrixForMask.tr,
                    0
                )
            }

            /*
                texture0
            */
            run {
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(
                    GL_TEXTURE_2D,
                    texture.id
                )
                glUniform1i(
                    uniform(Uniform.TEXTURE0),
                    0
                )
            }

            /*
                channelFlag
            */
            run {
                val colorChannel = clipContext.colorChannel
                glUniform4f(
                    uniform(Uniform.CHANNEL_FLAG),
                    colorChannel.r,
                    colorChannel.g,
                    colorChannel.b,
                    colorChannel.a
                )
            }

            /*
                baseColor 这里是边界
            */
            run {
                val rect: csmRectF = clipContext.layoutBounds
                glUniform4f(
                    uniform(Uniform.BASE_COLOR),
                    rect.x * 2.0f - 1.0f,
                    rect.y * 2.0f - 1.0f,
                    rect.right * 2.0f - 1.0f,
                    rect.bottom * 2.0f - 1.0f
                )
            }

            glBlendFuncSeparate(
                GL_ZERO,
                GL_ONE_MINUS_SRC_COLOR,
                GL_ZERO,
                GL_ONE_MINUS_SRC_ALPHA
            )
        }
    }

    enum class CubismShaderSet {
        SETUP_MASK(
            "shader/live2d/setup_mask.vert", "shader/live2d/setup_mask.frag",
            attributes = setOf(
                Attribute.POSITION,
                Attribute.TEXCOORD
            ),
            uniforms = setOf(
                Uniform.CLIP_MATRIX,
                Uniform.TEXTURE0,
                Uniform.CHANNEL_FLAG,
                Uniform.BASE_COLOR
            )
        ),
        SIMPLE(
            "shader/live2d/simple.vert", "shader/live2d/simple.frag",
            attributes = setOf(
                Attribute.POSITION,
                Attribute.TEXCOORD
            ),
            uniforms = setOf(
                Uniform.MATRIX,
                Uniform.TEXTURE0,
                Uniform.BASE_COLOR,
                Uniform.MULTIPLY_COLOR,
                Uniform.SCREEN_COLOR,
            ),
        ), // 用于未预乘alpha的 (stbi_load)
        PREMULTIPLIED_ALPHA(
            "shader/live2d/premultiplied_alpha.vert", "shader/live2d/premultiplied_alpha.frag",
            attributes = setOf(
                Attribute.POSITION,
                Attribute.TEXCOORD
            ),
            uniforms = setOf(
                Uniform.MATRIX,
                Uniform.TEXTURE0,
                Uniform.BASE_COLOR,
                Uniform.MULTIPLY_COLOR,
                Uniform.SCREEN_COLOR,
            ),
        ), // 用于已预乘alpha的 (android BitmapFactory.decodeStream)
        MASKED(
            "shader/live2d/masked.vert", "shader/live2d/masked.frag",
            attributes = setOf(
                Attribute.POSITION,
                Attribute.TEXCOORD
            ),
            uniforms = setOf(
                Uniform.MATRIX,
                Uniform.CLIP_MATRIX,
                Uniform.TEXTURE0,
                Uniform.TEXTURE1,
                Uniform.CHANNEL_FLAG,
                Uniform.BASE_COLOR,
                Uniform.MULTIPLY_COLOR,
                Uniform.SCREEN_COLOR,
            )
        ),
        MASKED_INVERTED(
            "shader/live2d/masked_inverted.vert", "shader/live2d/masked_inverted.frag",
            attributes = setOf(
                Attribute.POSITION,
                Attribute.TEXCOORD
            ),
            uniforms = setOf(
                Uniform.MATRIX,
                Uniform.CLIP_MATRIX,
                Uniform.TEXTURE0,
                Uniform.TEXTURE1,
                Uniform.CHANNEL_FLAG,
                Uniform.BASE_COLOR,
                Uniform.MULTIPLY_COLOR,
                Uniform.SCREEN_COLOR,
            )
        ),
        MASKED_PREMULTIPLIED_ALPHA(
            "shader/live2d/masked_premultiplied_alpha.vert",
            "shader/live2d/masked_premultiplied_alpha.frag",
            attributes = setOf(
                Attribute.POSITION,
                Attribute.TEXCOORD
            ),
            uniforms = setOf(
                Uniform.MATRIX,
                Uniform.CLIP_MATRIX,
                Uniform.TEXTURE0,
                Uniform.TEXTURE1,
                Uniform.CHANNEL_FLAG,
                Uniform.BASE_COLOR,
                Uniform.MULTIPLY_COLOR,
                Uniform.SCREEN_COLOR,
            )
        ),
        MASKED_INVERTED_PREMULTIPLIED_ALPHA(
            "shader/live2d/masked_inverted_premultiplied_alpha.vert",
            "shader/live2d/masked_inverted_premultiplied_alpha.frag",
            attributes = setOf(
                Attribute.POSITION,
                Attribute.TEXCOORD
            ),
            uniforms = setOf(
                Uniform.MATRIX,
                Uniform.CLIP_MATRIX,
                Uniform.TEXTURE0,
                Uniform.TEXTURE1,
                Uniform.CHANNEL_FLAG,
                Uniform.BASE_COLOR,
                Uniform.MULTIPLY_COLOR,
                Uniform.SCREEN_COLOR,
            )
        ),
        ;

        val shaderProgram: ShaderProgram
        private val attributeMap: Map<Attribute, Int>
        private val uniformMap: Map<Uniform, Int>

        constructor(
            vararg shaderPaths: String,
            attributes: Set<Attribute> = setOf(),
            uniforms: Set<Uniform> = setOf(),
        ) {
            shaderProgram = ShaderProgram(
                listOf(
                    Shader(
                        shaderPaths[0], GL_VERTEX_SHADER,
                    ),
                    Shader(
                        shaderPaths[1], GL_FRAGMENT_SHADER
                    )
                )
            )

            attributeMap = attributes.associateWith {
                glGetAttribLocation(
                    shaderProgram.id,
                    it.value
                )
            }

            uniformMap = uniforms.associateWith {
                glGetUniformLocation(
                    shaderProgram.id,
                    it.value
                )
            }
        }

        fun attribute(attribute: Attribute): Int =
            attributeMap[attribute]?.takeIf { it >= 0 }
                ?: error("Attribute[${attribute.value}] isn't exist!")

        fun uniform(uniform: Uniform): Int =
            uniformMap[uniform]?.takeIf { it >= 0 }
                ?: error("Uniform[${uniform.value}] isn't exist!")
    }

    enum class Attribute(
        val value: String,
    ) {
        POSITION("a_position"),
        TEXCOORD("a_texCoord"),
    }

    enum class Uniform(
        val value: String,
    ) {
        MATRIX("u_matrix"),
        CLIP_MATRIX("u_clipMatrix"),

        TEXTURE0("s_texture0"),
        TEXTURE1("s_texture1"),

        BASE_COLOR("u_baseColor"),
        MULTIPLY_COLOR("u_multiplyColor"),
        SCREEN_COLOR("u_screenColor"),

        CHANNEL_FLAG("u_channelFlag")
    }
}

class Shader {
    val byteArray: ByteArray
    val type: Int

    constructor(path: String, type: Int) {
        byteArray = Shader::class.java.classLoader.getResourceAsStream(path).readAllBytes()
        this.type = type
    }
}

class ShaderProgram(
    val shaders: List<Shader>,
) {
    val id: Int by lazy {
        fwCreateProgram().let { programId ->
            shaders.map { shader ->
                fwCreateShader(shader.type).apply {
                    fwCompileShader(
                        this,
                        String(shader.byteArray)
                    )
                }.apply {
                    glAttachShader(
                        programId,
                        this
                    )
                }
            }.also {
                fwLinkProgram(programId)
            }.forEach { shaderId ->
                glDetachShader(
                    programId,
                    shaderId
                )
                glDeleteShader(shaderId)
            }

            programId
        }
    }

}


fun fwCreateProgram(
    check: Boolean = true,
): Int {
    val id = glCreateProgram()

    if (check) {
        id.takeIf { it != 0 } ?: run {
            throw IllegalStateException("Could not create shader program")
        }
    }

    return id
}

fun fwCreateShader(
    type: Int,
    check: Boolean = true,
): Int {
    val shaderId = glCreateShader(type)

    if (check) {
        shaderId.takeIf { it != 0 } ?: run {
            throw IllegalStateException("Could not create shader: ${type}")
        }
    }

    return shaderId
}

fun fwCompileShader(
    shaderId: Int,
    content: String,
    check: Boolean = true,
) {
    glShaderSource(
        shaderId,
        content
    )
    glCompileShader(shaderId)

    if (check) {
        val status = IntArray(1)
        glGetShaderiv(
            shaderId,
            GL_COMPILE_STATUS,
            status,
            0
        )
        if (status[0] == 0) {
            throw IllegalStateException(
                "Could not compile shader: ${
                    glGetShaderInfoLog(
                        shaderId,
                    )
                }"
            )
        }
    }
}

fun fwLinkProgram(
    programId: Int,
    check: Boolean = true,
) {
    glLinkProgram(programId)

    if (check) {
        val status = IntArray(1)
        glGetProgramiv(
            programId,
            GL_LINK_STATUS,
            status,
            0
        )
        if (status[0] == 0) {
            throw IllegalStateException(
                "Could not link program: ${
                    glGetProgramInfoLog(
                        programId,
                    )
                }"
            )
        }
    }
}
