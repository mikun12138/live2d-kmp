package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.type.csmRectF
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import org.lwjgl.opengl.GL46.*
import com.live2d.sdk.cubism.framework.rendering.Live2DShader.CubismShaderSet.Type.*
import com.live2d.sdk.cubism.framework.type.bottom
import com.live2d.sdk.cubism.framework.type.right


object Live2DShader {
    fun setupShaderProgramForDraw(
        renderer: Live2DRendererImpl,
        model: Live2DModel,
        index: Int,
    ) {
        var srcColor = 0
        var dstColor = 0
        var srcAlpha = 0
        var dstAlpha = 0

        val isMasked = renderer.clippingContextBufferForDraw != null
        val isInvertedMask = model.getDrawableInvertedMask(index)
        val isPremultipliedAlpha = renderer.isPremultipliedAlpha
        val type = when {
            !isMasked && !isPremultipliedAlpha -> SIMPLE
            isMasked && !isInvertedMask && !isPremultipliedAlpha -> MASKED
            isMasked && isInvertedMask && !isPremultipliedAlpha -> MASKED_INVERTED
            !isMasked && isPremultipliedAlpha -> PREMULTIPLIED_ALPHA
            isMasked && !isInvertedMask && isPremultipliedAlpha -> MASKED_PREMULTIPLIED_ALPHA
            isMasked && isInvertedMask && isPremultipliedAlpha -> MASKED_INVERTED_PREMULTIPLIED_ALPHA
            else -> null
        }

        with(
            when (model.getDrawableBlendMode(index)) {
                CubismBlendMode.ADDITIVE -> {
                    srcColor = GL_ONE
                    dstColor = GL_ONE
                    srcAlpha = GL_ZERO
                    dstAlpha = GL_ONE
                    when (type) {
                        SIMPLE -> ADD
                        MASKED -> ADD_MASKED
                        MASKED_INVERTED -> ADD_MASKED_INVERTED
                        PREMULTIPLIED_ALPHA -> ADD_PREMULTIPLIED_ALPHA
                        MASKED_PREMULTIPLIED_ALPHA -> ADD_MASKED_PREMULTIPLIED_ALPHA
                        MASKED_INVERTED_PREMULTIPLIED_ALPHA -> ADD_MASKED_INVERTED_PREMULTIPLIED_ALPHA
                        else -> null!!
                    }
                }

                CubismBlendMode.MULTIPLICATIVE -> {
                    srcColor = GL_DST_COLOR;
                    dstColor = GL_ONE_MINUS_SRC_ALPHA;
                    srcAlpha = GL_ZERO;
                    dstAlpha = GL_ONE;
                    when (type) {
                        SIMPLE -> MULT
                        MASKED -> MULT_MASKED
                        MASKED_INVERTED -> MULT_MASKED_INVERTED
                        PREMULTIPLIED_ALPHA -> MULT_PREMULTIPLIED_ALPHA
                        MASKED_PREMULTIPLIED_ALPHA -> MULT_MASKED_PREMULTIPLIED_ALPHA
                        MASKED_INVERTED_PREMULTIPLIED_ALPHA -> MULT_MASKED_INVERTED_PREMULTIPLIED_ALPHA
                        else -> null!!
                    }
                }

                else -> {
                    srcColor = GL_ONE;
                    dstColor = GL_ONE_MINUS_SRC_ALPHA;
                    srcAlpha = GL_ONE;
                    dstAlpha = GL_ONE_MINUS_SRC_ALPHA;
                    when (type) {
                        SIMPLE -> NORMAL
                        MASKED -> NORMAL_MASKED
                        MASKED_INVERTED -> NORMAL_MASKED_INVERTED
                        PREMULTIPLIED_ALPHA -> NORMAL_PREMULTIPLIED_ALPHA
                        MASKED_PREMULTIPLIED_ALPHA -> NORMAL_MASKED_PREMULTIPLIED_ALPHA
                        MASKED_INVERTED_PREMULTIPLIED_ALPHA -> NORMAL_MASKED_INVERTED_PREMULTIPLIED_ALPHA
                        else -> null!!
                    }
                }
            }
        ) {
            glUseProgram(this.type.shaderProgram.id)

            renderer.drawableInfoCachesHolder.let { drawableInfoCachesHolder ->
                /*
                    position
                 */
                drawableInfoCachesHolder?.setUpVertexArray(
                    index,
                    model.getDrawableVertexPositions(index)!!
                )?.also { vertexBuffer ->
                    glEnableVertexAttribArray(this.attributePositionLocation);
                    glVertexAttribPointer(
                        this.attributePositionLocation,
                        2,
                        GL_FLOAT,
                        false,
                        Float.SIZE_BYTES * 2,
                        vertexBuffer
                    );
                }

                /*
                    uv
                 */
                drawableInfoCachesHolder?.setUpUvArray(
                    index,
                    model.getDrawableVertexUVs(index)!!
                )?.also { uvBuffer ->
                    glEnableVertexAttribArray(this.attributeTexCoordLocation);
                    glVertexAttribPointer(
                        this.attributeTexCoordLocation,
                        2,
                        GL_FLOAT,
                        false,
                        Float.SIZE_BYTES * 2,
                        uvBuffer
                    )
                }
            }

            if (isMasked) {
                glActiveTexture(GL_TEXTURE1)

                // OffscreenSurfaceに描かれたテクスチャ
                val tex: Int =
                    renderer.offscreenSurfaces[renderer.clippingContextBufferForDraw!!.bufferIndex].colorBuffer[0]

                /*
                    texture1
                 */
                glBindTexture(
                    GL_TEXTURE_2D,
                    tex
                )
                glUniform1i(
                    this.samplerTexture1Location,
                    1
                )

                /*
                    clipMatrix
                 */
                // set up a matrix to convert View-coordinates to ClippingContext coordinates
                glUniformMatrix4fv(
                    this.uniformClipMatrixLocation,
                    false,
                    renderer.clippingContextBufferForDraw!!.matrixForDraw.tr,
                )

                /*
                    colorChannel
                 */
                // Set used color channel.
                val channelIndex: Int = renderer.clippingContextBufferForDraw!!.layoutChannelIndex
                val colorChannel: CubismTextureColor = renderer
                    .clippingContextBufferForDraw!!
                    .manager
                    .channelColors[channelIndex]
                glUniform4f(
                    this.uniformChannelFlagLocation,
                    colorChannel.r,
                    colorChannel.g,
                    colorChannel.b,
                    colorChannel.a
                )
            }

            /*
                texture0
             */
            // texture setting
            val textureId: Int = renderer.getBoundTextureId(
                model.getDrawableTextureIndex(index)
            )
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(
                GL_TEXTURE_2D,
                textureId
            )
            glUniform1i(
                this.samplerTexture0Location,
                0
            )

            /*
                modelMatrix
             */
            // coordinate transformation
            val matrix44 = renderer.mvpMatrix44
            glUniformMatrix4fv(
                this.uniformMatrixLocation,
                false,
                matrix44.tr,
            )


            /*
                baseColor multiplyColor screenColor
             */
            // ベース色の取得
            val baseColor = renderer.model.getModelColorWithOpacity(
                model.getDrawableOpacity(index)
            )
            val multiplyColor = model.model.drawableViews[index].multiplyColors!!
            val screenColor = model.model.drawableViews[index].screenColors!!
            glUniform4f(
                this.uniformBaseColorLocation,
                baseColor.r,
                baseColor.g,
                baseColor.b,
                baseColor.a
            )
            glUniform4f(
                this.uniformMultiplyColorLocation,
                multiplyColor[0],
                multiplyColor[1],
                multiplyColor[2],
                multiplyColor[3]
            )
            glUniform4f(
                this.uniformScreenColorLocation,
                screenColor[0],
                screenColor[1],
                screenColor[2],
                screenColor[3]
            )

            glBlendFuncSeparate(
                srcColor,
                dstColor,
                srcAlpha,
                dstAlpha
            )
        }
    }

