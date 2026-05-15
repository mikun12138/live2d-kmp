package me.mikun.live2d.ex.rendering.context

import me.mikun.live2d.ex.model.ALive2DUserModel
import me.mikun.live2d.ex.rendering.CubismBlendMode
import me.mikun.live2d.ex.rendering.Live2DColor
import me.mikun.live2d.framework.model.Live2DModel

abstract class ALive2DModelRenderContext {
    val drawableContextArray: Array<Live2DDrawableContext>

    constructor(
        userModel: ALive2DUserModel,
    ) {
        drawableContextArray = Array(userModel.model.drawableCount) {
            Live2DDrawableContext(userModel.model, it)
        }
    }

    open fun update() {
        drawableContextArray.forEach {
            it.update()
        }
    }
}

class Live2DDrawableContext(
    val model: Live2DModel,
    val index: Int,
) {
    val textureIndex = model.getDrawableTextureIndex(index)

    val drawOrder = model.getDrawableDrawOrder(index)
    val renderOrder = model.getDrawableRenderOrder(index)
    var opacity = model.getDrawableOpacity(index)
    val masks = model.getDrawableMask(index)

    val vertex: Vertex = Vertex(model, index)


    val blendMode: CubismBlendMode = model.getDrawableBlendMode(index)
    val isCulling = !model.getDrawableIsDoubleSided(index)
    val isInvertedMask = model.getDrawableInvertedMask(index)

    var isVisible = false
    var visibilityDidChange = false
    var opacityDidChange = false
    var drawOrderDidChange = false
    var renderOrderDidChange = false
    var vertexPositionDidChange = false
    var blendColorDidChange = false

    lateinit var baseColor: Live2DColor
    lateinit var multiplyColor: Live2DColor
    lateinit var screenColor: Live2DColor


    fun update() {
        isVisible = model.getDrawableDynamicFlagIsVisible(index)
        visibilityDidChange = model.getDrawableDynamicFlagVisibilityDidChange(index)
        opacityDidChange = model.getDrawableDynamicFlagOpacityDidChange(index)
        drawOrderDidChange = model.getDrawableDynamicFlagDrawOrderDidChange(index)
        renderOrderDidChange = model.getDrawableDynamicFlagRenderOrderDidChange(index)
        vertexPositionDidChange = model.getDrawableDynamicFlagVertexPositionsDidChange(index)
        blendColorDidChange = model.getDrawableDynamicFlagBlendColorDidChange(index)

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
