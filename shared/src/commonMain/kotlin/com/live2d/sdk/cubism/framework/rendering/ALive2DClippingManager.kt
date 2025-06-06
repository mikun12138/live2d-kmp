/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_OFFSET
import com.live2d.sdk.cubism.framework.Live2DFramework.VERTEX_STEP
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismVector2
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.rendering.ILive2DClippingManager.Companion.CLIPPING_MASK_MAX_COUNT_ON_DEFAULT
import com.live2d.sdk.cubism.framework.rendering.ILive2DClippingManager.Companion.CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE
import com.live2d.sdk.cubism.framework.rendering.ILive2DClippingManager.Companion.COLOR_CHANNEL_COUNT
import com.live2d.sdk.cubism.framework.type.csmRectF
import kotlin.math.max
import kotlin.math.min


expect fun ACubismClippingManager.Companion.create(
    model: Live2DModel,
    maskBufferCount: Int,
): ACubismClippingManager

abstract class ACubismClippingManager : ILive2DClippingManager {

    constructor(
        model: Live2DModel,
        maskBufferCount: Int,
    ) {
        renderTextureCount = maskBufferCount

        // レンダーテクスチャのクリアフラグの配列の初期化
        clearedMaskBufferFlags = BooleanArray(renderTextureCount)

        val drawableCount: Int = model.drawableCount // 描画オブジェクトの数
        val drawableMasks: Array<IntArray?> =
            model.model.drawables.masks // 描画オブジェクトをマスクする描画オブジェクトのインデックスのリスト
        val drawableMaskCounts: IntArray =
            model.model.drawables.maskCounts // 描画オブジェクトをマスクする描画オブジェクトの数

        // クリッピングマスクを使う描画オブジェクトを全て登録する。
        // クリッピングマスクは、通常数個程度に限定して使うものとする。
        for (i in 0..<drawableCount) {
            if (drawableMaskCounts[i] <= 0) {
                // クリッピングマスクが使用されていないアートメッシュ（多くの場合使用しない）
                clippingContextListForDraw.add(null)
                continue
            }

            // 既にあるClipContextと同じかチェックする。
            var cc: Live2DClippingContext? = findSameClip(drawableMasks[i]!!, drawableMaskCounts[i])
            if (cc == null) {
                // 同一のマスクが存在していない場合は生成する。
                cc = Live2DClippingContext(
                    this,
                    drawableMasks[i]!!,
                    drawableMaskCounts[i]
                )

//                clippingContextListForMask.add(cc)
                drawIndex_2_clippingContext.add(i to cc)
            }

            cc!!.addClippedDrawable(i)
            clippingContextListForDraw.add(cc)
        }
    }
    /*

        override fun setupMatrixForHighPrecision(model: Model, isRightHanded: Boolean) {
            // 全てのクリッピングを用意する。
            // 同じクリップ（複数の場合はまとめて1つのクリップ）を使う場合は1度だけ設定する。
            var usingClipCount = 0
            for (clipIndex in clippingContextListForMask.indices) {
                // 1つのクリッピングマスクに関して
                val cc: CubismClippingContext = clippingContextListForMask.get(clipIndex)

                // このクリップを利用する描画オブジェクト群全体を囲む矩形を計算
                calcClippedDrawTotalBounds(model, cc)

                if (cc.isUsing) {
                    usingClipCount++ // 使用中としてカウント
                }
            }

            // マスク行列作成処理
            if (usingClipCount <= 0) {
                return  // クリッピングマスクが存在しない場合何もしない。
            }

            setupLayoutBounds(0)

            // サイズがレンダーテクスチャの枚数と合わない場合は合わせる。
            if (clearedMaskBufferFlags!!.size != renderTextureCount) {
                clearedMaskBufferFlags = BooleanArray(renderTextureCount)
            } else {
                for (i in 0..<renderTextureCount) {
                    clearedMaskBufferFlags!![i] = false
                }
            }

            // 実際にマスクを生成する。
            // 全てのマスクをどのようにレイアウトして描くかを決定し、ClipContext, ClippedDrawContextに記憶する。
            for (clipIndex in clippingContextListForMask.indices) {
                // ---- 実際に1つのマスクを描く ----
                val clipContext: CubismClippingContext = clippingContextListForMask.get(clipIndex)
                val allClippedDrawRect: csmRectF =
                    clipContext.allClippedDrawRect // このマスクを使う、全ての描画オブジェクトの論理座標上の囲み矩形
                val layoutBoundsOnTex01: csmRectF = clipContext.layoutBounds // このマスクを収める

                val margin = 0.05f
                val scaleX: Float
                val scaleY: Float
                val ppu: Float = model.getPixelPerUnit()
                val maskPixelWidth: Float =
                    clipContext.getClippingManager().getClippingMaskBufferSize().x
                val maskPixelHeight: Float =
                    clipContext.getClippingManager().getClippingMaskBufferSize().y
                val physicalMaskWidth = layoutBoundsOnTex01.width * maskPixelWidth
                val physicalMaskHeight = layoutBoundsOnTex01.height * maskPixelHeight

                tmpBoundsOnModel.setRect(allClippedDrawRect)

                if (tmpBoundsOnModel.width * ppu > physicalMaskWidth) {
                    tmpBoundsOnModel.expand(allClippedDrawRect.width * margin, 0.0f)
                    scaleX = layoutBoundsOnTex01.width / tmpBoundsOnModel.width
                } else {
                    scaleX = ppu / physicalMaskWidth
                }

                if (tmpBoundsOnModel.height * ppu > physicalMaskHeight) {
                    tmpBoundsOnModel.expand(0.0f, allClippedDrawRect.height * margin)
                    scaleY = layoutBoundsOnTex01.height / tmpBoundsOnModel.height
                } else {
                    scaleY = ppu / physicalMaskHeight
                }

                // マスク生成時に使う行列を求める。
                createMatrixForMask(isRightHanded, layoutBoundsOnTex01, scaleX, scaleY)

                clipContext.matrixForMask.setMatrix(tmpMatrixForMask.getArray())
                clipContext.matrixForDraw.setMatrix(tmpMatrixForDraw.getArray())
            }
        }

    */

