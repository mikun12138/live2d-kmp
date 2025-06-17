package me.mikun.live2d.ex.rendering

import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import me.mikun.live2d.ex.annotation.Experimental
import me.mikun.live2d.framework.model.Live2DModel
import me.mikun.live2d.framework.type.csmRectF

class DrawableContext(
    val model: Live2DModel,
    val index: Int,
) {
    val textureIndex = model.getDrawableTextureIndex(index)

    @Experimental("")
    val drawOrder = model.getDrawableDrawOrder(index)
    val renderOrder = model.getDrawableRenderOrder(index)
    var opacity = 1.0f

    /*
        不为空说明该物件有蒙版
     */
    var clipContext: ClipContext? = null
    val vertex: Vertex = Vertex(model, index)
    lateinit var baseColor: CubismTextureColor
    lateinit var multiplyColor: CubismTextureColor
    lateinit var screenColor: CubismTextureColor

    val blendMode: CubismBlendMode = model.getDrawableBlendMode(index)
    val isCulling = !model.getDrawableIsDoubleSided(index)
    val isInvertedMask = model.getDrawableInvertedMask(index)

    var isVisible = false

    @Experimental("")
    var visibilityDidChange = false

    @Experimental("")
    var opacityDidChange = false

    @Experimental("")
    var drawOrderDidChange = false

    @Experimental("")
    var renderOrderDidChange = false
    var vertexPositionDidChange = false

    @Experimental("")
    var blendColorDidChange = false


    fun update() {
        isVisible = model.getDrawableDynamicFlagIsVisible(index)
        visibilityDidChange = model.getDrawableDynamicFlagVisibilityDidChange(index)
        opacityDidChange = model.getDrawableDynamicFlagOpacityDidChange(index)
        drawOrderDidChange = model.getDrawableDynamicFlagDrawOrderDidChange(index)
        renderOrderDidChange = model.getDrawableDynamicFlagRenderOrderDidChange(index)
        vertexPositionDidChange = model.getDrawableDynamicFlagVertexPositionsDidChange(index)
        blendColorDidChange = model.getDrawableDynamicFlagBlendColorDidChange(index)

        opacity = model.getDrawableOpacity(index)
        vertex.update()
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
            indicesArray = model.getDrawableIndices(index)!!
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

    val matrixForMask: CubismMatrix44 = CubismMatrix44.Companion.create()

    val matrixForDraw: CubismMatrix44 = CubismMatrix44.Companion.create()

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