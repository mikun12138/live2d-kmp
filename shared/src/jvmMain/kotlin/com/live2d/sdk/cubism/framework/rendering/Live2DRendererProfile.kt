package com.live2d.sdk.cubism.framework.rendering

import org.lwjgl.opengl.GL46.*

object Live2DRendererProfile {
    /**
     * FBO just before model drawing
     */
    val lastFBO: IntArray = IntArray(1)

    /**
     * Viewport just before drawing the model
     */
    val lastViewport: IntArray = IntArray(4)

    /**
     * Vertex buffer just before drawing the model
     */
    private val lastArrayBufferBinding = IntArray(1)

    /**
     * Element buffer just before drawing the model
     */
    private val lastElementArrayBufferBinding = IntArray(1)

    /**
     * Shader program buffer just before drawing the model
     */
    private val lastProgram = IntArray(1)

    /**
     * The active texture just before drawing the model
     */
    private val lastActiveTexture = IntArray(1)

    /**
     * Texture unit0 just before model drawing
     */
    private val lastTexture0Binding2D = IntArray(1)

    /**
     * Texture unit1 just before model drawing
     */
    private val lastTexture1Binding2D = IntArray(1)

    /**
     * GL_VERTEX_ATTRIB_ARRAY_ENABLED parameter just before model drawing
     */
    private val lastVertexAttribArrayEnabled = Array<IntArray>(4) { IntArray(1) }

    /**
     * GL_SCISSOR_TEST parameter just before drawing the model
     */
    private var lastScissorTest = false

    /**
     * GL_BLEND parameter just before model drawing
     */
    private var lastBlend = false

    /**
     * GL_STENCIL_TEST parameter just before drawing the model
     */
    private var lastStencilTest = false

    /**
     * GL_DEPTH_TEST parameter just before drawing the model
     */
    private var lastDepthTest = false

    /**
     * GL_CULL_FACE parameter just before drawing the model
     */
    private var lastCullFace = false

    /**
     * GL_FRONT_FACE parameter just before model drawing
     */
    private val lastFrontFace = IntArray(1)

    /**
     * GL_COLOR_WRITEMASK parameter just before model drawing
     */
    private val lastColorMask = BooleanArray(4)

    /**
     * GL_BLEND_SRC_RGB parameter just before model drawing
     */
    private val lastBlendingSrcRGB = IntArray(1)

    /**
     * GL_BLEND_DST_RGB parameter just before model drawing
     */
    private val lastBlendingDstRGB = IntArray(1)

    /**
     * GL_BLEND_SRC_ALPHA parameter just before model drawing
     */
    private val lastBlendingSrcAlpha = IntArray(1)

    /**
     * GL_BLEND_DST_ALPHA parameter just before model drawing
     */
    private val lastBlendingDstAlpha = IntArray(1)

    fun save() {
        //-- push state --
        glGetIntegerv(
            GL_ARRAY_BUFFER_BINDING,
            lastArrayBufferBinding,
        );
        glGetIntegerv(
            GL_ELEMENT_ARRAY_BUFFER_BINDING,
            lastElementArrayBufferBinding,
        );
        glGetIntegerv(
            GL_CURRENT_PROGRAM,
            lastProgram,
        );

        glGetIntegerv(
            GL_ACTIVE_TEXTURE,
            lastActiveTexture,
        );

        // Activate Texture Unit1 (It is the target to be set thereafter)
        glActiveTexture(GL_TEXTURE1);
        glGetIntegerv(
            GL_TEXTURE_BINDING_2D,
            lastTexture1Binding2D,
        );

        // Activate Texture Unit0 (It is the target to be set thereafter)
        glActiveTexture(GL_TEXTURE0);
        glGetIntegerv(
            GL_TEXTURE_BINDING_2D,
            lastTexture0Binding2D,
        );

        glGetVertexAttribiv(
            0,
            GL_VERTEX_ATTRIB_ARRAY_ENABLED,
            lastVertexAttribArrayEnabled[0],
        );
        glGetVertexAttribiv(
            1,
            GL_VERTEX_ATTRIB_ARRAY_ENABLED,
            lastVertexAttribArrayEnabled[1],
        );
        glGetVertexAttribiv(
            2,
            GL_VERTEX_ATTRIB_ARRAY_ENABLED,
            lastVertexAttribArrayEnabled[2],
        );
        glGetVertexAttribiv(
            3,
            GL_VERTEX_ATTRIB_ARRAY_ENABLED,
            lastVertexAttribArrayEnabled[3],
        );

        lastScissorTest = glIsEnabled(GL_SCISSOR_TEST);
        lastStencilTest = glIsEnabled(GL_STENCIL_TEST);
        lastDepthTest = glIsEnabled(GL_DEPTH_TEST);
        lastCullFace = glIsEnabled(GL_CULL_FACE);
        lastBlend = glIsEnabled(GL_BLEND);

        glGetIntegerv(
            GL_FRONT_FACE,
            lastFrontFace,
        );

//        glGetBooleanv(
//            GL_COLOR_WRITEMASK,
//            lastColorMask,
//        );

        // backup blending
        glGetIntegerv(
            GL_BLEND_SRC_RGB,
            lastBlendingSrcRGB,
        );
        glGetIntegerv(
            GL_BLEND_DST_RGB,
            lastBlendingDstRGB,
        );
        glGetIntegerv(
            GL_BLEND_SRC_ALPHA,
            lastBlendingSrcAlpha,
        );
        glGetIntegerv(
            GL_BLEND_DST_ALPHA,
            lastBlendingDstAlpha,
        );

        // Save the FBO and viewport just before drawing the model.
        glGetIntegerv(
            GL_FRAMEBUFFER_BINDING,
            lastFBO,
        );
        glGetIntegerv(
            GL_VIEWPORT,
            lastViewport,
        );
    }