    fun setupShaderProgramForMask(
        renderer: Live2DRendererImpl,
        model: Live2DModel,
        index: Int,
    ) {
        var srcColor = 0
        var dstColor = 0
        var srcAlpha = 0
        var dstAlpha = 0

        glUseProgram(SETUP_MASK.type.shaderProgram.id)

        renderer.getBoundTextureId(model.getDrawableTextureIndex(index)).let { textureId ->

            /*
                texture0
             */
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(
                GL_TEXTURE_2D,
                textureId
            )
            glUniform1i(
                SETUP_MASK.samplerTexture0Location,
                0
            )

            renderer.drawableInfoCachesHolder?.let { drawableInfoCachesHolder ->
                /*
                    position
                 */
                drawableInfoCachesHolder.setUpVertexArray(
                    index,
                    model.getDrawableVertexPositions(index)!!
                ).also { buffer ->
                    glEnableVertexAttribArray(SETUP_MASK.attributePositionLocation)
                    glVertexAttribPointer(
                        SETUP_MASK.attributePositionLocation,
                        2,
                        GL_FLOAT,
                        false,
                        Float.SIZE_BYTES * 2,
                        buffer
                    )
                }

                /*
                    uv
                 */
                drawableInfoCachesHolder.setUpUvArray(
                    index,
                    model.getDrawableVertexUVs(index)!!
                ).also { buffer ->
                    glEnableVertexAttribArray(SETUP_MASK.attributeTexCoordLocation)
                    glVertexAttribPointer(
                        SETUP_MASK.attributeTexCoordLocation,
                        2,
                        GL_FLOAT,
                        false,
                        Float.SIZE_BYTES * 2,
                        buffer
                    )
                }

                /*
                    colorChannel
                 */
                val channelIndex = renderer.clippingContextBufferForMask!!.layoutChannelIndex
                val colorChannel =
                    renderer.clippingContextBufferForMask!!.manager.channelColors[channelIndex]
                glUniform4f(
                    SETUP_MASK.uniformChannelFlagLocation,
                    colorChannel.r,
                    colorChannel.g,
                    colorChannel.b,
                    colorChannel.a
                )

                /*
                    clipMatrix
                 */
                glUniformMatrix4fv(
                    SETUP_MASK.uniformClipMatrixLocation,
                    false,
                    renderer.clippingContextBufferForMask!!.matrixForMask.tr,
                );

                val rect: csmRectF = renderer.clippingContextBufferForMask!!.layoutBounds

                /*
                    好像是个区域?
                 */
                glUniform4f(
                    SETUP_MASK.uniformBaseColorLocation,
                    rect.x * 2.0f - 1.0f,
                    rect.y * 2.0f - 1.0f,
                    rect.right * 2.0f - 1.0f,
                    rect.bottom * 2.0f - 1.0f
                )

                /*
                    multiplyColor screenColor
                 */
                val multiplyColor = model.model.drawableViews[index].multiplyColors!!
                val screenColor = model.model.drawableViews[index].screenColors!!
                glUniform4f(
                    SETUP_MASK.uniformMultiplyColorLocation,
                    multiplyColor[0],
                    multiplyColor[1],
                    multiplyColor[2],
                    multiplyColor[3]
                )
                glUniform4f(
                    SETUP_MASK.uniformScreenColorLocation,
                    screenColor[0],
                    screenColor[1],
                    screenColor[2],
                    screenColor[3]
                )

                srcColor = GL_ZERO;
                dstColor = GL_ONE_MINUS_SRC_COLOR;
                srcAlpha = GL_ZERO;
                dstAlpha = GL_ONE_MINUS_SRC_ALPHA;

                glBlendFuncSeparate(
                    srcColor,
                    dstColor,
                    srcAlpha,
                    dstAlpha
                );
            }
        }
    }


