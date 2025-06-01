/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.CubismFramework.VERTEX_OFFSET
import com.live2d.sdk.cubism.framework.CubismFramework.VERTEX_STEP
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.Model
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_BUFFER_SIZE_X
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_BUFFER_SIZE_Y
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_MAX_COUNT_ON_DEFAULT
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE
import com.live2d.sdk.cubism.framework.rendering.ICubismClippingManager.Companion.COLOR_CHANNEL_COUNT
import com.live2d.sdk.cubism.framework.type.csmRectF
import com.live2d.sdk.cubism.framework.type.expand
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogError
import kotlin.math.max
import kotlin.math.min

expect fun ACubismClippingManager.Companion.create(
    model: Model,
    maskBufferCount: Int,
): ACubismClippingManager

abstract class ACubismClippingManager : ICubismClippingManager {

    val framebufferCount: Int
    val clippingContextListForDraw: MutableList<CubismClippingContext?> = mutableListOf()
    val clippingContextListForMask: MutableList<CubismClippingContext> = mutableListOf()
    val clippingContextForMask_2_ClippedDrawableIndexList: MutableMap<CubismClippingContext, MutableList<Int>> =
        mutableMapOf()

    val offscreenSurfaces_2_clippingContextForMaskList
            : Array<Pair<ACubismOffscreenSurface, List<CubismClippingContext>>> by lazy {
        Array(framebufferCount) { index ->
            ACubismOffscreenSurface.create().apply {
                createOffscreenSurface(
                    CLIPPING_MASK_BUFFER_SIZE_X,
                    CLIPPING_MASK_BUFFER_SIZE_Y,
                )
            } to clippingContextForMask_2_ClippedDrawableIndexList.keys.filter { it.bufferIndex == index }
        }
    }

    constructor(
        model: Model,
        maskBufferCount: Int,
    ) {
        framebufferCount = maskBufferCount

        val drawableCount = model.drawableCount // 描画オブジェクトの数
        val drawableMasks = model.model.drawables.masks // 描画オブジェクトをマスクする描画オブジェクトのインデックスのリスト
        val drawableMaskCounts = model.model.drawables.maskCounts // 描画オブジェクトをマスクする描画オブジェクトの数

        // クリッピングマスクを使う描画オブジェクトを全て登録する。
        // クリッピングマスクは、通常数個程度に限定して使うものとする。
        repeat(drawableCount) { drawableIndex ->
            // 若无mask, 则添加null
            if (drawableMaskCounts[drawableIndex] <= 0) {
                // クリッピングマスクが使用されていないアートメッシュ（多くの場合使用しない）
                clippingContextListForDraw.add(null)
                return@repeat
            }

            // 既にあるClipContextと同じかチェックする。
            val cc =
                findSameClip(drawableMasks[drawableIndex]!!)
                    ?: run {
                        CubismClippingContext(
                            this,
                            drawableMasks[drawableIndex]!!,
                        ).also {
                            clippingContextListForMask.add(it)
                        }
                    }

            clippingContextListForDraw.add(cc)
            clippingContextForMask_2_ClippedDrawableIndexList.getOrPut(cc) {
                mutableListOf()
            }.add(drawableIndex)
        }
    }

