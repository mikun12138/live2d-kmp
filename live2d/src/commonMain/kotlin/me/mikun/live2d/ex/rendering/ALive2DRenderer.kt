package me.mikun.live2d.ex.rendering

import me.mikun.live2d.ex.rendering.context.ALive2DModelClipContext
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext
import me.mikun.live2d.ex.rendering.context.Live2DDrawableContext
import me.mikun.live2d.framework.utils.math.CubismMatrix44
import me.mikun.live2d.framework.Live2DFramework.VERTEX_OFFSET
import me.mikun.live2d.framework.Live2DFramework.VERTEX_STEP
import me.mikun.live2d.framework.utils.math.csmRectF
import kotlin.math.max
import kotlin.math.min

abstract class ALive2DRenderer {

    abstract class PreClip(
        val pushViewportFun: (Int, Int, Int, Int, () -> Unit) -> Unit,
        val pushFrameBufferFun: (() -> Unit) -> Unit,
    ) : ALive2DRenderer() {

        fun frame(
            renderContext: ALive2DModelRenderContext,
            clipContext: ALive2DModelClipContext,
        ) {
            setupMask(renderContext, clipContext)
            draw(renderContext, clipContext)
        }

        protected fun setupMask(
            renderContext: ALive2DModelRenderContext,
            clipContext: ALive2DModelClipContext,
        ) {
            pushViewportFun(
                0, 0, 512, 512
            ) {
                pushFrameBufferFun {
                    clipContext.clipContext_2_drawableContextList.forEach { (clipContext, drawableContextList) ->
                        run {
                            clipContext.calcClippedDrawTotalBounds(
                                drawableContextList
                            ).let {
                                clipContext.createMatrixForMask(it)
                                clipContext.createMatrixForDraw(it)
                            }
                        }
                    }

                    clipContext.drawableClipContextNotNullSet.groupBy { it.bufferIndex }
                        .forEach { (bufferIndex, clipContextList) ->
                            clipContext.offscreenSurfaces[bufferIndex].let {
                                it.draw {
                                    clipContextList.forEach { drawableClipContext ->
                                        for (maskIndex in drawableClipContext.maskIndexArray) {
                                            val drawableContext =
                                                renderContext.drawableContextArray[maskIndex]

                                            if (!drawableContext.vertexPositionDidChange) continue

                                            setupMaskDraw(
                                                renderContext,
                                                drawableContext,
                                                drawableClipContext
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
            renderContext: ALive2DModelRenderContext,
            drawableContext: Live2DDrawableContext,
            drawableClipContext: ClipContext,
        )

        protected fun draw(
            renderContext: ALive2DModelRenderContext,
            clipContext: ALive2DModelClipContext,
        ) {
            val sortedDrawableContextArray = renderContext.drawableContextArray.sortedWith(
                compareBy { it.renderOrder }
            )

            sortedDrawableContextArray.forEach { drawableContext ->
                if (!drawableContext.isVisible) return@forEach

                clipContext.drawableClipContextList[drawableContext.index]?.let {
                    maskDraw(
                        renderContext,
                        clipContext,
                        drawableContext
                    )
                } ?: run {
                    simpleDraw(
                        renderContext,
                        drawableContext
                    )
                }
            }
        }

        protected abstract fun maskDraw(
            renderContext: ALive2DModelRenderContext,
            clipContext: ALive2DModelClipContext,
            drawableContext: Live2DDrawableContext,
        )

        protected abstract fun simpleDraw(
            renderContext: ALive2DModelRenderContext,
            drawableContext: Live2DDrawableContext,
        )


        fun ClipContext.createMatrixForMask(
            allClippedDrawRect: csmRectF,
        ) {
            CubismMatrix44().apply {
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
                matrixForMask.set(it)
            }
        }

        fun ClipContext.createMatrixForDraw(
            allClippedDrawRect: csmRectF,
        ) {
            CubismMatrix44().apply {
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
                matrixForDraw.set(it)
            }
        }

        fun ClipContext.calcClippedDrawTotalBounds(
            drawableContextList: List<Live2DDrawableContext>,
        ): csmRectF {
            var clippedDrawTotalMinX = Float.MAX_VALUE
            var clippedDrawTotalMinY = Float.MAX_VALUE
            var clippedDrawTotalMaxX = -Float.MAX_VALUE
            var clippedDrawTotalMaxY = -Float.MAX_VALUE

            for (drawableContext in drawableContextList) {

                var minX = Float.MAX_VALUE
                var minY = Float.MAX_VALUE
                var maxX = -Float.MAX_VALUE
                var maxY = -Float.MAX_VALUE

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

                if (minX == Float.MAX_VALUE) {
                    continue
                }

                clippedDrawTotalMinX = min(clippedDrawTotalMinX, minX)
                clippedDrawTotalMaxX = max(clippedDrawTotalMaxX, maxX)
                clippedDrawTotalMinY = min(clippedDrawTotalMinY, minY)
                clippedDrawTotalMaxY = max(clippedDrawTotalMaxY, maxY)
            }

            if (clippedDrawTotalMinX == Float.MAX_VALUE) {
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

            val matrixForMask: CubismMatrix44 = CubismMatrix44()

            val matrixForDraw: CubismMatrix44 = CubismMatrix44()

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