    val SETUP_MASK = CubismShaderSet(
        MASK,
    )
    val NORMAL = CubismShaderSet(
        SIMPLE,
    )
    val NORMAL_MASKED = CubismShaderSet(
        MASKED,
    )
    val NORMAL_MASKED_INVERTED = CubismShaderSet(
        MASKED_INVERTED
    )
    val NORMAL_PREMULTIPLIED_ALPHA = CubismShaderSet(
        PREMULTIPLIED_ALPHA,
    )
    val NORMAL_MASKED_PREMULTIPLIED_ALPHA = CubismShaderSet(
        MASKED_PREMULTIPLIED_ALPHA,
    )
    val NORMAL_MASKED_INVERTED_PREMULTIPLIED_ALPHA = CubismShaderSet(
        MASKED_INVERTED_PREMULTIPLIED_ALPHA
    )
    val ADD = CubismShaderSet(
        SIMPLE,
    )
    val ADD_MASKED = CubismShaderSet(
        MASKED,
    )
    val ADD_MASKED_INVERTED = CubismShaderSet(
        MASKED_INVERTED
    )
    val ADD_PREMULTIPLIED_ALPHA = CubismShaderSet(
        PREMULTIPLIED_ALPHA,
    )
    val ADD_MASKED_PREMULTIPLIED_ALPHA = CubismShaderSet(
        MASKED_PREMULTIPLIED_ALPHA,
    )
    val ADD_MASKED_INVERTED_PREMULTIPLIED_ALPHA = CubismShaderSet(
        MASKED_INVERTED_PREMULTIPLIED_ALPHA
    )
    val MULT = CubismShaderSet(
        SIMPLE,
    )
    val MULT_MASKED = CubismShaderSet(
        MASKED,
    )
    val MULT_MASKED_INVERTED = CubismShaderSet(
        MASKED_INVERTED
    )
    val MULT_PREMULTIPLIED_ALPHA = CubismShaderSet(
        PREMULTIPLIED_ALPHA,
    )
    val MULT_MASKED_PREMULTIPLIED_ALPHA = CubismShaderSet(
        MASKED_PREMULTIPLIED_ALPHA,
    )
    val MULT_MASKED_INVERTED_PREMULTIPLIED_ALPHA = CubismShaderSet(
        MASKED_INVERTED_PREMULTIPLIED_ALPHA
    )