    fun restore() {
        glUseProgram(lastProgram[0]);

        setGlEnableVertexAttribArray(
            0,
            lastVertexAttribArrayEnabled[0][0]
        );
        setGlEnableVertexAttribArray(
            1,
            lastVertexAttribArrayEnabled[1][0]
        );
        setGlEnableVertexAttribArray(
            2,
            lastVertexAttribArrayEnabled[2][0]
        );
        setGlEnableVertexAttribArray(
            3,
            lastVertexAttribArrayEnabled[3][0]
        );

        setGlEnable(
            GL_SCISSOR_TEST,
            lastScissorTest
        );
        setGlEnable(
            GL_STENCIL_TEST,
            lastStencilTest
        );
        setGlEnable(
            GL_DEPTH_TEST,
            lastDepthTest
        );
        setGlEnable(
            GL_CULL_FACE,
            lastCullFace
        );
        setGlEnable(
            GL_BLEND,
            lastBlend
        );

        glFrontFace(lastFrontFace[0]);

//        glColorMask(
//            lastColorMask[0],
//            lastColorMask[1],
//            lastColorMask[2],
//            lastColorMask[3]
//        );

        // If the buffer was bound before, it needs to be destroyed.
        glBindBuffer(
            GL_ARRAY_BUFFER,
            lastArrayBufferBinding[0]
        );
        glBindBuffer(
            GL_ELEMENT_ARRAY_BUFFER,
            lastElementArrayBufferBinding[0]
        );

        // Restore Texture Unit1.
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(
            GL_TEXTURE_2D,
            lastTexture1Binding2D[0]
        );

        // Restore Texture Unit0.
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(
            GL_TEXTURE_2D,
            lastTexture0Binding2D[0]
        );

        glActiveTexture(lastActiveTexture[0]);

        // restore blending
        glBlendFuncSeparate(
            lastBlendingSrcRGB[0],
            lastBlendingDstRGB[0],
            lastBlendingSrcAlpha[0],
            lastBlendingDstAlpha[0]
        );
    }

    /**
     * Set enable/disable of OpenGL ES 2.0 features.
     *
     * @param index index of function to enable/disable
     * @param enabled If true, enable it.
     */
    private fun setGlEnable(
        index: Int,
        enabled: Boolean
    ) {
        if (enabled) {
            glEnable(index)
        } else {
            glDisable(index)
        }
    }

    /**
     * Set enable/disable for Vertex Attribute Array feature in OpenGL ES 2.0.
     *
     * @param index index of function to enable/disable
     * @param isEnabled If true, enable it.
     */
    private fun setGlEnableVertexAttribArray(
        index: Int,
        isEnabled: Int
    ) {
        // It true
        if (isEnabled != 0) {
            glEnableVertexAttribArray(index)
        } else {
            glDisableVertexAttribArray(index)
        }
    }

}