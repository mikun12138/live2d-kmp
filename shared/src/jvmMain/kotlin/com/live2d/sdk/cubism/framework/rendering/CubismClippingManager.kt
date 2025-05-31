package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Model
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_BUFFER_SIZE_X
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_BUFFER_SIZE_Y
import com.live2d.sdk.cubism.framework.type.csmRectF
import com.live2d.sdk.cubism.framework.type.expand
import org.lwjgl.opengl.GL46.*

class CubismClippingManager(
    model: Model,
    maskBufferCount: Int,
) : ACubismClippingManager(
    model,
    maskBufferCount
) {
    fun setupClippingContext(
        model: Model, renderer: Live2DRenderer, lastViewport: IntArray,
    ) {
        // Prepare all clipping.
        // Set only once when using the same clip (or a group of clips if there ares multiple clips).
        val usingClipCount =
            clippingContextForMask_2_ClippedDrawableIndexList.keys.count { clipContext ->
                calcClippedDrawTotalBounds(model, clipContext)
                clipContext.isUsing
            }

        // Process of creating mask.
        // Set up a viewport with the same size as the generated MaskBuffer.
        glViewport(
            0, 0,
            CLIPPING_MASK_BUFFER_SIZE_X.toInt(), CLIPPING_MASK_BUFFER_SIZE_Y.toInt()
        )

        // Determine the layout of each mask.
        setupLayoutBounds(usingClipCount)


        offscreenSurfaces_2_clippingContextForMaskList.forEach { (offscreenSurface, clipContextList) ->

            offscreenSurface.draw {
                renderer.preDraw()
                // マスクをクリアする。
                // (仮仕様) 1が無効（描かれない）領域、0が有効（描かれる）領域。（シェーダーCd*Csで0に近い値をかけてマスクを作る。1をかけると何も起こらない）
                glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
                glClear(GL_COLOR_BUFFER_BIT)

                clipContextList.forEach { clipContext ->
                    run {

                        // The enclosing rectangle in logical coordinates of all drawing objects that use this mask.
                        val allClippedDrawRect: csmRectF = clipContext.allClippedDrawRect
                        // Fit the mask in here.
                        val layoutBoundsOnTex01: csmRectF = clipContext.layoutBounds

                        val margin = 0.05f

                        // Use a rectangle on the model coordinates with margins as appropriate.
                        val allClippedDrawRectActually = allClippedDrawRect.copy().expand(
                            allClippedDrawRect.width * margin,
                            allClippedDrawRect.height * margin
                        )

                        // ######## It is best to keep the size to a minimum, rather than using the entire allocated space.
                        // Find the formula for the shader. If rotation is not taken into account, the formula is as follows.
                        // movePeriod' = movePeriod * scaleX + offX     [[ movePeriod' = (movePeriod - tmpBoundsOnModel.movePeriod)*scale + layoutBoundsOnTex01.movePeriod ]]
                        val scaleX: Float =
                            layoutBoundsOnTex01.width / allClippedDrawRectActually.width
                        val scaleY: Float =
                            layoutBoundsOnTex01.height / allClippedDrawRectActually.height

                        // Calculate the matrix to be used for mask generation.
                        clipContext.matrixForMask.setMatrix(
                            createMatrixForMask(
                                layoutBoundsOnTex01,
                                scaleX, scaleY,
                                allClippedDrawRectActually
                            ).tr
                        )
                        clipContext.matrixForDraw.setMatrix(
                            createMatrixForDraw(
                                layoutBoundsOnTex01,
                                scaleX, scaleY,
                                allClippedDrawRectActually
                            ).tr
                        )
                    }
                    clippingContextForMask_2_ClippedDrawableIndexList.get(clipContext)!!.forEach { clipDrawIndex ->
                        // If vertex information is not updated and reliable, pass drawing.
                        if (!model.getDrawableDynamicFlagVertexPositionsDidChange(clipDrawIndex)) {
                            return@forEach
                        }

                        // Apply this special transformation to draw it.
                        // Switching channel is also needed.(A,R,G,B)
                        renderer.clippingContextBufferForMask = clipContext


                        renderer.drawMesh(
                            model,
                            clipDrawIndex
                        )

                    }

                }
            }
        }


        // --- Post Processing ---
        // Return the drawing target
        renderer.clippingContextBufferForMask = null
        glViewport(lastViewport[0], lastViewport[1], lastViewport[2], lastViewport[3])


//        // ---------- Mask Drawing Process -----------
//        // Actually generate the masks.
//        // Determine how to layout and draw all the masks, and store them in ClipContext and ClippedDrawContext.
//        clippingContextForMask_2_ClippedDrawableIndexList.forEach { clipContext, clipDrawIndexList ->
//            run {
//                // The enclosing rectangle in logical coordinates of all drawing objects that use this mask.
//                val allClippedDrawRect: csmRectF = clipContext.allClippedDrawRect
//                // Fit the mask in here.
//                val layoutBoundsOnTex01: csmRectF = clipContext.layoutBounds
//
//                val margin = 0.05f
//
//                // Use a rectangle on the model coordinates with margins as appropriate.
//                val allClippedDrawRectActually = allClippedDrawRect.copy().expand(
//                    allClippedDrawRect.width * margin,
//                    allClippedDrawRect.height * margin
//                )
//
//                // ######## It is best to keep the size to a minimum, rather than using the entire allocated space.
//                // Find the formula for the shader. If rotation is not taken into account, the formula is as follows.
//                // movePeriod' = movePeriod * scaleX + offX     [[ movePeriod' = (movePeriod - tmpBoundsOnModel.movePeriod)*scale + layoutBoundsOnTex01.movePeriod ]]
//                val scaleX: Float = layoutBoundsOnTex01.width / allClippedDrawRectActually.width
//                val scaleY: Float = layoutBoundsOnTex01.height / allClippedDrawRectActually.height
//
//                // Calculate the matrix to be used for mask generation.
//                clipContext.matrixForMask.setMatrix(
//                    createMatrixForMask(
//                        layoutBoundsOnTex01,
//                        scaleX, scaleY,
//                        allClippedDrawRectActually
//                    ).tr
//                )
//                clipContext.matrixForDraw.setMatrix(
//                    createMatrixForDraw(
//                        layoutBoundsOnTex01,
//                        scaleX, scaleY,
//                        allClippedDrawRectActually
//                    ).tr
//                )
//            }
//
//            // clipContextに設定したオフスクリーンサーフェスをインデックスで取得
//            val clipContextOffscreenSurface = renderer.getMaskBuffer(clipContext.bufferIndex)
//
//            // 現在のオフスクリーンサーフェスがclipContextのものと異なる場合
//            if (currentMaskBuffer !== clipContextOffscreenSurface) {
//                currentMaskBuffer.endDraw()
//                currentMaskBuffer = clipContextOffscreenSurface
//
//                // マスク用RenderTextureをactiveにセット。
//                currentMaskBuffer.beginDraw(lastFBO)
//
//                // バッファをクリアする。
//                renderer.preDraw()
//            }
//
//            // 実際の描画を行う。
//            clipDrawIndexList.forEach { clipDrawIndex ->
//                // If vertex information is not updated and reliable, pass drawing.
//                if (!model.getDrawableDynamicFlagVertexPositionsDidChange(clipDrawIndex)) {
//                    continue
//                }
//
//                // TODO:: move to drawing fun
//                renderer.isCulling(model.getDrawableCulling(clipDrawIndex))
//
//                // マスクがクリアされていないなら処理する。
//                if (!clearedMaskBufferFlags[clipContext.bufferIndex]) {
//                    // マスクをクリアする。
//                    // (仮仕様) 1が無効（描かれない）領域、0が有効（描かれる）領域。（シェーダーCd*Csで0に近い値をかけてマスクを作る。1をかけると何も起こらない）
//                    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
//                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//                    clearedMaskBufferFlags[clipContext.bufferIndex] = true
//                }
//
//                // Apply this special transformation to draw it.
//                // Switching channel is also needed.(A,R,G,B)
//                renderer.setClippingContextBufferForMask(clipContext)
//
//                renderer.drawMeshAndroid(
//                    model,
//                    clipDrawIndex
//                )
//            }
//        }
    }


}