    /*
        TODO::High版本好像有点问题
     */
    fun setupMatrixForHighPrecision(model: Model) {
        // 全てのクリッピングを用意する。
        // 同じクリップ（複数の場合はまとめて1つのクリップ）を使う場合は1度だけ設定する。
        clippingContextForMask_2_ClippedDrawableIndexList.keys.count {
            calcClippedDrawTotalBounds(model, it)
            it.isUsing
        }.takeIf { it > 0 }?.let {

            setupLayoutBounds(0)

            // 実際にマスクを生成する。
            // 全てのマスクをどのようにレイアウトして描くかを決定し、ClipContext, ClippedDrawContextに記憶する。
            for (clipContext in clippingContextForMask_2_ClippedDrawableIndexList.keys) {

                val margin = 0.05f
                val allClippedDrawRectActually = clipContext.allClippedDrawRect.copy()

                val scaleX: Float
                run {
                    val physicalMaskWidth =
                        clipContext.layoutBounds.width * CLIPPING_MASK_BUFFER_SIZE_X
                    // 绘制区域大于mask的大小的时候 添加边距 ???
                    if (clipContext.allClippedDrawRect.width * model.pixelPerUnit > physicalMaskWidth) {
                        // 拓展自身的0.05
                        allClippedDrawRectActually.expand(
                            clipContext.allClippedDrawRect.width * margin,
                            0.0f
                        )
                        scaleX = clipContext.layoutBounds.width / allClippedDrawRectActually.width
                    } else {
                        scaleX = model.pixelPerUnit / physicalMaskWidth
                    }
                }

                val scaleY: Float
                run {
                    val physicalMaskHeight =
                        clipContext.layoutBounds.height * CLIPPING_MASK_BUFFER_SIZE_Y
                    if (clipContext.allClippedDrawRect.height * model.pixelPerUnit > physicalMaskHeight) {
                        allClippedDrawRectActually.expand(
                            0.0f,
                            clipContext.allClippedDrawRect.height * margin
                        )
                        scaleY = clipContext.layoutBounds.height / allClippedDrawRectActually.height
                    } else {
                        scaleY = model.pixelPerUnit / physicalMaskHeight
                    }
                }

                clipContext.matrixForMask.setMatrix(
                    createMatrixForMask(
                        clipContext.layoutBounds,
                        scaleX,
                        scaleY,
                        allClippedDrawRectActually
                    ).tr
                )
                clipContext.matrixForDraw.setMatrix(
                    createMatrixForDraw(
                        clipContext.layoutBounds,
                        scaleX,
                        scaleY,
                        allClippedDrawRectActually
                    ).tr
                )
            }
        }
    }

    abstract fun setupClippingContext(model: Model, renderer: Live2DRenderer)

    protected fun createMatrixForMask(
        layoutBoundsOnTex01: csmRectF,
        scaleX: Float,
        scaleY: Float,
        allClippedDrawRectActually: csmRectF,
    ): CubismMatrix44 {
        // マスク作成用の行列の計算
        return CubismMatrix44.create().apply {
            this.loadIdentity()
            // Layout0..1を、-1..1に変換
            run {
                this.translateRelative(-1.0f, -1.0f)
                this.scaleRelative(2.0f, 2.0f)
            }
            // view to Layout0..1
            run {
                this.translateRelative(
                    layoutBoundsOnTex01.x,
                    layoutBoundsOnTex01.y
                )
            }

            this.scaleRelative(scaleX, scaleY)
            this.translateRelative(
                -allClippedDrawRectActually.x,
                -allClippedDrawRectActually.y
            )
        }
    }

    protected fun createMatrixForDraw(
        layoutBoundsOnTex01: csmRectF,
        scaleX: Float,
        scaleY: Float,
        allClippedDrawRectActually: csmRectF,
    ): CubismMatrix44 {
        return CubismMatrix44.create().apply {
            // 描画用の行列の計算
            this.loadIdentity()
            this.translateRelative(
                layoutBoundsOnTex01.x,
                layoutBoundsOnTex01.y
            )
            this.scaleRelative(scaleX, scaleY)
            this.translateRelative(
                -allClippedDrawRectActually.x,
                -allClippedDrawRectActually.y
            )
        }
    }