    fun createMatrixForMask(
        isRightHanded: Boolean,
        layoutBoundsOnTex01: csmRectF,
        scaleX: Float,
        scaleY: Float,
    ) {
        // マスク作成用の行列の計算
        tmpMatrix.loadIdentity()
        run {
            // Layout0..1を、-1..1に変換
            tmpMatrix.translateRelative(-1.0f, -1.0f)
            tmpMatrix.scaleRelative(2.0f, 2.0f)

            // view to Layout0..1
            tmpMatrix.translateRelative(
                layoutBoundsOnTex01.x,
                layoutBoundsOnTex01.y
            )
            tmpMatrix.scaleRelative(scaleX, scaleY)
            tmpMatrix.translateRelative(
                -tmpBoundsOnModel.x,
                -tmpBoundsOnModel.y
            )
        }
        tmpMatrixForMask.setMatrix(tmpMatrix)

        // 描画用の行列の計算
        tmpMatrix.loadIdentity()
        run {
            tmpMatrix.translateRelative(
                layoutBoundsOnTex01.x,
                layoutBoundsOnTex01.y * (if (isRightHanded) -1.0f else 1.0f)
            )
            tmpMatrix.scaleRelative(scaleX, scaleY * (if (isRightHanded) -1.0f else 1.0f))
            tmpMatrix.translateRelative(
                -tmpBoundsOnModel.x,
                -tmpBoundsOnModel.y
            )
        }
        tmpMatrixForDraw.setMatrix(tmpMatrix)
    }

