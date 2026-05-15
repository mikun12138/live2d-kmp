/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.model

import me.mikun.live2d.core.CubismDrawableFlag.ConstantFlag
import me.mikun.live2d.core.CubismDrawableFlag.DynamicFlag
import me.mikun.live2d.core.CubismDrawableView
import me.mikun.live2d.core.CubismModel
import me.mikun.live2d.core.CubismParameterView
import me.mikun.live2d.core.CubismPartView
import me.mikun.live2d.ex.rendering.CubismBlendMode
import me.mikun.live2d.ex.rendering.Live2DColor
import me.mikun.live2d.framework.id.Live2DId
import kotlin.experimental.and

class Live2DModel {
    private val model: CubismModel

    val parameterViews: Array<CubismParameterView>
    val partViews: Array<CubismPartView>
    val drawableViews: Array<CubismDrawableView>

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

    internal constructor(model: CubismModel) {
        this.model = model

        this.parameterViews = Array(model.parameters.count) {
            CubismParameterView(it, model.parameters)
        }
        this.partViews = Array(model.parts.count) {
            CubismPartView(it, model.parts)
        }
        this.drawableViews = Array(model.drawables.count) {
            CubismDrawableView(it, model.drawables)
        }
    }

    fun update() {
        model.update()
        model.resetDrawableDynamicFlags()
    }

    /* ---------- *
     * PARAMETERS *
     * ---------- */

    private fun createParameter(parameterId: Live2DId): Int {
        val parameterIndex = parameterViews.size + notExistParameterId2Index.size
        notExistParameterId2Index[parameterId] = parameterIndex
        notExitParameterIndex2Value[parameterIndex] = 0.0f
        return parameterIndex
    }

    fun getParameterIndexOrCreate(parameterId: Live2DId): Int {
        parameterViews.firstOrNull { it.id == parameterId.value  }?.let {
            return it.index
        }

        notExistParameterId2Index[parameterId]?.let {
            return it
        }
        createParameter(parameterId).let {
            return it
        }
    }

    val parameterCount: Int
        get() = parameterViews.size

    private fun getParameterMinimumValue(parameterIndex: Int): Float {
        return parameterViews[parameterIndex].minimumValue
    }

    fun getParameterMinimumValue(parameterId: Live2DId): Float {
        return getParameterMinimumValue(
            getParameterIndexOrCreate(parameterId)
        )
    }

    private fun getParameterMaximumValue(parameterIndex: Int): Float {
        return parameterViews[parameterIndex].maximumValue
    }

    fun getParameterMaximumValue(parameterId: Live2DId): Float {
        return getParameterMaximumValue(
            getParameterIndexOrCreate(parameterId)
        )
    }

    private fun getParameterDefaultValue(parameterIndex: Int): Float {
        return parameterViews[parameterIndex].defaultValue
    }

    fun getParameterDefaultValue(parameterId: Live2DId): Float {
        return getParameterDefaultValue(
            getParameterIndexOrCreate(parameterId)
        )
    }

    // TODO:: make it private
    fun getParameterValue(parameterIndex: Int): Float {
        notExitParameterIndex2Value[parameterIndex]?.let {
            return it
        }
        return parameterViews[parameterIndex].value
    }

    fun getParameterValue(parameterId: Live2DId): Float {
        return getParameterValue(
            getParameterIndexOrCreate(parameterId)
        )
    }

    fun setParameterValue(parameterIndex: Int, value: Float, weight: Float = 1.0f) {
        notExitParameterIndex2Value.computeIfPresent(parameterIndex) { k, v ->
            (v * (1.0f - weight)) + (value * weight)
        }?.run {
            return
        }

        val parameter = parameterViews[parameterIndex]
        parameter.value = (parameter.value * (1.0f - weight)) + (value * weight)
    }

    fun setParameterValue(parameterId: Live2DId, value: Float, weight: Float = 1.0f) {
        val index = getParameterIndexOrCreate(parameterId)
        setParameterValue(index, value, weight)
    }

    fun getParameterId(parameterIndex: Int): String {
        return parameterViews[parameterIndex].id!!
    }

    /* ----- *
     * PARTS *
     * ----- */