    // TODO:: 这玩意似乎只需要执行一次
    protected fun setupLayoutBounds(usingClipCount: Int) {
        val useClippingMaskMaxCount = if (framebufferCount <= 1)
            CLIPPING_MASK_MAX_COUNT_ON_DEFAULT
        else
            CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE * framebufferCount

        if (usingClipCount !in 0 until useClippingMaskMaxCount) {
//            if (usingClipCount > useClippingMaskMaxCount) {
//                // マスクの制限数の警告を出す
//                val count = usingClipCount - useClippingMaskMaxCount
//                cubismLogError(
//                    "not supported mask count :$count\n[Details] render texture count: $framebufferCount\n, mask count : $usingClipCount",
//                )
//            }
            // この場合は一つのマスクターゲットを毎回クリアして使用する
            clippingContextForMask_2_ClippedDrawableIndexList.keys.forEach { cc ->
                cc.layoutChannelIndex = 0 // どうせ毎回消すので固定で良い
                cc.layoutBounds = csmRectF()
//                cc.bufferIndex = 0
            }
            return
        }

        // TODO:: use this?
//        val result = Array(renderTextureCount) {
//            IntArray(4)
//        }
//
//        val div0 = usingClipCount / renderTextureCount
//        val mod0 = usingClipCount % renderTextureCount
//
//        repeat(renderTextureCount) { loop0 ->
//            val count0 = div0 + if (loop0 < mod0) 1 else 0
//
//            val div1 = count0 / 4
//            val mod1 = count0 % 4
//
//            repeat(4) { loop1 ->
//                val count1 = div1 + if (loop1 < mod1) 1 else 0
//                result[loop0][loop1] = count1
//            }
//        }


        // レンダーテクスチャが1枚なら9分割する（最大36枚）
        // 4个通道除以4
        val layoutCountMaxValue =
            if (framebufferCount <= 1)
                CLIPPING_MASK_MAX_COUNT_ON_DEFAULT / COLOR_CHANNEL_COUNT
            else
                CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE / COLOR_CHANNEL_COUNT

        // ひとつのRenderTextureを極力いっぱいに使ってマスクをレイアウトする。
        // マスクグループの数が4以下ならRGBA各チャンネルに１つずつマスクを配置し、5以上6以下ならRGBAを2,2,1,1と配置する。
        // NOTE: 1枚に割り当てるマスクの分割数を取りたいため、小数点は切り上げる。
        // 每个纹理使用的遮罩数 (向上取整)
        val countPerSheetDiv =
            (usingClipCount + framebufferCount - 1) / framebufferCount // レンダーテクスチャ1枚あたり何枚割り当てるか
        // 部分纹理可省个通道
        val reduceLayoutTextureCount =
            usingClipCount % framebufferCount // レイアウトの数を1枚減らすレンダーテクスチャの数（この数だけのレンダーテクスチャが対象）。

        // RGBAを順番に使っていく。
        // 每个通道分配的遮罩数
        val divCount =
            countPerSheetDiv / COLOR_CHANNEL_COUNT // 1チャンネルに配置する基本のマスク個数
        // 多余另作分配
        val modCount =
            countPerSheetDiv % COLOR_CHANNEL_COUNT // 余り、この番号のチャンネルまでに1つずつ配分する（インデックスではない）

        // RGBAそれぞれのチャンネルを用意していく(0:R , 1:G , 2:B, 3:A, )
        var curClipIndex = 0 // 順番に設定していく

        // 遍历纹理
        for (renderTextureIndex in 0..<framebufferCount) {
            // 遍历通道
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
                    val cc = clippingContextListForMask.get(curClipIndex++)
                    cc.layoutChannelIndex = channelIndex
                    cc.bufferIndex = renderTextureIndex

                    cc.layoutBounds = csmRectF(
                        0.0f,
                        0.0f,
                        1.0f,
                        1.0f
                    )
                } else if (layoutCount == 2) { // 2切片
                    repeat(layoutCount) {
                        val cc = clippingContextListForMask.get(curClipIndex++)
                        cc.layoutChannelIndex = channelIndex
                        cc.bufferIndex = renderTextureIndex

                        val xpos = it % 2
                        cc.layoutBounds = csmRectF(
                            xpos * 0.5f,
                            0.0f,
                            0.5f,
                            1.0f
                        )
                    }
                } else if (layoutCount <= 4) { // 4切片
                    repeat(layoutCount) {
                        val cc = clippingContextListForMask.get(curClipIndex++)
                        cc.layoutChannelIndex = channelIndex
                        cc.bufferIndex = renderTextureIndex

                        val xpos = it % 2
                        val ypos = it / 2
                        cc.layoutBounds = csmRectF(
                            xpos * 0.5f,
                            ypos * 0.5f,
                            0.5f,
                            0.5f
                        )
                    }
                } else if (layoutCount <= layoutCountMaxValue) { //9切片
                    repeat(layoutCount) {
                        val cc = clippingContextListForMask.get(curClipIndex++)
                        cc.layoutChannelIndex = channelIndex
                        cc.bufferIndex = renderTextureIndex

                        val xpos = it % 3
                        val ypos = it / 3
                        cc.layoutBounds = csmRectF(
                            xpos / 3.0f,
                            ypos / 3.0f,
                            1.0f / 3.0f,
                            1.0f / 3.0f,
                        )
                    }
                } else {
                    val count = usingClipCount - useClippingMaskMaxCount
                    cubismLogError(
                        "not supported mask count : $count\n[Details] render texture count: $framebufferCount\n, mask count : $usingClipCount",
                    )

                    check(false)

                    // 引き続き実行する場合、 SetupShaderProgramでオーバーアクセスが発生するので仕方なく適当に入れておく。
                    // もちろん描画結果はろくなことにならない。
                    repeat(layoutCount) {
                        val cc = clippingContextListForMask.get(curClipIndex++)
                        cc.layoutChannelIndex = 0
                        cc.bufferIndex = 0

                        cc.layoutBounds = csmRectF(
                            0.0f,
                            0.0f,
                            1.0f,
                            1.0f
                        )
                    }
                }
            }
        }
    }

    private fun findSameClip(drawableMasks: IntArray): CubismClippingContext? {
        return clippingContextForMask_2_ClippedDrawableIndexList.keys.firstOrNull { clipContext ->
            clipContext.clippingIdList.size == drawableMasks.size
                    && clipContext.clippingIdList.all { clipId ->
                drawableMasks.any { it == clipId }
            }
        }
    }

    protected fun calcClippedDrawTotalBounds(model: Model, clippingContext: CubismClippingContext) {
        // 被クリッピングマスク（マスクされる描画オブジェクト）の全体の矩形
        var clippedDrawTotalMinX = Float.Companion.MAX_VALUE
        var clippedDrawTotalMinY = Float.Companion.MAX_VALUE
        var clippedDrawTotalMaxX = -Float.Companion.MAX_VALUE
        var clippedDrawTotalMaxY = -Float.Companion.MAX_VALUE

        // このマスクが実際に必要か判定する。
        // このクリッピングを利用する「描画オブジェクト」がひとつでも使用可能であればマスクを生成する必要がある。
        for (drawableIndex in clippingContextForMask_2_ClippedDrawableIndexList.get(clippingContext)!!) {
            // マスクを使用する描画オブジェクトの描画される矩形を求める。

            val drawableVertexCount = model.getDrawableVertexCount(drawableIndex)
            val drawableVertices = model.getDrawableVertexPositions(drawableIndex)

            var minX = Float.Companion.MAX_VALUE
            var minY = Float.Companion.MAX_VALUE
            var maxX = -Float.Companion.MAX_VALUE
            var maxY = -Float.Companion.MAX_VALUE

            var pi: Int = VERTEX_OFFSET
            repeat(drawableVertexCount) {
                val x = drawableVertices!![pi]
                val y = drawableVertices[pi + 1]
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
            clippingContext.isUsing = false
            clippingContext.allClippedDrawRect = csmRectF()
        } else {
            clippingContext.isUsing = true
            clippingContext.allClippedDrawRect = csmRectF(
                clippedDrawTotalMinX,
                clippedDrawTotalMinY,
                clippedDrawTotalMaxX - clippedDrawTotalMinX,
                clippedDrawTotalMaxY - clippedDrawTotalMinY
            )
        }
    }

    /**
     * カラーチャンネル(RGBA)のフラグのリスト(0:R, 1:G, 2:B, 3:A)
     */
    val channelColors: List<CubismTextureColor> = listOf(
        CubismTextureColor(
            1.0f, 0.0f, 0.0f, 0.0f
        ),
        CubismTextureColor(
            0.0f, 1.0f, 0.0f, 0.0f
        ),
        CubismTextureColor(
            0.0f, 0.0f, 1.0f, 0.0f
        ),
        CubismTextureColor(
            0.0f, 0.0f, 0.0f, 1.0f
        ),
    )
    companion object
}
