package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Live2DModel
import org.lwjgl.opengl.GL46.*


actual fun ALive2DRenderer.Companion.create(): ALive2DRenderer {
    return Live2DRendererImpl()
}

class Live2DRendererImpl : ALive2DRenderer() {
    private lateinit var clippingManager: ACubismClippingManager

    var drawableInfoCachesHolder: CubismDrawableInfoCachesHolder? = null

    override fun doInit(
        model: Live2DModel,
        maskBufferCount: Int,
    ) {
        drawableInfoCachesHolder = CubismDrawableInfoCachesHolder(
            model,
        )

//        if (model.isUsingMasking) {
        if (true) {
            var maskBufferCount1: Int = maskBufferCount
            if (maskBufferCount1 < 1) {
                maskBufferCount1 = 1
            }

            // Initialize clipping mask and buffer preprocessing method
            clippingManager = ACubismClippingManager.create(
                model,
                maskBufferCount1
            )

            offscreenSurfaces = Array(size = maskBufferCount1) {
                ACubismOffscreenSurface.create().apply {
                    createOffscreenSurface(
                        clippingManager.clippingMaskBufferSize
                    )
                }
            }
        }

    }

    override fun saveProfile() {
        Live2DRendererProfile.save()
    }

    override fun doDrawModel() {
//        preDraw()
        if (clippingManager != null) {

            run {

//            if (isUsingHighPrecisionMask) {
                if (false) {
//                    clippingManager.setupMatrixForHighPrecision(
//                        model,
//                        false
//                    )
                } else {
                    clippingManager.setupClippingContext(
                        model,
                        this,
                    )
                }
            }
        }


//        preDraw()

        val sortedDrawableIndexArray = IntArray(size = model.drawableCount)

        // sort index by drawing order
        repeat(model.drawableCount) {
            sortedDrawableIndexArray.set(
                index = model.model.drawableViews[it].renderOrder,
                value = it
            )
        }

        repeat(model.drawableCount) {
            val drawableIndex = sortedDrawableIndexArray[it]
            if (!model.getDrawableDynamicFlagIsVisible(drawableIndex)) {
                return@repeat
            }
            // 用于draw的clippingContext
            val clipContext =
                if (clippingManager != null) clippingManager.drawIndex_2_clippingContext.find { it.first == drawableIndex }?.second else null
            val clipContext1 =
                if (clippingManager != null) clippingManager.clippingContextListForDraw[drawableIndex] else null

/*

            if (clipContext != null && isUsingHighPrecisionMask) {

                if (clipContext.isUsing) {
                    glViewport(
                        0,
                        0,
                        clippingManager.getClippingMaskBufferSize().x.toInt(),
                        clippingManager.getClippingMaskBufferSize().y.toInt()
                    )

                    preDraw()

                    // 选定 framebuffer
                    offscreenSurfaces[clipContext.bufferIndex].beginDraw(
                        Live2DRendererProfileWindows.lastFBO
                    )

                    glClearColor(
                        1.0f,
                        1.0f,
                        1.0f,
                        1.0f
                    )
                    glClear(GL_COLOR_BUFFER_BIT)
                }

                repeat(clipContext.clippingIdCount) {
                    clipContext.clippingIdList[it].let { clipDrawIndex ->
                        if (!model.getDrawableDynamicFlagVertexPositionsDidChange(clipDrawIndex)) {
                            return@repeat
                        }

                        // 应用该drawable的culling
                        isCulling(model.getDrawableCulling(clipDrawIndex))

                        // 选定context
                        clippingContextBufferForMask = clipContext

                        drawMeshAndroid(
                            model,
                            clipDrawIndex
                        )

                    }

                }

                // --- 後処理 ---
                for (j in 0 until clippingManager.getRenderTextureCount()) {
                    offscreenSurfaces[j].endDraw()
                    clippingContextBufferForMask = null
                    glViewport(
                        Live2DRendererProfileWindows.lastViewport[0],
                        Live2DRendererProfileWindows.lastViewport[1],
                        Live2DRendererProfileWindows.lastViewport[2],
                        Live2DRendererProfileWindows.lastViewport[3]
                    )
                }

            }

 */

            clippingContextBufferForDraw = clipContext1

            isCulling = !model.getDrawableIsDoubleSided(drawableIndex)

            drawMeshAndroid(
                model,
                drawableIndex
            );

        }
        postDraw()
    }

    /**
     * 实现绘制
     */
    override fun drawMeshAndroid(
        model: Live2DModel,
        clipDrawIndex: Int,
    ) {
        preDraw()
        // If the texture referenced by the model is not bound, skip drawing.
        if (model.textures[model.getDrawableTextureIndex(clipDrawIndex)] == null) {
            return;
        }

        if (isCulling) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }

        // In Cubism3 OpenGL, CCW becomes surface for both masks and art meshes.
        glFrontFace(GL_CCW)

        // マスク生成時
        if (clippingContextBufferForMask != null) {
            Live2DShader.setupShaderProgramForMask(
                this,
                model,
                clipDrawIndex
            )
        } else {
            Live2DShader.setupShaderProgramForDraw(
                this,
                model,
                clipDrawIndex
            )
        }

        // Draw the prygon mesh
//        val indexCount = model.getDrawableVertexIndexCount(clipDrawIndex)
        val indexBuffer = drawableInfoCachesHolder?.setUpIndexArray(
            clipDrawIndex,
            model.getDrawableIndices(clipDrawIndex)!!
        )

        glDrawElements(
            GL_TRIANGLES,
            indexBuffer!!
        )
        // post-processing
        glUseProgram(0)
        clippingContextBufferForDraw = null;
        clippingContextBufferForMask = null;

    }

    override fun restoreProfile() {
        Live2DRendererProfile.restore()
    }

    override fun preDraw() {
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

        glBindBuffer(
            GL_ELEMENT_ARRAY_BUFFER,
            0
        )
        // If the buffer has been bound before, it needs to be destroyed
        glBindBuffer(
            GL_ARRAY_BUFFER,
            0
        )

//        // Anisotropic filtering. If it is not supported, do not set it
//        if (getAnisotropy() >= 1.0f) {
//            for (entry in textures.entries) {
//                glBindTexture(
//                    GL_TEXTURE_2D,
//                    entry.value!!
//                )
//                glTexParameterf(
//                    GL_TEXTURE_2D,
//                    GL_TEXTURE_MAX_ANISOTROPY_EXT,
//                    getAnisotropy()
//                )
//            }
//        }
    }

    fun postDraw() {

    }

//    fun getBoundTextureId(textureId: Int): Int {
//        return model.textures.get(textureId)?.id ?: -1
//    }
}