    private fun createPart(partId: Live2DId): Int {
        val parameterIndex = partViews.size + notExistPartId2Index.size
        notExistPartId2Index[partId] = parameterIndex
        notExitPartIndex2Opacities[parameterIndex] = 0.0f
        return parameterIndex
    }

    fun getPartIndexOrCreate(partId: Live2DId): Int {
        partViews.firstOrNull { it.id == partId.value  }?.let {
            return it.index
        }

        notExistPartId2Index[partId]?.let {
            return it
        }

        createPart(partId).let {
            return it
        }
    }

    val partCount: Int
        get() = partViews.size

    private fun setPartOpacity(partIndex: Int, opacity: Float) {
        notExitPartIndex2Opacities.computeIfPresent(partIndex) { k, v ->
            opacity
        }?.run {
            return
        }

        partViews[partIndex].opacity = opacity
    }

    fun setPartOpacity(partId: Live2DId, opacity: Float) {
        setPartOpacity(
            getPartIndexOrCreate(partId),
            opacity
        )
    }


    private fun getPartOpacity(partIndex: Int): Float {
        notExitPartIndex2Opacities[partIndex]?.let {
            return it
        }

        return partViews[partIndex].opacity
    }

    fun getPartOpacity(partId: Live2DId): Float {
        return getPartOpacity(
            getPartIndexOrCreate(partId)
        )
    }


    /* --------- *
     * DRAWABLES *
     * --------- */

    val drawableCount: Int
        get() = drawableViews.size

    /**
     * Return true if the logical product of flag and mask matches the mask.
     *
     * @return Return true if the logical product of flag and mask matches the mask.
     */
    private fun isBitSet(flag: Byte, mask: Byte): Boolean {
        return (flag and mask) == mask
    }

    private fun getDrawableConstantFlag(
        drawableIndex: Int,
        flag: ConstantFlag,
    ): Boolean {
        val constantFlag: Byte = drawableViews[drawableIndex].constantFlag
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
        val dynamicFlag: Byte = drawableViews[drawableIndex].dynamicFlag
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
        return drawableViews[drawableIndex].textureIndex
    }

    fun getDrawableDrawOrder(drawableIndex: Int): Int {
        return drawableViews[drawableIndex].drawOrder
    }

    fun getDrawableRenderOrder(drawableIndex: Int): Int {
        return drawableViews[drawableIndex].renderOrder
    }

    fun getDrawableOpacity(drawableIndex: Int): Float {
        return drawableViews[drawableIndex].opacity
    }

    fun getDrawableMask(drawableIndex: Int): IntArray {
        return drawableViews[drawableIndex].masks
    }

    fun getDrawableVertexCount(drawableIndex: Int): Int {
        return drawableViews[drawableIndex].vertexCount
    }

    fun getDrawableVertexPositions(drawableIndex: Int): FloatArray {
        return drawableViews[drawableIndex].vertexPositions
    }

    fun getDrawableVertexUVs(drawableIndex: Int): FloatArray {
        return drawableViews[drawableIndex].vertexUvs
    }

    fun getDrawableIndices(drawableIndex: Int): ShortArray {
        return drawableViews[drawableIndex].indices
    }

    fun getDrawableMultiplyColors(drawableIndex: Int): FloatArray {
        return drawableViews[drawableIndex].multiplyColors
    }

    fun getDrawableScreenColors(drawableIndex: Int): FloatArray {
        return drawableViews[drawableIndex].screenColors
    }

    fun getDrawableParentPartIndex(drawableIndex: Int): Int {
        return drawableViews[drawableIndex].parentPartIndex
    }

    fun getModelColorWithOpacity(opacity: Float): Live2DColor {
        return Live2DColor(
            modelColor.r,
            modelColor.g,
            modelColor.b,
            modelColor.a * opacity,
        )
    }

    /**
     * List of IDs for non-existent parameters
     */
    private val notExistParameterId2Index = mutableMapOf<Live2DId, Int>()
    private val notExitParameterIndex2Value = mutableMapOf<Int, Float>()

    private val notExistPartId2Index = mutableMapOf<Live2DId, Int>()
    private val notExitPartIndex2Opacities = mutableMapOf<Int, Float>()

}

