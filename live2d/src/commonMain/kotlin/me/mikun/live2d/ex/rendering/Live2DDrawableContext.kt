package me.mikun.live2d.ex.rendering

import me.mikun.live2d.ex.annotation.Experimental
import me.mikun.live2d.framework.model.Live2DModel

class Live2DDrawableContext(
    val model: Live2DModel,
    val index: Int,
) {
    val textureIndex = model.getDrawableTextureIndex(index)

    @Experimental("")
    val drawOrder = model.getDrawableDrawOrder(index)
    val renderOrder = model.getDrawableRenderOrder(index)
    var opacity = 1.0f
    val masks = model.getDrawableMask(index)

    val vertex: Vertex = Vertex(model, index)
    lateinit var baseColor: Live2DColor
    lateinit var multiplyColor: Live2DColor
    lateinit var screenColor: Live2DColor

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
            Live2DColor(
                it[0],
                it[1],
                it[2],
                it[3],
            )
        }
        screenColor = model.getDrawableScreenColors(index)!!.let {
            Live2DColor(
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
