package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Model
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_BUFFER_SIZE_X
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_BUFFER_SIZE_Y
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


    override fun init(
        model: Model,
        maskBufferCount: Int,
    ) {

        // 頂点情報をキャッシュする。
        modelMesh = ModelMesh(model)

//        if (model.isUsingMasking()) {
        if (true) {

            // Initialize clipping mask and buffer preprocessing method
            clippingManager = ACubismClippingManager.create(
                model,
                max(maskBufferCount, 1)
            )
        }
    }

    override fun saveProfile() {
        Live2DRendererProfile.save()
    }


    override fun doDrawModel(model: Model) {

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

            val clipContext = clippingManager.clippingContextListForDraw[drawableIndex]

            // マスクを描く必要がある
            if (clipContext != null && false) {
                // 描くことになっていた
                if (clipContext.isUsing) {
                    // 生成したOffscreenSurfaceと同じサイズでビューポートを設定
                    glViewport(
                        0,
                        0,
                        CLIPPING_MASK_BUFFER_SIZE_X.toInt(),
                        CLIPPING_MASK_BUFFER_SIZE_Y.toInt(),
                    )

                    // マスク描画処理
                    // マスク用RenderTextureをactiveにセット
                    clippingManager.offscreenSurfaces_2_clippingContextForMaskList[clipContext.bufferIndex].first.beginDraw()

                    // マスクをクリアする。
                    // 1が無効（描かれない領域）、0が有効（描かれる）領域。（シェーダーでCd*Csで0に近い値をかけてマスクを作る。1をかけると何も起こらない。）
                    glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
                    glClear(GL_COLOR_BUFFER_BIT)
                }

                for (clipDrawIndex in clipContext.clippingIdList) {

                    // 頂点情報が更新されておらず、信頼性がない場合は描画をパスする
                    if (!model.getDrawableDynamicFlagVertexPositionsDidChange(clipDrawIndex)) {
                        continue
                    }

                    // 今回専用の変換を適用して描く
                    // チャンネルも切り替える必要がある（A,R,G,B）
                    clippingContextBufferForMask = clipContext

                    draw(model, clipDrawIndex)
                }
                // --- 後処理 ---
                for (j in 0..<clippingManager.framebufferCount) {
                    clippingManager.offscreenSurfaces_2_clippingContextForMaskList[j].first.endDraw()
                    clippingContextBufferForMask = null
                    glViewport(
                        Live2DRendererProfile.lastViewport[0],
                        Live2DRendererProfile.lastViewport[1],
                        Live2DRendererProfile.lastViewport[2],
                        Live2DRendererProfile.lastViewport[3],
                    )
                }
            }


            // クリッピングマスクをセットする
            clippingContextBufferForDraw = clipContext

            draw(model, drawableIndex)
        }
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

    override fun postDraw() {

    }

    override fun doDraw(
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