    fun setupLayoutBounds(usingClipCount: Int) {
        val useClippingMaskMaxCount = if (renderTextureCount <= 1)
            CLIPPING_MASK_MAX_COUNT_ON_DEFAULT
        else
            CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE * renderTextureCount

        if (usingClipCount <= 0 || usingClipCount > useClippingMaskMaxCount) {
            // この場合は一つのマスクターゲットを毎回クリアして使用する
            for ((index, cc) in drawIndex_2_clippingContext) {
//                val cc: Live2DClippingContext = clippingContextListForMask.get(index)!!

                cc.layoutChannelIndex = 0 // どうせ毎回消すので固定で良い
                cc.layoutBounds.x = 0.0f
                cc.layoutBounds.y = 0.0f
                cc.layoutBounds.width = 1.0f
                cc.layoutBounds.height = 1.0f
                cc.bufferIndex = 0
            }
            return
        }

        // レンダーテクスチャが1枚なら9分割する（最大36枚）
        val layoutCountMaxValue = if (renderTextureCount <= 1) 9 else 8

        // ひとつのRenderTextureを極力いっぱいに使ってマスクをレイアウトする。
        // マスクグループの数が4以下ならRGBA各チャンネルに１つずつマスクを配置し、5以上6以下ならRGBAを2,2,1,1と配置する。
        // NOTE: 1枚に割り当てるマスクの分割数を取りたいため、小数点は切り上げる。
        val countPerSheetDiv =
            (usingClipCount + renderTextureCount - 1) / renderTextureCount // レンダーテクスチャ1枚あたり何枚割り当てるか
        val reduceLayoutTextureCount =
            usingClipCount % renderTextureCount // レイアウトの数を1枚減らすレンダーテクスチャの数（この数だけのレンダーテクスチャが対象）。

        // RGBAを順番に使っていく。
        val divCount = countPerSheetDiv / COLOR_CHANNEL_COUNT // 1チャンネルに配置する基本のマスク個数
        val modCount =
            countPerSheetDiv % COLOR_CHANNEL_COUNT // 余り、この番号のチャンネルまでに1つずつ配分する（インデックスではない）

        // RGBAそれぞれのチャンネルを用意していく(0:R , 1:G , 2:B, 3:A, )
        var curClipIndex = 0 // 順番に設定していく

        for (renderTextureIndex in 0..<renderTextureCount) {
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
                    val cc: Live2DClippingContext = drawIndex_2_clippingContext.get(curClipIndex++)!!.second
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

                        val cc: Live2DClippingContext =
                            drawIndex_2_clippingContext.get(curClipIndex++)!!.second
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

                        val cc: Live2DClippingContext =
                            drawIndex_2_clippingContext.get(curClipIndex++)!!.second
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

                        val cc: Live2DClippingContext =
                            drawIndex_2_clippingContext.get(curClipIndex++)!!.second
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
                        val cc: Live2DClippingContext =
                            drawIndex_2_clippingContext.get(curClipIndex++)!!.second
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

    fun findSameClip(drawableMasks: IntArray, drawableMaskCounts: Int): Live2DClippingContext? {
        // 作成済みClippingContextと一致するか確認
        for ((_, clipContext) in drawIndex_2_clippingContext) {
//            val clipContext: Live2DClippingContext = clippingContextListForMask.get(i)!!

            val count: Int = clipContext.clippingIdCount
            if (count != drawableMaskCounts) {
                // 個数が違う場合は別物
                continue
            }
            var sameCount = 0

            // 同じIDを持つか確認。配列の数が同じなので、一致した個数が同じなら同じ物を持つとする。
            for (j in 0..<count) {
                val clipId: Int = clipContext.clippingIdList[j]
                for (k in 0..<count) {
                    if (drawableMasks[k] == clipId) {
                        sameCount++
                        break
                    }
                }
            }
            if (sameCount == count) {
                return clipContext
            }
        }

        return null // 見つからなかった。
    }

    fun calcClippedDrawTotalBounds(model: Live2DModel, clippingContext: Live2DClippingContext) {
        // 被クリッピングマスク（マスクされる描画オブジェクト）の全体の矩形
        var clippedDrawTotalMinX = Float.Companion.MAX_VALUE
        var clippedDrawTotalMinY = Float.Companion.MAX_VALUE
        var clippedDrawTotalMaxX = -Float.Companion.MAX_VALUE
        var clippedDrawTotalMaxY = -Float.Companion.MAX_VALUE

        // このマスクが実際に必要か判定する。
        // このクリッピングを利用する「描画オブジェクト」がひとつでも使用可能であればマスクを生成する必要がある。
        val clippedDrawCount: Int = clippingContext.clippedDrawableIndexList.size
        for (clippedDrawableIndex in 0..<clippedDrawCount) {
            // マスクを使用する描画オブジェクトの描画される矩形を求める。
            val drawableIndex: Int =
                clippingContext.clippedDrawableIndexList.get(clippedDrawableIndex)!!

            val drawableVertexCount: Int = model.model.drawableViews[drawableIndex].vertexCount
            val drawableVertices: FloatArray = model.getDrawableVertexPositions(drawableIndex)!!

            var minX = Float.Companion.MAX_VALUE
            var minY = Float.Companion.MAX_VALUE
            var maxX = -Float.Companion.MAX_VALUE
            var maxY = -Float.Companion.MAX_VALUE

            val loop = drawableVertexCount * VERTEX_STEP
            var pi = VERTEX_OFFSET
            while (pi < loop) {
                val x = drawableVertices[pi]
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

            val clippedDrawRect: csmRectF = clippingContext.allClippedDrawRect
            clippedDrawRect.x = 0.0f
            clippedDrawRect.y = 0.0f
            clippedDrawRect.width = 0.0f
            clippedDrawRect.height = 0.0f
        } else {
            clippingContext.isUsing = true
            val w = clippedDrawTotalMaxX - clippedDrawTotalMinX
            val h = clippedDrawTotalMaxY - clippedDrawTotalMinY

            val clippedDrawRect: csmRectF = clippingContext.allClippedDrawRect
            clippedDrawRect.x = clippedDrawTotalMinX
            clippedDrawRect.y = clippedDrawTotalMinY
            clippedDrawRect.width = w
            clippedDrawRect.height = h
        }
    }

    protected var clearedMaskBufferFlags: BooleanArray = BooleanArray(0)

    val channelColors: List<CubismTextureColor> = listOf(
        CubismTextureColor(
            r = 1.0f,
            g = 0.0f,
            b = 0.0f,
            a = 0.0f,
        ),
        CubismTextureColor(
            r = 0.0f,
            g = 1.0f,
            b = 0.0f,
            a = 0.0f,
        ),
        CubismTextureColor(
            r = 0.0f,
            g = 0.0f,
            b = 1.0f,
            a = 0.0f,
        ),
        CubismTextureColor(
            r = 0.0f,
            g = 0.0f,
            b = 0.0f,
            a = 1.0f,
        )
    )

    val drawIndex_2_clippingContext: MutableList<Pair<Int, Live2DClippingContext>> = mutableListOf()
//    val clippingContext_2_drawIndexList: MutableList<Pair<Live2DClippingContext, List<Int>>> = mutableListOf()

//    val clippingContextListForMask: MutableList<Live2DClippingContext> = mutableListOf()

    val clippingContextListForDraw: MutableList<Live2DClippingContext?> = mutableListOf()

    val clippingMaskBufferSize: CubismVector2 = CubismVector2(256f, 256f)

    /**
     * 生成するレンダーテクスチャの枚数
     */
    protected var renderTextureCount: Int = 0

    /**
     * 一時計算用行列
     */
    protected var tmpMatrix: CubismMatrix44 = CubismMatrix44.create()

    /**
     * マスク計算のための一時計算用行列
     */
    protected var tmpMatrixForMask: CubismMatrix44 = CubismMatrix44.create()

    /**
     * 描画用の一時計算用行列
     */
    protected var tmpMatrixForDraw: CubismMatrix44 = CubismMatrix44.create()

    /**
     * マスク配置計算用の一時計算用矩形
     */
    protected var tmpBoundsOnModel: csmRectF = csmRectF()

    companion object
}
