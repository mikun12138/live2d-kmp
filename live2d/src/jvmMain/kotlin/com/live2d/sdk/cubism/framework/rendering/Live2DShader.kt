package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.ex.rendering.ClipContext
import com.live2d.sdk.cubism.ex.rendering.ClipContext.Companion.CHANNEL_FLAGS
import com.live2d.sdk.cubism.ex.rendering.CubismBlendMode
import com.live2d.sdk.cubism.ex.rendering.DrawableContext
import com.live2d.sdk.cubism.framework.type.bottom
import com.live2d.sdk.cubism.framework.type.right
import com.live2d.sdk.cubism.framework.type.csmRectF
import org.lwjgl.opengl.GL46.*

object Live2DShader {

    private fun setupVertexArray(
        vertex: DrawableContext.Vertex,
        vertexArray: Live2DRenderer.VertexArray,
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

                }
                /*
                    uv
                 */
                run {
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
        renderer: Live2DRenderer,
        drawableContext: DrawableContext,
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
                    false,
                    renderer.mvp.tr,
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
        renderer: Live2DRenderer,
        drawableContext: DrawableContext,
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

            /*
                texture1
             */
            glActiveTexture(GL_TEXTURE1)
            glBindTexture(
                GL_TEXTURE_2D,
                renderer.offscreenSurfaces[drawableContext.clipContext!!.bufferIndex].colorBuffer[0]
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
                false,
                drawableContext.clipContext!!.matrixForDraw.tr,
            )

            /*
                colorChannel
             */
            run {
                val colorChannel =
                    CHANNEL_FLAGS[drawableContext.clipContext!!.layoutChannelIndex]
                glUniform4f(
                    uniform(Uniform.CHANNEL_FLAG),
                    colorChannel.r,
                    colorChannel.g,
                    colorChannel.b,
                    colorChannel.a
                )
            }
            /*
                modelMatrix (其实是mvp
             */
            run {
                glUniformMatrix4fv(
                    uniform(Uniform.MATRIX),
                    false,
                    renderer.mvp.tr,
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
        renderer: Live2DRenderer,
        drawableContext: DrawableContext,
        clipContext: ClipContext,
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
                    false,
                    clipContext.matrixForMask.tr
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
                val colorChannel =
                    CHANNEL_FLAGS[clipContext.layoutChannelIndex]
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
            "live2d/setup_mask.vert", "live2d/setup_mask.frag",
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
            "live2d/simple.vert", "live2d/simple.frag",
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
            "live2d/premultiplied_alpha.vert", "live2d/premultiplied_alpha.frag",
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
            "live2d/masked.vert", "live2d/masked.frag",
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
            "live2d/masked_inverted.vert", "live2d/masked_inverted.frag",
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
            "live2d/masked_premultiplied_alpha.vert", "live2d/masked_premultiplied_alpha.frag",
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
            "live2d/masked_inverted_premultiplied_alpha.vert",
            "live2d/masked_inverted_premultiplied_alpha.frag",
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
        glGetShaderi(
            shaderId,
            GL_COMPILE_STATUS
        ).takeIf { it != 0 } ?: run {
            throw IllegalStateException(
                "Could not compile shader: ${
                    glGetShaderInfoLog(
                        shaderId,
                        1024
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
        glGetProgrami(
            programId,
            GL_LINK_STATUS
        ).takeIf { it != 0 } ?: run {
            throw IllegalStateException(
                "Could not link program: ${
                    glGetProgramInfoLog(
                        programId,
                        1024
                    )
                }"
            )
        }
    }
}
