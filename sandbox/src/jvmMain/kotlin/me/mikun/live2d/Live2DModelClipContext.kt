package me.mikun.live2d

import me.mikun.live2d.ex.rendering.context.ALive2DModelClipContext
import me.mikun.live2d.ex.rendering.context.ALive2DModelRenderContext

class Live2DModelClipContext(
    offscreenSurfacesCount: Int,
    renderContext: ALive2DModelRenderContext,
): ALive2DModelClipContext(
    offscreenSurfacesCount,
    renderContext
) {
    override val offscreenSurfaces: Array<Live2DOffscreenSurface> = Array(offscreenSurfacesCount) {
        Live2DOffscreenSurface().apply {
            createOffscreenSurface(
                512.0f, 512.0f
            )
        }
    }
}