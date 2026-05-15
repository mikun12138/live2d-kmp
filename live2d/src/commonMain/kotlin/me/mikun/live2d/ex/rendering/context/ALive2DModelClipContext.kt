package me.mikun.live2d.ex.rendering.context

import me.mikun.live2d.ex.rendering.ALive2DOffscreenSurface
import me.mikun.live2d.ex.rendering.ALive2DRenderer.PreClip.ClipContext
import me.mikun.live2d.ex.rendering.ALive2DRenderer.PreClip.ClipContext.Companion.CHANNEL_FLAGS
import me.mikun.live2d.framework.utils.math.csmRectF

abstract class ALive2DModelClipContext(
    val offscreenSurfacesCount: Int,
    renderContext: ALive2DModelRenderContext,
) {
    abstract val offscreenSurfaces: Array<out ALive2DOffscreenSurface>
    val drawableClipContextList: MutableList<ClipContext?> = mutableListOf()
    val drawableClipContextNotNullSet: Set<ClipContext> by lazy {
        drawableClipContextList.filterNotNull().toSet()
    }

    var clipContext_2_drawableContextList: Map<ClipContext, List<Live2DDrawableContext>>

    init {
        renderContext.drawableContextArray.forEach { drawableContext ->
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
        clipContext_2_drawableContextList =
            drawableClipContextNotNullSet.associateWith { clipContext -> renderContext.drawableContextArray.filter { drawableClipContextList[it.index] === clipContext } }
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
}