package com.live2d.sdk.cubism.ex.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.ex.rendering.ALive2DRenderer.State
import com.live2d.sdk.cubism.framework.type.csmRectF
import com.live2d.sdk.cubism.framework.utils.IState
import com.live2d.sdk.cubism.framework.utils.StateContext
import com.live2d.sdk.cubism.framework.utils.switchStateTo

abstract class ALive2DRenderer : StateContext<ALive2DRenderer, State> {
    override var state: State = State.SETUP_MASK
    val drawableContextArray: Array<DrawableContext>

    var mvp: CubismMatrix44 = CubismMatrix44.create()

    val offScreenSurfacesCount: Int
    abstract val offscreenSurfaces: Array<ALive2DOffscreenSurface>

    val clipContextList: MutableList<ClipContext> = mutableListOf()

    constructor(
        model: Live2DModel,
        offScreenSurfacesCount: Int,
    ) {

        drawableContextArray = Array(model.drawableCount) {
            DrawableContext(model, it)
        }

        this.offScreenSurfacesCount = offScreenSurfacesCount

        repeat(model.drawableCount) { index ->
            val drawableMask = model.getDrawableMask(index)!!
            if (drawableMask.isNotEmpty()) {
                val clipContext = clipContextList.find {
                    it.maskIndexArray.size == drawableMask.size
                            && it.maskIndexArray.all { drawableMask.contains(it) }
                } ?: run {
                    ClipContext(drawableMask).also {
                        clipContextList.add(it)
                    }
                }
                clipContext.let {
                    drawableContextArray[index].clipContext = it
                }
            }
        }

        setupLayoutBounds(clipContextList.size)
    }

