package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Model
import org.lwjgl.opengl.GL46.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.max

actual fun <T : Live2DRenderer> Live2DRenderer.create(creation: () -> T): T {

}

class ModelMesh {
    val vaoArray: IntArray
    private val vertexArrayCaches: Array<FloatBuffer>

    private val uvArrayCaches: Array<FloatBuffer>

    private val indexArrayCaches: Array<ShortBuffer>

    constructor(model: Model) {
        val drawableCount: Int = model.drawableCount

        vaoArray = IntArray(drawableCount)
        vertexArrayCaches = Array<FloatBuffer>(drawableCount) { drawableIndex ->
            val positions = model.model.drawableViews[drawableIndex].vertexPositions!!

            ByteBuffer.allocateDirect(
                positions.size * Float.SIZE_BYTES
            )
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        }
        uvArrayCaches = Array<FloatBuffer>(drawableCount) { drawableIndex ->
            val uvs = model.model.drawableViews[drawableIndex].vertexUvs!!
            ByteBuffer.allocateDirect(
                uvs.size * Float.SIZE_BYTES
            )
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        }
        indexArrayCaches = Array<ShortBuffer>(drawableCount) { drawableIndex ->
            val indices = model.model.drawableViews[drawableIndex].indices!!
            ByteBuffer.allocateDirect(
                indices.size * Short.SIZE_BYTES
            )
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
        }

        var vbo: Int
        repeat(drawableCount) { drawableIndex ->
            vaoArray[drawableIndex] = glGenVertexArrays()
            glBindVertexArray(vaoArray[drawableIndex])

            run {
                vbo = glGenBuffers()

                val positions = model.model.drawableViews[drawableIndex].vertexPositions!!
                glBindBuffer(GL_ARRAY_BUFFER, vbo)
                glBufferData(
                    GL_ARRAY_BUFFER,
                    (positions.size * Float.SIZE_BYTES).toLong(),
                    GL_DYNAMIC_DRAW
                )
                glBindBuffer(GL_ARRAY_BUFFER, 0)
            }

            run {
                vbo = glGenBuffers()

                val uvs: FloatArray = model.model.drawableViews[drawableIndex].vertexUvs!!
                glBindBuffer(GL_ARRAY_BUFFER, vbo)
                glBufferData(
                    GL_ARRAY_BUFFER,
                    (uvs.size * Float.SIZE_BYTES).toLong(),
                    GL_DYNAMIC_DRAW
                )
                glBindBuffer(GL_ARRAY_BUFFER, 0)
            }

            run {
                vbo = glGenBuffers()

                val indices: ShortArray = model.model.drawableViews[drawableIndex].indices!!
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo)
                glBufferData(
                    GL_ARRAY_BUFFER,
                    (indices.size * Short.SIZE_BYTES).toLong(),
                    GL_STATIC_DRAW
                )
                glBindBuffer(GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER)
            }

            glBindVertexArray(0)
        }
    }

    fun updatePositions(drawableIndex: Int, drawableVertices: FloatArray): FloatBuffer {
        return vertexArrayCaches[drawableIndex]
            .clear()
            .put(drawableVertices)
            .position(0)
    }

    fun updateUvs(drawableIndex: Int, drawableVertexUvs: FloatArray): FloatBuffer {
        return uvArrayCaches[drawableIndex]
            .clear()
            .put(drawableVertexUvs)
            .position(0)
    }

    fun updateIndices(drawableIndex: Int, drawableIndices: ShortArray): ShortBuffer {
        return indexArrayCaches[drawableIndex]
            .clear()
            .put(drawableIndices)
            .position(0)
    }

}

class Live2DRendererImpl : Live2DRenderer() {
    lateinit var modelMesh: ModelMesh

    override fun initialize(
        model: Model,
        maskBufferCount: Int,
    ) {

        // 頂点情報をキャッシュする。
        modelMesh = ModelMesh(model)

        if (model.isUsingMasking()) {

            // Initialize clipping mask and buffer preprocessing method
            clippingManager = CubismClippingManager(
                model,
                max(maskBufferCount, 1)
            )
        }

        val sortedDrawableIndexList = IntArray(model.drawableCount)
    }

    override fun saveProfile() {
        Live2DRendererProfile.save()
    }

    lateinit var clippingManager: CubismClippingManager

    override fun doDrawModel(model: Model) {

        // In the case of clipping mask and buffer preprocessing method
        if (model.isUsingMasking()) {
            preDraw()
//
//            // If offscreen frame buffer size is different from clipping mask buffer size, recreate it.
//            for (i in 0..<clippingManager.framebufferCount) {
//                val offscreenSurface = offscreenSurfaces[i]
//
//                if (!offscreenSurface.isSameSize(clippingManager.getClippingMaskBufferSize())) {
//                    offscreenSurface.createOffscreenSurface(
//                        clippingManager.getClippingMaskBufferSize(),
//                        null
//                    )
//                }
//            }

//            if (isUsingHighPrecisionMask()) {
            if (false) {
//                clippingManager.setupMatrixForHighPrecision(getModel(), false)
            } else {
                clippingManager.setupClippingContext(
                    model,
                    this,
                    Live2DRendererProfile.lastViewport
                )
            }
        }


        // preDraw() method is called twice.
        preDraw()
    }

    /**
     * Additional proccesing at the start of drawing
     * This method implements the necessary processing for the clipping mask before drawing the model
     */
    override fun preDraw() {
        glDisable(GL_SCISSOR_TEST)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)
        glColorMask(true, true, true, true)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        // If the buffer has been bound before, it needs to be destroyed
        glBindBuffer(GL_ARRAY_BUFFER, 0)

//        // Anisotropic filtering. If it is not supported, do not set it
//        if (getAnisotropy() >= 1.0f) {
//            for (entry in textures.entries) {
//                glBindTexture(GL_TEXTURE_2D, entry.value)
//                glTexParameterf(
//                    GL_TEXTURE_2D,
//                    GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT,
//                    getAnisotropy()
//                )
//            }
//        }
    }

    override fun drawMesh(
        model: Model,
        index: Int,
    ) {
//        if (!CSM_DEBUG) {
//            // If the texture referenced by the model is not bound, skip drawing.
//            if (textures.get(model.getDrawableTextureIndex(index)) == null) {
//                return
//            }
//        }

        // Enabling/disabling culling
        if (model.getDrawableCulling(index)) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }

        // In Cubism3 OpenGL, CCW becomes surface for both masks and art meshes.
        glFrontFace(GL_CCW)

        // マスク生成時
        if (isGeneratingMask()) {
            CubismShaderAndroid.getInstance().setupShaderProgramForMask(this, model, index)
        } else {
            CubismShaderAndroid.getInstance().setupShaderProgramForDraw(this, model, index)
        }


        // Draw the prygon mesh
        val indexCount: Int = model.model.drawableViews[index].indices!!.size
//        val indexArrayBuffer: ShortBuffer? = drawableInfoCachesHolder.setUpIndexArray(
//            index,
//            model.getDrawableVertexIndices(index)
//        )
        glBindVertexArray(modelMesh.vaoArray[index])

        glDrawElements(
            GL_TRIANGLES,
            indexCount,
            GL_UNSIGNED_SHORT,
            0
        )
        glBindVertexArray(0)

        // post-processing
        glUseProgram(0)
        setClippingContextBufferForDraw(null)
        setClippingContextBufferForMask(null)
    }


    override fun restoreProfile() {
        Live2DRendererProfile.restore()
    }

    override var isPremultipliedAlpha: Boolean

}