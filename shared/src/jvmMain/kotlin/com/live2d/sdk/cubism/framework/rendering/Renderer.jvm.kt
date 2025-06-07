package com.live2d.sdk.cubism.framework.rendering

import com.live2d.sdk.cubism.framework.model.Live2DModel
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.GL_SCISSOR_TEST
import org.lwjgl.opengl.GL11.GL_STENCIL_TEST
import org.lwjgl.opengl.GL11.glColorMask
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glBindBuffer

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
    override fun genMasks() {
        var usingClipCount = 0
        for (clipContext in clipContext_2_drawableIndexList.keys) {

            // Calculate the rectangle that encloses the entire group of drawing objects that use this clip.
            calcClippedDrawTotalBounds(
                model,
                clipContext
            )

            if (clipContext.isUsing) {
                // Count as in use.
                usingClipCount++
            }
        }

        if (usingClipCount <= 0) {
            return
        }


        glViewport(
            0,
            0,
            clippingMaskBufferSize.x.toInt(),
            clippingMaskBufferSize.y.toInt()
        )

    }

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

        drawableContext.vertex.indices
        glDrawElement


    }

}
