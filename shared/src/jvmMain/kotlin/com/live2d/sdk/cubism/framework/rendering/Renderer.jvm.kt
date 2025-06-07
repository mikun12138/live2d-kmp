package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_OFFSET
import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_STEP
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.rendering.ILive2DClippingManager.Companion.CLIPPING_MASK_MAX_COUNT_ON_DEFAULT
import com.live2d.sdk.cubism.framework.rendering.ILive2DClippingManager.Companion.CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE
import com.live2d.sdk.cubism.framework.rendering.ILive2DClippingManager.Companion.COLOR_CHANNEL_COUNT
import com.live2d.sdk.cubism.framework.type.csmRectF
import com.live2d.sdk.cubism.framework.type.expand
import org.lwjgl.opengl.GL46.*
import kotlin.math.max
import kotlin.math.min

actual fun Renderer.Companion.create(
    model: Live2DModel,
    offScreenBufferCount: Int,
): Renderer {
    return RendererImpl(
        model,
        offScreenBufferCount
    )
}

class RendererImpl(
    model: Live2DModel,
    offScreenBufferCount: Int,
) : Renderer(
    model,
    offScreenBufferCount,
) {
    fun calcClippedDrawTotalBounds(clipContext: ClipContext): Boolean {
        // 被クリッピングマスク（マスクされる描画オブジェクト）の全体の矩形
        var clippedDrawTotalMinX = Float.Companion.MAX_VALUE
        var clippedDrawTotalMinY = Float.Companion.MAX_VALUE
        var clippedDrawTotalMaxX = -Float.Companion.MAX_VALUE
        var clippedDrawTotalMaxY = -Float.Companion.MAX_VALUE

        for (drawableIndex in clipContext_2_drawableIndexList[clipContext]!!) {
            val drawableContext = drawableContextArray[drawableIndex]

            var minX = Float.Companion.MAX_VALUE
            var minY = Float.Companion.MAX_VALUE
            var maxX = -Float.Companion.MAX_VALUE
            var maxY = -Float.Companion.MAX_VALUE

            val loop = drawableContext.vertex.count * VERTEX_STEP
            var pi = VERTEX_OFFSET
            while (pi < loop) {
                val x = drawableContext.vertex.positions[pi]
                val y = drawableContext.vertex.positions[pi + 1]
                minX = min(minX, x)
                maxX = max(maxX, x)
                minY = min(minY, y)
                maxY = max(maxY, y)
                pi += VERTEX_STEP
            }

            if (minX == Float.Companion.MAX_VALUE) {
                continue  // 有効な点が1つも取れなかったのでスキップする
            }

            // 全体の矩形に反映
            clippedDrawTotalMinX = min(clippedDrawTotalMinX, minX)
            clippedDrawTotalMaxX = max(clippedDrawTotalMaxX, maxX)
            clippedDrawTotalMinY = min(clippedDrawTotalMinY, minY)
            clippedDrawTotalMaxY = max(clippedDrawTotalMaxY, maxY)
        }

        if (clippedDrawTotalMinX == Float.Companion.MAX_VALUE) {
            val clippedDrawRect: csmRectF = clipContext.allClippedDrawRect
            clippedDrawRect.x = 0.0f
            clippedDrawRect.y = 0.0f
            clippedDrawRect.width = 0.0f
            clippedDrawRect.height = 0.0f
            return false
        } else {
            val w = clippedDrawTotalMaxX - clippedDrawTotalMinX
            val h = clippedDrawTotalMaxY - clippedDrawTotalMinY

            val clippedDrawRect: csmRectF = clipContext.allClippedDrawRect
            clippedDrawRect.x = clippedDrawTotalMinX
            clippedDrawRect.y = clippedDrawTotalMinY
            clippedDrawRect.width = w
            clippedDrawRect.height = h
            return true
        }
    }

    fun setupLayoutBounds(usingClipCount: Int) {
        val useClippingMaskMaxCount = if (offscreenSurfaces.size <= 1)
            CLIPPING_MASK_MAX_COUNT_ON_DEFAULT
        else
            CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE * offscreenSurfaces.size

        // レンダーテクスチャが1枚なら9分割する（最大36枚）
        val layoutCountMaxValue = if (offscreenSurfaces.size <= 1) 9 else 8

        // ひとつのRenderTextureを極力いっぱいに使ってマスクをレイアウトする。
        // マスクグループの数が4以下ならRGBA各チャンネルに１つずつマスクを配置し、5以上6以下ならRGBAを2,2,1,1と配置する。
        // NOTE: 1枚に割り当てるマスクの分割数を取りたいため、小数点は切り上げる。
        val countPerSheetDiv =
            (usingClipCount + offscreenSurfaces.size - 1) / offscreenSurfaces.size // レンダーテクスチャ1枚あたり何枚割り当てるか
        val reduceLayoutTextureCount =
            usingClipCount % offscreenSurfaces.size // レイアウトの数を1枚減らすレンダーテクスチャの数（この数だけのレンダーテクスチャが対象）。

        // RGBAを順番に使っていく。
        val divCount = countPerSheetDiv / COLOR_CHANNEL_COUNT // 1チャンネルに配置する基本のマスク個数
        val modCount =
            countPerSheetDiv % COLOR_CHANNEL_COUNT // 余り、この番号のチャンネルまでに1つずつ配分する（インデックスではない）

        // RGBAそれぞれのチャンネルを用意していく(0:R , 1:G , 2:B, 3:A, )
        val iterator = clipContext_2_drawableIndexList.keys.iterator()

        for (renderTextureIndex in 0..<offscreenSurfaces.size) {
            for (channelIndex in 0..<COLOR_CHANNEL_COUNT) {
                // このチャンネルにレイアウトする数
                // NOTE: レイアウト数 = 1チャンネルに配置する基本のマスク + 余りのマスクを置くチャンネルなら1つ追加
                var layoutCount = divCount + (if (channelIndex < modCount) 1 else 0)

                // レイアウトの数を1枚減らす場合にそれを行うチャンネルを決定
                // divが0の時は正常なインデックスの範囲になるように調整
                val checkChannelIndex = modCount + (if (divCount < 1) -1 else 0)

                // 今回が対象のチャンネルかつ、レイアウトの数を1枚減らすレンダーテクスチャが存在する場合
                if (channelIndex == checkChannelIndex && reduceLayoutTextureCount > 0) {
                    // 現在のレンダーテクスチャが、対象のレンダーテクスチャであればレイアウトの数を1枚減らす。
                    layoutCount -= if (renderTextureIndex >= reduceLayoutTextureCount) 1 else 0
                }

                // 分割方法を決定する。
                if (layoutCount == 0) {
                    // 何もしない。
                } else if (layoutCount == 1) {
                    // 全てをそのまま使う。
                    val cc: ClipContext = iterator.next()
                    cc.layoutChannelIndex = channelIndex
                    val bounds: csmRectF = cc.layoutBounds

                    bounds.x = 0.0f
                    bounds.y = 0.0f
                    bounds.width = 1.0f
                    bounds.height = 1.0f

                    cc.bufferIndex = renderTextureIndex
                } else if (layoutCount == 2) {
                    for (i in 0..<layoutCount) {
                        val xpos = i % 2

                        val cc: ClipContext = iterator.next()

                        cc.layoutChannelIndex = channelIndex
                        val bounds: csmRectF = cc.layoutBounds

                        // UVを2つに分解して使う
                        bounds.x = xpos * 0.5f
                        bounds.y = 0.0f
                        bounds.width = 0.5f
                        bounds.height = 1.0f

                        cc.bufferIndex = renderTextureIndex
                    }
                } else if (layoutCount <= 4) {
                    // 4分割して使う
                    for (i in 0..<layoutCount) {
                        val xpos = i % 2
                        val ypos = i / 2

                        val cc: ClipContext = iterator.next()

                        cc.layoutChannelIndex = channelIndex
                        val bounds: csmRectF = cc.layoutBounds

                        bounds.x = xpos * 0.5f
                        bounds.y = ypos * 0.5f
                        bounds.width = 0.5f
                        bounds.height = 0.5f

                        cc.bufferIndex = renderTextureIndex
                    }
                } else if (layoutCount <= layoutCountMaxValue) {
                    // 9分割して使う
                    for (i in 0..<layoutCount) {
                        val xpos = i % 3
                        val ypos = i / 3

                        val cc: ClipContext = iterator.next()

                        cc.layoutChannelIndex = channelIndex
                        val bounds: csmRectF = cc.layoutBounds

                        bounds.x = xpos / 3.0f
                        bounds.y = ypos / 3.0f
                        bounds.width = 1.0f / 3.0f
                        bounds.height = 1.0f / 3.0f

                        cc.bufferIndex = renderTextureIndex
                    }
                } else {
                    val count = usingClipCount - useClippingMaskMaxCount
//                    cubismLogError(
//                        "not supported mask count : %d\n[Details] render texture count: %d\n, mask count : %d",
//                        count,
//                        renderTextureCount,
//                        usingClipCount
//                    )

                    // 開発モードの場合は停止させる。
                    assert(false)

                    // 引き続き実行する場合、 SetupShaderProgramでオーバーアクセスが発生するので仕方なく適当に入れておく。
                    // もちろん描画結果はろくなことにならない。
                    for (i in 0..<layoutCount) {
                        val cc: ClipContext = iterator.next()

                        cc.layoutChannelIndex = 0

                        val bounds: csmRectF = cc.layoutBounds
                        bounds.x = 0.0f
                        bounds.y = 0.0f
                        bounds.width = 1.0f
                        bounds.height = 1.0f

                        cc.bufferIndex = 0
                    }
                }
            }
        }
    }

    fun createMatrixForMask(
        isRightHanded: Boolean,
        layoutBoundsOnTex01: csmRectF,
        scaleX: Float,
        scaleY: Float,
    ) {
        CubismMatrix44.create().apply {
            loadIdentity()
            // Layout0..1を、-1..1に変換
            translateRelative(-1.0f, -1.0f)
            scaleRelative(2.0f, 2.0f)

            // view to Layout0..1
            translateRelative(
                layoutBoundsOnTex01.x,
                layoutBoundsOnTex01.y
            )
            scaleRelative(scaleX, scaleY)
            translateRelative(
                -tmpBoundsOnModel.x,
                -tmpBoundsOnModel.y
            )
        }.also {
            tmpMatrixForMask.setMatrix(it)
        }

        CubismMatrix44.create().apply {
            loadIdentity()

            translateRelative(
                layoutBoundsOnTex01.x,
                layoutBoundsOnTex01.y * (if (isRightHanded) -1.0f else 1.0f)
            )
            scaleRelative(scaleX, scaleY * (if (isRightHanded) -1.0f else 1.0f))
            translateRelative(
                -tmpBoundsOnModel.x,
                -tmpBoundsOnModel.y
            )
        }.also {
            tmpMatrixForDraw.setMatrix(it)
        }
    }

    var clearedMaskBufferFlags = BooleanArray(0)
    private lateinit var currentMaskBuffer: ACubismOffscreenSurface
    var tmpBoundsOnModel: csmRectF = csmRectF()

    var tmpMatrixForMask: CubismMatrix44 = CubismMatrix44.create()

    var tmpMatrixForDraw: CubismMatrix44 = CubismMatrix44.create()

    override fun setupMask() {
        val usingClipCount = clipContext_2_drawableIndexList.keys.count {
            calcClippedDrawTotalBounds(it)
        }.takeIf { it > 0 } ?: return

        glViewport(
            0,
            0,
            512,
            512,
        )
        // 後の計算のためにインデックスの最初をセットする。
        currentMaskBuffer = offscreenSurfaces[0]
        // マスク描画処理
        currentMaskBuffer.beginDraw()

        // Determine the layout of each mask.
        setupLayoutBounds(usingClipCount)

        // サイズがレンダーテクスチャの枚数と合わない場合は合わせる。
        if (clearedMaskBufferFlags.size != offscreenSurfaces.size) {
            clearedMaskBufferFlags = BooleanArray(offscreenSurfaces.size)
        } else {
            for (i in 0..<offscreenSurfaces.size) {
                clearedMaskBufferFlags[i] = false
            }
        }
        // ---------- Mask Drawing Process -----------
        // Actually generate the masks.
        // Determine how to layout and draw all the masks, and store them in ClipContext and ClippedDrawContext.
        for (clipContext in clipContext_2_drawableIndexList.keys) {

            // The enclosing rectangle in logical coordinates of all drawing objects that use this mask.
            val allClippedDrawRect: csmRectF = clipContext.allClippedDrawRect
            // Fit the mask in here.
            val layoutBoundsOnTex01: csmRectF = clipContext.layoutBounds

            val margin = 0.05f

            // clipContextに設定したオフスクリーンサーフェスをインデックスで取得
            val clipContextOffscreenSurface = offscreenSurfaces[clipContext.bufferIndex]

            // 現在のオフスクリーンサーフェスがclipContextのものと異なる場合
            if (currentMaskBuffer != clipContextOffscreenSurface) {
                currentMaskBuffer.endDraw()
                currentMaskBuffer = clipContextOffscreenSurface

                // マスク用RenderTextureをactiveにセット。
                currentMaskBuffer.beginDraw()

            }


            // Use a rectangle on the model coordinates with margins as appropriate.
            tmpBoundsOnModel = allClippedDrawRect.copy()

            tmpBoundsOnModel.expand(
                allClippedDrawRect.width * margin,
                allClippedDrawRect.height * margin
            )

            // ######## It is best to keep the size to a minimum, rather than using the entire allocated space.
            // Find the formula for the shader. If rotation is not taken into account, the formula is as follows.
            // movePeriod' = movePeriod * scaleX + offX     [[ movePeriod' = (movePeriod - tmpBoundsOnModel.movePeriod)*scale + layoutBoundsOnTex01.movePeriod ]]
            val scaleX = layoutBoundsOnTex01.width / tmpBoundsOnModel.width
            val scaleY = layoutBoundsOnTex01.height / tmpBoundsOnModel.height

            // Calculate the matrix to be used for mask generation.
            createMatrixForMask(
                false,
                layoutBoundsOnTex01,
                scaleX,
                scaleY
            )

            clipContext.matrixForMask.setMatrix(tmpMatrixForMask)
            clipContext.matrixForDraw.setMatrix(tmpMatrixForDraw)

            // 実際の描画を行う。
            for (maskIndex in clipContext.maskIndexArray) {
                val drawableContext = drawableContextArray[maskIndex]

                if (!drawableContext.vertexPositionDidChange) continue

                // マスクがクリアされていないなら処理する。
                if (!clearedMaskBufferFlags[clipContext.bufferIndex]) {
                    // マスクをクリアする。
                    // (仮仕様) 1が無効（描かれない）領域、0が有効（描かれる）領域。（シェーダーCd*Csで0に近い値をかけてマスクを作る。1をかけると何も起こらない）
                    glClearColor(
                        1.0f,
                        1.0f,
                        1.0f,
                        1.0f
                    )
                    glClear(GL_COLOR_BUFFER_BIT)
                    clearedMaskBufferFlags[clipContext.bufferIndex] = true
                }
                currentClipContextForSetupMask = clipContext
                drawMesh(
                    drawableContext
                )
            }
        }


        // --- Post Processing ---
        // Return the drawing target
        currentMaskBuffer.endDraw()

        Live2DRendererProfile.lastViewport.forEach {
            println(it)
        }
        glViewport(
            Live2DRendererProfile.lastViewport[0],
            Live2DRendererProfile.lastViewport[1],
            Live2DRendererProfile.lastViewport[2],
            Live2DRendererProfile.lastViewport[3],
        )
    }

    lateinit var currentClipContextForSetupMask: ClipContext



    override fun drawMesh(
        drawableContext: DrawableContext,
    ) {
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

        if (drawableContext.isCulling) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }
        glFrontFace(GL_CCW)

        when (state) {
            State.SETUP_MASK -> {
                Live2DShader.setupMask(
                    this,
                    drawableContext,
                    currentClipContextForSetupMask
                )
            }

            State.DRAW -> {
                drawableContext.clipContext?.let {

                    Live2DShader.drawMasked(
                        this,
                        drawableContext
                    )
                } ?: run {
                    Live2DShader.drawSimple(
                        this,
                        drawableContext
                    )
                }
            }
        }
        glDrawElements(
            GL_TRIANGLES,
            drawableContext.vertex.indices
        )

    }

}