    class CubismShaderSet(
        val type: Type,
    ) {
        enum class Type(
            val shaderProgram: ShaderProgram,
        ) {
            MASK(
                "live2d/setup_mask.vert",
                "live2d/setup_mask.frag"
            ),
            SIMPLE(
                "live2d/simple.vert",
                "live2d/simple.frag"
            ), // 用于未预乘alpha的 (stbi_load)
            MASKED(
                "live2d/masked.vert",
                "live2d/masked.frag"
            ),
            MASKED_INVERTED(
                "live2d/masked_inverted.vert",
                "live2d/masked_inverted.frag"
            ),
            PREMULTIPLIED_ALPHA(
                "live2d/premultiplied_alpha.vert",
                "live2d/premultiplied_alpha.frag"
            ), // 用于已预乘alpha的 (android BitmapFactory.decodeStream)
            MASKED_PREMULTIPLIED_ALPHA(
                "live2d/masked_premultiplied_alpha.vert",
                "live2d/masked_premultiplied_alpha.frag"
            ),
            MASKED_INVERTED_PREMULTIPLIED_ALPHA(
                "live2d/masked_inverted_premultiplied_alpha.vert",
                "live2d/masked_inverted_premultiplied_alpha.frag"
            ),
            ;

            constructor(
                vararg shaderPaths: String,
            ) : this(
                ShaderProgram(
                    listOf(
                        Shader(
                            shaderPaths[0], GL_VERTEX_SHADER,
                        ),
                        Shader(
                            shaderPaths[1], GL_FRAGMENT_SHADER
                        )
                    )
                )
            )
        }

        /**
         * Address of the variable to be passed to the shader program (Position)
         */
        var attributePositionLocation: Int = 0

        /**
         * Address of the variable to be passed to the shader program (TexCoord)
         */
        var attributeTexCoordLocation: Int = 0

        /**
         * Address of the variable to be passed to the shader program (Matrix)
         */
        var uniformMatrixLocation: Int = 0

        /**
         * Address of the variable to be passed to the shader program (ClipMatrix)
         */
        var uniformClipMatrixLocation: Int = 0

        /**
         * Address of the variable to be passed to the shader program (Texture0)
         */
        var samplerTexture0Location: Int = 0

        /**
         * Address of the variable to be passed to the shader program (Texture1)
         */
        var samplerTexture1Location: Int = 0

        /**
         * Address of the variable to be passed to the shader program (BaseColor)
         */
        var uniformBaseColorLocation: Int = 0

        /**
         * Address of the variable to be passed to the shader program (MultiplyColor)
         */
        var uniformMultiplyColorLocation: Int = 0

        /**
         * Address of the variable to be passed to the shader program (ScreenColor)
         */
        var uniformScreenColorLocation: Int = 0

        /**
         * Address of the variable to be passed to the shader program (ChannelFlag)
         */
        var uniformChannelFlagLocation: Int = 0

        init {
            attributePositionLocation = glGetAttribLocation(
                type.shaderProgram.id,
                "a_position"
            )
            attributeTexCoordLocation = glGetAttribLocation(
                type.shaderProgram.id,
                "a_texCoord"
            )
            uniformMatrixLocation = glGetUniformLocation(
                type.shaderProgram.id,
                "u_matrix"
            )
            uniformClipMatrixLocation = when (type) {
                MASK, MASKED, MASKED_INVERTED, MASKED_PREMULTIPLIED_ALPHA, MASKED_INVERTED_PREMULTIPLIED_ALPHA -> {
                    glGetUniformLocation(
                        type.shaderProgram.id,
                        "u_clipMatrix"
                    )
                }

                else -> 0
            }
            samplerTexture0Location = glGetUniformLocation(
                type.shaderProgram.id,
                "s_texture0"
            )
            samplerTexture1Location = when (type) {
                MASKED, MASKED_INVERTED, MASKED_PREMULTIPLIED_ALPHA, MASKED_INVERTED_PREMULTIPLIED_ALPHA -> {
                    glGetUniformLocation(
                        type.shaderProgram.id,
                        "s_texture1"
                    )
                }

                else -> 0
            }
            uniformBaseColorLocation = glGetUniformLocation(
                type.shaderProgram.id,
                "u_baseColor"
            )
            uniformMultiplyColorLocation = glGetUniformLocation(
                type.shaderProgram.id,
                "u_multiplyColor"
            )
            uniformScreenColorLocation = glGetUniformLocation(
                type.shaderProgram.id,
                "u_screenColor"
            )
            uniformChannelFlagLocation = when (type) {
                MASK, MASKED, MASKED_INVERTED, MASKED_PREMULTIPLIED_ALPHA, MASKED_INVERTED_PREMULTIPLIED_ALPHA -> {
                    glGetUniformLocation(
                        type.shaderProgram.id,
                        "u_channelFlag"
                    )
                }

                else -> 0
            }
        }
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
                );
                glDeleteShader(shaderId);
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
