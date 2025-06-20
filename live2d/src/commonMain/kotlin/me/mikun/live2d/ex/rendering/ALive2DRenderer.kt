package me.mikun.live2d.ex.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import me.mikun.live2d.ex.model.ALive2DUserModel
import me.mikun.live2d.ex.model.Live2DUserModelImpl
import me.mikun.live2d.ex.rendering.ALive2DRenderer.PreClip.ClipContext.Companion.CHANNEL_FLAGS
import me.mikun.live2d.framework.Live2DFramework.VERTEX_OFFSET
import me.mikun.live2d.framework.Live2DFramework.VERTEX_STEP
import me.mikun.live2d.framework.type.csmRectF
import kotlin.math.max
import kotlin.math.min

abstract class ALive2DRenderer {
    val drawableContextArray: Array<Live2DDrawableContext>

    constructor(
        userModel: ALive2DUserModel,
    ) {
        drawableContextArray = Array(userModel.model.drawableCount) {
            Live2DDrawableContext(userModel.model, it)
        }
    }

    protected fun doFrame() {
        doUpdateData()
        doRender()
    }

    protected open fun doUpdateData() {
        drawableContextArray.forEach {
            it.update()
        }
    }

    protected abstract fun doRender()


    abstract class PreClip(
        userModel: ALive2DUserModel,
        val offscreenSurfacesCount: Int,
        val pushViewportFun: (Int, Int, Int, Int, () -> Unit) -> Unit,
        val pushFrameBufferFun: (() -> Unit) -> Unit,
    ) : ALive2DRenderer(
        userModel
    ) {
        abstract val offscreenSurfaces: Array<out ALive2DOffscreenSurface>
        val drawableClipContextList: MutableList<ClipContext?> = mutableListOf()
        val drawableClipContextNotNullSet: Set<ClipContext> by lazy {
            drawableClipContextList.filterNotNull().toSet()
        }

        init {
            drawableContextArray.forEach { drawableContext ->
                val drawableMask = drawableContext.masks
                drawableClipContextList.add(
                    if (drawableMask.isEmpty())
                        null
                    else
                        drawableClipContextList.find {
                            it?.maskIndexArray?.size == drawableMask.size
                                    && it.maskIndexArray.all { drawableMask.contains(it) }
                        } ?: ClipContext(drawableMask)
                )
            }
            setupLayoutBounds(drawableClipContextNotNullSet.size)
        }

        private fun setupLayoutBounds(clipContextCount: Int) {

            val result = Array(offscreenSurfacesCount) {
                IntArray(4)
            }

            val div0 = clipContextCount / offscreenSurfacesCount
            val mod0 = clipContextCount % offscreenSurfacesCount

            repeat(offscreenSurfacesCount) { loop0 ->
                val count0 = div0 + if (loop0 < mod0) 1 else 0

                val div1 = count0 / 4
                val mod1 = count0 % 4

                repeat(4) { loop1 ->
                    val count1 = div1 + if (loop1 < mod1) 1 else 0
                    result[loop0][loop1] = count1
                }
            }

            val iterator = drawableClipContextNotNullSet.iterator()

            result.forEachIndexed { renderTextureIndex, texture ->
                texture.forEachIndexed { channelIndex, count ->
                    when (count) {
                        0 -> {}
                        1 -> {
                            val cc: ClipContext = iterator.next()
                            cc.colorChannel = CHANNEL_FLAGS[channelIndex]
                            cc.layoutBounds = csmRectF(
                                x = 0.0f,
                                y = 0.0f,
                                width = 1.0f,
                                height = 1.0f
                            )

                            cc.bufferIndex = renderTextureIndex
                        }

                        2 -> {
                            for (i in 0..<2) {
                                val xpos = i % 2

                                val cc: ClipContext = iterator.next()
                                cc.colorChannel = CHANNEL_FLAGS[channelIndex]
                                cc.layoutBounds = csmRectF(
                                    x = xpos * 0.5f,
                                    y = 0.0f,
                                    width = 0.5f,
                                    height = 1.0f
                                )

                                cc.bufferIndex = renderTextureIndex
                            }
                        }

                        in 3..4 -> {
                            for (i in 0..<count) {
                                val xpos = i % 2
                                val ypos = i / 2

                                val cc: ClipContext = iterator.next()
                                cc.colorChannel = CHANNEL_FLAGS[channelIndex]
                                cc.layoutBounds = csmRectF(
                                    x = xpos * 0.5f,
                                    y = ypos * 0.5f,
                                    width = 0.5f,
                                    height = 0.5f

                                )
                                cc.bufferIndex = renderTextureIndex
                            }
                        }

                        in 5..9 -> {
                            for (i in 0..<count) {
                                val xpos = i % 3
                                val ypos = i / 3

                                val cc: ClipContext = iterator.next()

                                cc.colorChannel = CHANNEL_FLAGS[channelIndex]
                                cc.layoutBounds = csmRectF(
                                    x = xpos / 3.0f,
                                    y = ypos / 3.0f,
                                    width = 1.0f / 3.0f,
                                    height = 1.0f / 3.0f
                                )

                                cc.bufferIndex = renderTextureIndex
                            }
                        }

                        else -> error("Incorrect clipContextCount: $count")
                    }
                }
            }
        }


        override fun doRender() {
            setupMask()
            draw()
        }

        /*
            lazy cache
        */
        private val clipContext_2_drawableContextList: Map<ClipContext, List<Live2DDrawableContext>> by lazy {
            drawableClipContextNotNullSet.associateWith { clipContext -> drawableContextArray.filter { drawableClipContextList[it.index] === clipContext } }
        }

        protected fun setupMask() {
            pushViewportFun(
                0, 0, 512, 512
            ) {
                pushFrameBufferFun {
                    clipContext_2_drawableContextList.forEach { (clipContext, drawableContextList) ->
                        run {
                            clipContext.calcClippedDrawTotalBounds(
                                drawableContextList
                            ).let {
                                clipContext.createMatrixForMask(it)
                                clipContext.createMatrixForDraw(it)
                            }
                        }
                    }

                    drawableClipContextNotNullSet.groupBy { it.bufferIndex }
                        .forEach { (bufferIndex, clipContextList) ->
                            offscreenSurfaces[bufferIndex].let {
                                it.draw {
                                    clipContextList.forEach { clipContext ->
                                        for (maskIndex in clipContext.maskIndexArray) {
                                            val drawableContext = drawableContextArray[maskIndex]

                                            if (!drawableContext.vertexPositionDidChange) continue

                                            setupMaskDraw(
                                                drawableContext,
                                                clipContext
                                            )
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }

        protected abstract fun setupMaskDraw(
            drawableContext: Live2DDrawableContext,
            clipContext: ClipContext,
        )

        protected fun draw() {
            val sortedDrawableContextArray = drawableContextArray.sortedWith(
                compareBy { it.renderOrder }
            )

            sortedDrawableContextArray.forEach { drawableContext ->
                if (!drawableContext.isVisible) return@forEach

                drawableClipContextList[drawableContext.index]?.let {
                    maskDraw(
                        drawableContext
                    )
                } ?: run {
                    simpleDraw(
                        drawableContext
                    )
                }
            }
        }

        protected abstract fun maskDraw(
            drawableContext: Live2DDrawableContext,
        )

        protected abstract fun simpleDraw(
            drawableContext: Live2DDrawableContext,
        )


        fun ClipContext.createMatrixForMask(
            allClippedDrawRect: csmRectF,
        ) {
            CubismMatrix44.create().apply {
                loadIdentity()
                // Layout0..1を、-1..1に変換
                translateRelative(-1.0f, -1.0f)
                scaleRelative(2.0f, 2.0f)

                // view to Layout0..1
                translateRelative(
                    layoutBounds.x,
                    layoutBounds.y
                )
                scaleRelative(
                    layoutBounds.width / allClippedDrawRect.width,
                    layoutBounds.height / allClippedDrawRect.height
                )
                translateRelative(
                    -allClippedDrawRect.x,
                    -allClippedDrawRect.y
                )
            }.also {
                matrixForMask.setMatrix(it)
            }
        }

        fun ClipContext.createMatrixForDraw(
            allClippedDrawRect: csmRectF,
        ) {
            CubismMatrix44.create().apply {
                loadIdentity()

                translateRelative(
                    layoutBounds.x,
                    layoutBounds.y
                )
                scaleRelative(
                    layoutBounds.width / allClippedDrawRect.width,
                    layoutBounds.height / allClippedDrawRect.height
                )
                translateRelative(
                    -allClippedDrawRect.x,
                    -allClippedDrawRect.y
                )
            }.also {
                matrixForDraw.setMatrix(it)
            }
        }

        fun ClipContext.calcClippedDrawTotalBounds(
            drawableContextList: List<Live2DDrawableContext>,
        ): csmRectF {
            var clippedDrawTotalMinX = Float.Companion.MAX_VALUE
            var clippedDrawTotalMinY = Float.Companion.MAX_VALUE
            var clippedDrawTotalMaxX = -Float.Companion.MAX_VALUE
            var clippedDrawTotalMaxY = -Float.Companion.MAX_VALUE

            for (drawableContext in drawableContextList) {

                var minX = Float.Companion.MAX_VALUE
                var minY = Float.Companion.MAX_VALUE
                var maxX = -Float.Companion.MAX_VALUE
                var maxY = -Float.Companion.MAX_VALUE

                val loop = drawableContext.vertex.count * VERTEX_STEP
                var pi = VERTEX_OFFSET
                while (pi < loop) {
                    val x = drawableContext.vertex.positionsArray[pi]
                    val y = drawableContext.vertex.positionsArray[pi + 1]
                    minX = min(minX, x)
                    maxX = max(maxX, x)
                    minY = min(minY, y)
                    maxY = max(maxY, y)
                    pi += VERTEX_STEP
                }

                if (minX == Float.Companion.MAX_VALUE) {
                    continue
                }

                clippedDrawTotalMinX = min(clippedDrawTotalMinX, minX)
                clippedDrawTotalMaxX = max(clippedDrawTotalMaxX, maxX)
                clippedDrawTotalMinY = min(clippedDrawTotalMinY, minY)
                clippedDrawTotalMaxY = max(clippedDrawTotalMaxY, maxY)
            }

            if (clippedDrawTotalMinX == Float.Companion.MAX_VALUE) {
                return error("")
            } else {
                val w = clippedDrawTotalMaxX - clippedDrawTotalMinX
                val h = clippedDrawTotalMaxY - clippedDrawTotalMinY
                return csmRectF(
                    clippedDrawTotalMinX,
                    clippedDrawTotalMinY,
                    w,
                    h
                )
            }
        }


        /*
            对应framebuffer中的一个区域中的一个通道
         */
        class ClipContext(
            val maskIndexArray: IntArray,
        ) {
            var bufferIndex = -1
            lateinit var layoutBounds: csmRectF
            lateinit var colorChannel: Live2DColor

            val matrixForMask: CubismMatrix44 = CubismMatrix44.Companion.create()

            val matrixForDraw: CubismMatrix44 = CubismMatrix44.Companion.create()

            companion object {
                val CHANNEL_FLAGS = arrayOf(
                    Live2DColor(
                        r = 1.0f,
                        g = 0.0f,
                        b = 0.0f,
                        a = 0.0f,
                    ),
                    Live2DColor(
                        r = 0.0f,
                        g = 1.0f,
                        b = 0.0f,
                        a = 0.0f,
                    ),
                    Live2DColor(
                        r = 0.0f,
                        g = 0.0f,
                        b = 1.0f,
                        a = 0.0f,
                    ),
                    Live2DColor(
                        r = 0.0f,
                        g = 0.0f,
                        b = 0.0f,
                        a = 1.0f,
                    )
                )
            }
        }
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

enum class CubismBlendMode {
    NORMAL,  // 通常
    ADDITIVE,  // 加算
    MULTIPLICATIVE,  // 乗算
    MASK // マスク
}

data class Live2DColor(
    val r: Float = 1.0f,
    val g: Float = 1.0f,
    val b: Float = 1.0f,
    val a: Float = 1.0f,
)