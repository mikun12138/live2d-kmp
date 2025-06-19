/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.model

import me.mikun.live2d.core.CubismDrawableFlag.ConstantFlag
import me.mikun.live2d.core.CubismDrawableFlag.DynamicFlag
import me.mikun.live2d.core.CubismModel
import me.mikun.live2d.core.CubismParameterView
import me.mikun.live2d.core.CubismPartView
import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.ex.rendering.CubismBlendMode
import me.mikun.live2d.ex.rendering.Live2DColor
import kotlin.collections.mutableListOf
import kotlin.experimental.and

class Live2DModel {
    private val model: CubismModel

    constructor(model: CubismModel) {
        this.model = model

        val multiplyColor = Live2DColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
        )
        val screenColor = Live2DColor(
            0.0f,
            0.0f,
            0.0f,
            1.0f
        )

        model.drawableViews.let { drawableViews ->
            userDrawableMultiplyColors = List(drawableViews.size) {
                DrawableColorData(multiplyColor)
            }
            userDrawableScreenColors = List(drawableViews.size) {
                DrawableColorData(screenColor)
            }
            userDrawableCullings = List(drawableViews.size) {
                DrawableCullingData()
            }
        }
    }

    data class DrawableColorData(
        var color: Live2DColor,
        var isOverwritten: Boolean = false,
    )

    data class PartColorData(
        var color: Live2DColor,
        var isOverwritten: Boolean = false,
    )

    data class DrawableCullingData(
        var isCulling: Boolean = false,
        var isOverWritten: Boolean = false,
    )

    /**
     * Update model's parameters.
     */
    fun update() {
        model.update()
        model.resetDrawableDynamicFlags()
    }


    /* ---------- *
     * PARAMETERS *
     * ---------- */

    fun getParameterIndex(parameterId: Live2DId): Int {
        val parameterView: CubismParameterView? = model.findParameterView(parameterId.value)
        if (parameterView != null) {
            return parameterView.index
        }

        notExistParameterIds[parameterId]?.let {
            return it
        }

        val parameterIndex = model.parameterViews.size + notExistParameterIds.size
        notExistParameterIds.put(parameterId, parameterIndex)
        notExistParameterIndices.add(parameterIndex)
        notExistParameterValues.add(0.0f)

        return parameterIndex
    }

    val parameterCount: Int
        get() = model.parameterViews.size

    fun getParameterMinimumValue(parameterIndex: Int): Float {
        return model.parameterViews[parameterIndex].minimumValue
    }

    fun getParameterMaximumValue(parameterIndex: Int): Float {
        return model.parameterViews[parameterIndex].maximumValue
    }

    fun getParameterDefaultValue(parameterIndex: Int): Float {
        return model.parameterViews[parameterIndex].defaultValue
    }

    fun getParameterValue(parameterId: Live2DId): Float {
        val parameterIndex = getParameterIndex(parameterId)
        return getParameterValue(parameterIndex)
    }

    fun getParameterValue(parameterIndex: Int): Float {
        if (notExistParameterIndices.contains(parameterIndex)) {
            val index = notExistParameterIndices.indexOf(parameterIndex)
            val value = notExistParameterValues[index]
            return value
        }

        return model.parameterViews[parameterIndex].value
    }

    fun setParameterValue(parameterId: Live2DId, value: Float, weight: Float = 1.0f) {
        val index = getParameterIndex(parameterId)
        setParameterValue(index, value, weight)
    }

    fun setParameterValue(parameterIndex: Int, value: Float, weight: Float = 1.0f) {
        if (notExistParameterIndices.contains(parameterIndex)) {
            val index = notExistParameterIndices.indexOf(parameterIndex)
            val parameterValue = notExistParameterValues[index]
            notExistParameterValues[index] = (parameterValue * (1.0f - weight)) + (value * weight)
            return
        }

        var value1 = value
        run {
            val parameter = model.parameterViews[parameterIndex]
            value1 = value1.coerceIn(parameter.minimumValue, parameter.maximumValue)

            // 此处重写了 set
            parameter.value = (parameter.value * (1.0f - weight)) + (value1 * weight)
        }
    }

    /* ----- *
     * PARTS *
     * ----- */

    fun getPartIndex(partId: Live2DId): Int {
        val partView: CubismPartView? = model.findPartView(partId.value)
        if (partView != null) {
            return partView.index
        }

        notExistPartIds[partId]?.let {
            return it
        }

        val partIndex = model.partViews.size + notExistPartIds.size
        notExistPartIds.put(partId, partIndex)
        notExistPartIndices.add(partIndex)

        notExistPartOpacities.add(0.0f)

        return partIndex
    }

    val partCount: Int
        get() = model.partViews.size

    fun setPartOpacity(partId: Live2DId, opacity: Float) {
        setPartOpacity(
            getPartIndex(partId),
            opacity
        )
    }

    private fun setPartOpacity(partIndex: Int, opacity: Float) {
        if (notExistPartIndices.contains(partIndex)) {
            val index = notExistPartIndices.indexOf(partIndex)
            notExistPartOpacities[index] = opacity
            return
        }

        model.partViews[partIndex].opacity = opacity
    }

    fun getPartOpacity(partId: Live2DId): Float {
        return getPartOpacity(
            getPartIndex(partId)
        )
    }

    private fun getPartOpacity(partIndex: Int): Float {
        if (notExistPartIndices.contains(partIndex)) {
            val index = notExistPartIndices.indexOf(partIndex)
            return notExistPartOpacities[index]
        }

        return model.partViews[partIndex].opacity
    }

    /* --------- *
     * DRAWABLES *
     * --------- */

    val drawableCount: Int
        get() = model.drawableViews.size

    private fun getDrawableConstantFlag(
        drawableIndex: Int,
        flag: ConstantFlag,
    ): Boolean {
        val constantFlag: Byte = model.drawableViews[drawableIndex].constantFlag
        return isBitSet(constantFlag, flag.value)
    }

    fun getDrawableBlendMode(drawableIndex: Int): CubismBlendMode {
        return if (getDrawableConstantFlag(drawableIndex, ConstantFlag.BLEND_ADDITIVE))
            CubismBlendMode.ADDITIVE
        else
            if (getDrawableConstantFlag(drawableIndex, ConstantFlag.BLEND_MULTIPLICATIVE))
                CubismBlendMode.MULTIPLICATIVE
            else
                CubismBlendMode.NORMAL
    }

    fun getDrawableIsDoubleSided(drawableIndex: Int): Boolean {
        return getDrawableConstantFlag(drawableIndex, ConstantFlag.IS_DOUBLE_SIDED)
    }

    fun getDrawableInvertedMask(drawableIndex: Int): Boolean {
        return getDrawableConstantFlag(drawableIndex, ConstantFlag.IS_INVERTED_MASK)
    }

    private fun getDrawableDynamicFlag(drawableIndex: Int, flag: DynamicFlag): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, flag.value)
    }

    fun getDrawableDynamicFlagIsVisible(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.IS_VISIBLE)
    }

    fun getDrawableDynamicFlagVisibilityDidChange(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.VISIBILITY_DID_CHANGE)
    }

    fun getDrawableDynamicFlagOpacityDidChange(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.OPACITY_DID_CHANGE)
    }

    fun getDrawableDynamicFlagDrawOrderDidChange(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.DRAW_ORDER_DID_CHANGE)
    }

    fun getDrawableDynamicFlagRenderOrderDidChange(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.RENDER_ORDER_DID_CHANGE)
    }

    fun getDrawableDynamicFlagVertexPositionsDidChange(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.VERTEX_POSITIONS_DID_CHANGE)
    }

    fun getDrawableDynamicFlagBlendColorDidChange(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.BLEND_COLOR_DID_CHANGE)
    }


    fun getDrawableTextureIndex(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].textureIndex
    }

    fun getDrawableDrawOrder(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].drawOrder
    }

    fun getDrawableRenderOrder(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].renderOrder
    }

    fun getDrawableOpacity(drawableIndex: Int): Float {
        return model.drawableViews[drawableIndex].opacity
    }

    fun getDrawableMask(drawableIndex: Int): IntArray {
        return model.drawableViews[drawableIndex].masks
    }

    fun getDrawableVertexCount(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].vertexCount
    }

    fun getDrawableVertexPositions(drawableIndex: Int): FloatArray {
        return model.drawableViews[drawableIndex].vertexPositions
    }

    fun getDrawableVertexUVs(drawableIndex: Int): FloatArray {
        return model.drawableViews[drawableIndex].vertexUvs
    }

    fun getDrawableIndices(drawableIndex: Int): ShortArray {
        return model.drawableViews[drawableIndex].indices
    }

    fun getDrawableMultiplyColors(drawableIndex: Int): FloatArray {
        return model.drawableViews[drawableIndex].multiplyColors
    }

    fun getDrawableScreenColors(drawableIndex: Int): FloatArray {
        return model.drawableViews[drawableIndex].screenColors
    }

    fun getDrawableParentPartIndex(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].parentPartIndex
    }


    /* ------ *
     * CANVAS *
     * ------ */

    val canvasWidthPixel: Float
        get() {
            return model.canvasInfo.sizeInPixels[0]
        }

    val canvasHeightPixel: Float
        get() {
            return model.canvasInfo.sizeInPixels[1]
        }

    val canvasWidth: Float
        get() {
            return model.canvasInfo.sizeInPixels[0] / model.canvasInfo.pixelsPerUnit
        }

    val canvasHeight: Float
        get() {
            return model.canvasInfo.sizeInPixels[1] / model.canvasInfo.pixelsPerUnit
        }

    val pixelPerUnit: Float
        get() {
            return model.canvasInfo.pixelsPerUnit
        }

    private val modelColor = Live2DColor()

    fun getModelColorWithOpacity(opacity: Float): Live2DColor {
        return Live2DColor(
            modelColor.r,
            modelColor.g,
            modelColor.b,
            modelColor.a * opacity,
        ).apply {

//            if (this@CubismRenderer.isPremultipliedAlpha) {
//                this.r *= this.a
//                this.g *= this.a
//                this.b *= this.a
//            }
        }
    }


    // TODO:: remove 不知道为什么似乎不影响更新
    fun loadParameters() {
        var parameterCount = this.parameterCount
        val savedParameterCount = savedParameters.size

        if (parameterCount > savedParameterCount) {
            parameterCount = savedParameterCount
        }

        for (i in 0..<parameterCount) {
            model.parameterViews[i].value = savedParameters[i]
        }
    }

    // TODO:: remove 不知道为什么似乎不影响更新
    fun saveParameters() {
        val parameterCount = this.parameterCount

        if (savedParameters.size < parameterCount) {
            savedParameters = FloatArray(parameterCount)
        }
        for (i in 0..<parameterCount) {
            savedParameters[i] = model.parameterViews[i].value
        }
    }

    /**
     * Return true if the logical product of flag and mask matches the mask.
     *
     * @return Return true if the logical product of flag and mask matches the mask.
     */
    private fun isBitSet(flag: Byte, mask: Byte): Boolean {
        return (flag and mask) == mask
    }


    /**
     * List of IDs for non-existent parameters
     */
    private val notExistParameterIds: MutableMap<Live2DId, Int> = HashMap()
    private val notExistParameterIndices = mutableListOf<Int>()
    private var notExistParameterValues = mutableListOf<Float>()

    private val notExistPartIds: MutableMap<Live2DId, Int> = HashMap()
    private val notExistPartIndices = mutableListOf<Int>()
    private var notExistPartOpacities = mutableListOf<Float>()

    /**
     * Saved parameters
     */
    private var savedParameters = FloatArray(1)

    private var userDrawableMultiplyColors: List<DrawableColorData>
    private var userDrawableScreenColors: List<DrawableColorData>
    private var userDrawableCullings: List<DrawableCullingData>
}

