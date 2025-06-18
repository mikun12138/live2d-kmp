package me.mikun.live2d.ex.rendering

abstract class ALive2DOffscreenSurface {

    fun draw(
        block: () -> Unit,
    ) {
        beginDraw().takeIf { it }?.let {
            clear(
                1.0f,
                1.0f,
                1.0f,
                1.0f
            )
            block()

            endDraw()
        }
    }

    /**
     * return false if failed to bind framebuffer
     */
    protected abstract fun beginDraw(): Boolean
    protected abstract fun endDraw()
    protected abstract fun clear(r: Float, g: Float, b: Float, a: Float)

    abstract fun createOffscreenSurface(width: Float, height: Float)

}