    fun setupLayoutBounds(clipContextCount: Int) {
        val useClippingMaskMaxCount = if (offScreenSurfacesCount <= 1)
            CLIPPING_MASK_MAX_COUNT_ON_DEFAULT
        else
            CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE * offScreenSurfacesCount

        // レンダーテクスチャが1枚なら9分割する（最大36枚）
        val layoutCountMaxValue = if (offScreenSurfacesCount <= 1) 9 else 8

        // ひとつのRenderTextureを極力いっぱいに使ってマスクをレイアウトする。
        // マスクグループの数が4以下ならRGBA各チャンネルに１つずつマスクを配置し、5以上6以下ならRGBAを2,2,1,1と配置する。
        // NOTE: 1枚に割り当てるマスクの分割数を取りたいため、小数点は切り上げる。
        val countPerSheetDiv =
            (clipContextCount + offScreenSurfacesCount - 1) / offScreenSurfacesCount // レンダーテクスチャ1枚あたり何枚割り当てるか
        val reduceLayoutTextureCount =
            clipContextCount % offScreenSurfacesCount // レイアウトの数を1枚減らすレンダーテクスチャの数（この数だけのレンダーテクスチャが対象）。

        // RGBAを順番に使っていく。
        val divCount = countPerSheetDiv / COLOR_CHANNEL_COUNT // 1チャンネルに配置する基本のマスク個数
        val modCount =
            countPerSheetDiv % COLOR_CHANNEL_COUNT // 余り、この番号のチャンネルまでに1つずつ配分する（インデックスではない）

        // RGBAそれぞれのチャンネルを用意していく(0:R , 1:G , 2:B, 3:A, )
        val iterator = clipContextList.iterator()

        for (renderTextureIndex in 0..<offScreenSurfacesCount) {
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
                    val count = clipContextCount - useClippingMaskMaxCount
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

    fun frame(mvp: CubismMatrix44) {
        this.mvp.setMatrix(mvp)
        drawableContextArray.forEach {
            it.update()
        }
        this switchStateTo State.SETUP_MASK
        setupMask()
        this switchStateTo State.DRAW
        draw()
    }

    abstract fun setupMask()

    abstract fun draw()

    enum class State(
        override val onEnter: (ALive2DRenderer, State) -> Unit = { _, _ -> },
        override val onExit: (ALive2DRenderer, State) -> Unit = { _, _ -> },
    ) : IState<ALive2DRenderer, State> {
        SETUP_MASK,
        DRAW
    }

    companion object {

        /**
         * 実験時に1チャンネルの場合は1、RGBだけの場合は3、アルファも含める場合は4
         */
        var COLOR_CHANNEL_COUNT: Int = 4

        /**
         * 通常のフレームバッファ1枚あたりのマスク最大数
         */
        var CLIPPING_MASK_MAX_COUNT_ON_DEFAULT: Int = 36

        /**
         * フレームバッファが2枚以上ある場合のフレームバッファ1枚あたりのマスク最大数
         */
        var CLIPPING_MASK_MAX_COUNT_ON_MULTI_RENDER_TEXTURE: Int = 32
    }

}

// TODO:: expert DrawableContext.create() 然后平台实现?
class DrawableContext constructor(
    val model: Live2DModel,
    val index: Int,
) {
    val renderOrder = model.getDrawableRenderOrder(index)

    val textureIndex = model.getDrawableTextureIndex(index

    val texture: ALive2DTexture by lazy {
        ALive2DTexture.Cache.textures[model]!![model.getDrawableTextureIndex(index)]!!
    }

    val isCulling = !model.getDrawableIsDoubleSided(index)

    val blendMode: CubismBlendMode = model.getDrawableBlendMode(index)
    val isInvertedMask = model.getDrawableInvertedMask(index)

    val vertex: Vertex = Vertex(model, index)
    var isVisible = false
    var vertexPositionDidChange = false
    var opacity = 1.0f
    lateinit var baseColor: CubismTextureColor
    lateinit var multiplyColor: CubismTextureColor
    lateinit var screenColor: CubismTextureColor

    /*
        不为空说明该物件有蒙版
     */
    var clipContext: ClipContext? = null

    fun update() {
        vertex.update()
        isVisible = model.getDrawableDynamicFlagIsVisible(index)
        vertexPositionDidChange = model.getDrawableDynamicFlagVertexPositionsDidChange(index)
        opacity = model.getDrawableOpacity(index)
        baseColor = model.getModelColorWithOpacity(opacity)
        multiplyColor = model.getDrawableMultiplyColors(index)!!.let {
            CubismTextureColor(
                it[0],
                it[1],
                it[2],
                it[3],
            )
        }
        screenColor = model.getDrawableScreenColors(index)!!.let {
            CubismTextureColor(
                it[0],
                it[1],
                it[2],
                it[3],
            )
        }

    }

    class Vertex(
        val model: Live2DModel,
        val index: Int,
    ) {
        val count = model.getDrawableVertexCount(index)
        var positionsArray: FloatArray = model.getDrawableVertexPositions(index)!!
        var texCoordsArray: FloatArray = model.getDrawableVertexUVs(index)!!
        var indicesArray: ShortArray = model.getDrawableIndices(index)!!

        fun update() {
            positionsArray = model.getDrawableVertexPositions(index)!!
            texCoordsArray = model.getDrawableVertexUVs(index)!!
            indicesArray =  model.getDrawableIndices(index)!!
        }
    }

}

/*
    对应framebuffer中的一个区域中的一个通道
 */
class ClipContext(
    val maskIndexArray: IntArray,
) {
    var bufferIndex = 0
    val layoutBounds: csmRectF = csmRectF()
    var layoutChannelIndex = 0

    var allClippedDrawRect: csmRectF = csmRectF()

    val matrixForMask: CubismMatrix44 = CubismMatrix44.create()

    val matrixForDraw: CubismMatrix44 = CubismMatrix44.create()

    companion object {
        val CHANNEL_FLAGS = arrayOf(
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
    }
}

enum class CubismBlendMode {
    NORMAL,  // 通常
    ADDITIVE,  // 加算
    MULTIPLICATIVE,  // 乗算
    MASK // マスク
}

data class CubismTextureColor(
    val r: Float = 1.0f,
    val g: Float = 1.0f,
    val b: Float = 1.0f,
    val a: Float = 1.0f,
)