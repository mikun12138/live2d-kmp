package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Model
import org.lwjgl.opengl.GL46.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.max


actual fun Live2DRenderer.Companion.create(): Live2DRenderer {
    return Live2DRendererImpl()
}

class ModelMesh {
//    val vaoArray: IntArray
    private val vertexArrayCaches: Array<FloatBuffer>

    private val uvArrayCaches: Array<FloatBuffer>

    private val indexArrayCaches: Array<ShortBuffer>

    constructor(model: Model) {
        val drawableCount: Int = model.drawableCount

//        vaoArray = IntArray(drawableCount)
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
//            vaoArray[drawableIndex] = glGenVertexArrays()
//            glBindVertexArray(vaoArray[drawableIndex])

            run {
                vbo = glGenBuffers()

                val positions = model.model.drawableViews[drawableIndex].vertexPositions!!
                glBindBuffer(GL_ARRAY_BUFFER, vbo)
                glBufferData(
                    GL_ARRAY_BUFFER,
                    (positions.size * Float.SIZE_BYTES).toLong(),
                    GL_DYNAMIC_DRAW
                )
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
            }

//            glBindVertexArray(0)
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

    lateinit var clippingManager: CubismClippingManager

    override fun initialize(
        model: Model,
        maskBufferCount: Int,
    ) {

        // 頂点情報をキャッシュする。
        modelMesh = ModelMesh(model)

//        if (model.isUsingMasking()) {
        if (true) {

            // Initialize clipping mask and buffer preprocessing method
            clippingManager = CubismClippingManager(
                model,
                max(maskBufferCount, 1)
            )
        }
    }

    override fun saveProfile() {
        Live2DRendererProfile.save()
    }


    override fun doDrawModel(model: Model) {

        // In the case of clipping mask and buffer preprocessing method
//        if (model.isUsingMasking()) {
        if (true) {
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
        run {

            val drawableCount: Int = model.drawableCount
            val renderOrder: IntArray = model.model.drawables.renderOrders


            // Sort the index by drawing order
            val sortedDrawableIndexList = IntArray(drawableCount)
            for (i in 0..<drawableCount) {
                val order = renderOrder[i]
                sortedDrawableIndexList[order] = i
            }

            for (drawableIndex in sortedDrawableIndexList) {

                // If Drawable is not in the display state, the process is passed.
                if (!model.getDrawableDynamicFlagIsVisible(drawableIndex)) {
                    continue
                }

                /*            // マスクを描く必要がある
                            if (clipContext != null && isUsingHighPrecisionMask()) {
                                // 描くことになっていた
                                if (clipContext.isUsing) {
                                    // 生成したOffscreenSurfaceと同じサイズでビューポートを設定
                                    GLES20.glViewport(
                                        0,
                                        0,
                                        clippingManager.getClippingMaskBufferSize().x as Int,
                                        clippingManager.getClippingMaskBufferSize().y as Int
                                    )

                                    // バッファをクリアする
                                    preDraw()

                                    // マスク描画処理
                                    // マスク用RenderTextureをactiveにセット
                                    getMaskBuffer(clipContext.bufferIndex).beginDraw(rendererProfile.lastFBO)

                                    // マスクをクリアする。
                                    // 1が無効（描かれない領域）、0が有効（描かれる）領域。（シェーダーでCd*Csで0に近い値をかけてマスクを作る。1をかけると何も起こらない。）
                                    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
                                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                                }

                                val clipDrawCount: Int = clipContext.clippingIdCount
                                for (index in 0..<clipDrawCount) {
                                    val clipDrawIndex: Int = clipContext.clippingIdList[index]!!

                                    // 頂点情報が更新されておらず、信頼性がない場合は描画をパスする
                                    if (!getModel().getDrawableDynamicFlagVertexPositionsDidChange(clipDrawIndex)) {
                                        continue
                                    }

                                    isCulling(getModel().getDrawableCulling(clipDrawIndex))

                                    // 今回専用の変換を適用して描く
                                    // チャンネルも切り替える必要がある（A,R,G,B）
                                    setClippingContextBufferForMask(clipContext)

                                    drawMeshAndroid(model, clipDrawIndex)
                                }
                                // --- 後処理 ---
                                for (j in 0..<clippingManager.getRenderTextureCount()) {
                                    offscreenSurfaces[j].endDraw()
                                    setClippingContextBufferForMask(null)
                                    GLES20.glViewport(
                                        rendererProfile.lastViewport[0],
                                        rendererProfile.lastViewport[1],
                                        rendererProfile.lastViewport[2],
                                        rendererProfile.lastViewport[3]
                                    )
                                }
                            }*/

                val clipContext = clippingManager.clippingContextListForDraw[drawableIndex]

                // クリッピングマスクをセットする
                clippingContextBufferForDraw = clipContext

                drawMesh(model, drawableIndex)
            }
        }
        postDraw()

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

    fun postDraw() {

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
        if (!model.getDrawableIsDoubleSided(index)) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }

        // In Cubism3 OpenGL, CCW becomes surface for both masks and art meshes.
        glFrontFace(GL_CCW)

        // マスク生成時
        if (clippingContextBufferForMask != null) {
            Live2DShader.setupShaderProgramForMask(this, model, index)
        } else {
            Live2DShader.setupShaderProgramForDraw(this, model, index)
        }


        // Draw the prygon mesh
        val indexCount: Int = model.model.drawableViews[index].indices!!.size
        val indexArrayBuffer: ShortBuffer = modelMesh.updateIndices(
            index,
            model.getDrawableIndices(index)!!
        )
//        glBindVertexArray(modelMesh.vaoArray[index])

//        glDrawElements(
//            GL_TRIANGLES,
//            indexCount,
//            GL_UNSIGNED_INT,
//            0
//        )

        glDrawElements(
            GL_TRIANGLES,
            indexArrayBuffer
        )
//        glBindVertexArray(0)

        // post-processing
        glUseProgram(0)
        clippingContextBufferForDraw = null
        clippingContextBufferForMask = null
    }


    override fun restoreProfile() {
        Live2DRendererProfile.restore()
    }

    override var isPremultipliedAlpha: Boolean = false

}
