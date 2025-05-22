/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.core.CubismDrawableFlag.ConstantFlag.BLEND_ADDITIVE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.ConstantFlag.BLEND_MULTIPLICATIVE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.ConstantFlag.IS_DOUBLE_SIDED
import com.live2d.sdk.cubism.core.CubismDrawableFlag.ConstantFlag.IS_INVERTED_MASK
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.BLEND_COLOR_DID_CHANGE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.DRAW_ORDER_DID_CHANGE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.IS_VISIBLE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.OPACITY_DID_CHANGE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.RENDER_ORDER_DID_CHANGE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.VERTEX_POSITIONS_DID_CHANGE
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag.VISIBILITY_DID_CHANGE
import com.live2d.sdk.cubism.core.CubismDrawableView
import com.live2d.sdk.cubism.core.CubismParameterView
import com.live2d.sdk.cubism.core.CubismPartView
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer

/**
 * Model class created from Mclapoc data.
 */
class CubismModel
/**
 * Constructor
 *
 * @param model model instance
 */ internal constructor(
    /**
     * model
     */
    val model: com.live2d.sdk.cubism.core.CubismModel
) {
    /**
     * Inner class for handling texture colors in RGBA
     */
    data class DrawableColorData(
        var color: CubismRenderer.CubismTextureColor,
        var isOverwritten: Boolean = false
    )

    data class PartColorData(
        var color: CubismRenderer.CubismTextureColor,
        var isOverwritten: Boolean = false
    )

    data class DrawableCullingData(
        var isCulling: Boolean = false,
        var isOverWritten: Boolean = false
    )

    /**
     * Update model's parameters.
     */
    fun update() {
        model.update()
        model.resetDrawableDynamicFlags()
    }

    val canvasWidthPixel: Float
        get() {
            return model.canvasInfo.sizeInPixels[0]
        }

    val canvasHeightPixel: Float
        get() {
            return model.canvasInfo.sizeInPixels[1]
        }

    val pixelPerUnit: Float
        get() {
            return model.canvasInfo.pixelsPerUnit
        }

    val canvasWidth: Float
        get() {
            return model.canvasInfo.sizeInPixels[0] / model.canvasInfo.pixelsPerUnit
        }

    val canvasHeight: Float
        get() {
            return model.canvasInfo.sizeInPixels[1] / model.canvasInfo.pixelsPerUnit
        }

    fun getPartIndex(partId: CubismId): Int {
        val partView: CubismPartView? = model.findPartView(partId.string)
        if (partView != null) {
            return partView.index
        }

        // If the part does not exist in the model, it searches for it in the non-existent part ID list and returns its index.
        if (notExistPartIds.containsKey(partId)) {
            return notExistPartIds.get(partId)!!
        }

        // If the part does not exist in the non-existent part ID list, add newly the element.
        val partIndex = partViews.size + notExistPartIds.size
        notExistPartIds.put(partId, partIndex)
        notExistPartIndices.add(partIndex)

        val tmp = FloatArray(notExistPartIndices.size)
        System.arraycopy(notExistPartOpacities, 0, tmp, 0, notExistPartIndices.size - 1)
        tmp[notExistPartIndices.size - 1] = 0.0f
        notExistPartOpacities = FloatArray(notExistPartIndices.size)
        System.arraycopy(tmp, 0, notExistPartOpacities, 0, notExistPartIndices.size)

        return partIndex
    }

    val partCount: Int
        get() = model.partViews.size

    fun setPartOpacity(partId: CubismId, opacity: Float) {
        // Speeding up the process, this can get partIndex. However, it is not necessary when setting externally because it is not frequently called.
        val index = getPartIndex(partId)

        if (index < 0) {
            // Skip processes because there is no part.
            return
        }

        setPartOpacity(index, opacity)
    }

    fun setPartOpacity(partIndex: Int, opacity: Float) {
        if (notExistPartIndices.contains(partIndex)) {
            val index = notExistPartIndices.indexOf(partIndex)
            notExistPartOpacities[index] = opacity
            return
        }

        // Detect whether partIndex is not out of bounds index
        assert(0 <= partIndex && partIndex < this.partCount)

        partViews[partIndex].opacity = opacity
    }

    fun getPartOpacity(partId: CubismId): Float {
        // Speeding up the process, this can get partIndex. However, it is not necessary when setting externally because it is not frequently called.
        val index = getPartIndex(partId)

        if (index < 0) {
            // Skip processes because there is no part
            return 0f
        }

        return getPartOpacity(index)
    }

    fun getPartOpacity(partIndex: Int): Float {
        if (notExistPartIndices.contains(partIndex)) {
            // If the part ID does not exist in the model, returns the opacity from non-existence parts list.
            val index = notExistPartIndices.indexOf(partIndex)
            return notExistPartOpacities[index]
        }

        // Detect whether partIndex is not out of bounds index
        assert(0 <= partIndex && partIndex < this.partCount)

        return partViews[partIndex].opacity
    }

    /* ---------- *
     * PARAMETERS *
     * ---------- */

    fun getParameterIndex(parameterId: CubismId): Int {
        val parameterView: CubismParameterView? = model.findParameterView(parameterId.string)
        if (parameterView != null) {
            return parameterView.index
        }

        // If the parameter does not exist in the model, it searches for it in the non-existent parameter ID list and returns its index.
        if (notExistParameterIds.containsKey(parameterId)) {
            val index = checkNotNull(notExistParameterIds.get(parameterId))
            return index
        }

        // If the parameter does not exist in the non-existent parameter ID list, add newly the element.
        val parameterIndex = parameterViews.size + notExistParameterIds.size
        notExistParameterIds.put(parameterId, parameterIndex)
        notExistParameterIndices.add(parameterIndex)

        val tmp = FloatArray(notExistParameterIndices.size)
        System.arraycopy(notExistParameterValues, 0, tmp, 0, notExistParameterIndices.size - 1)
        tmp[notExistParameterIndices.size - 1] = 0.0f
        notExistParameterValues = FloatArray(notExistParameterIndices.size)
        System.arraycopy(tmp, 0, notExistParameterValues, 0, notExistParameterIndices.size)

        return parameterIndex
    }

    val parameterCount: Int
        /**
         * Get the number of parameters.
         *
         * @return the number of parameters
         */
        // TODO:: 写错了? 应该是parameterViews?
        get() = parameterViews.size

    fun getParameterValue(parameterId: CubismId): Float {
        // Speeding up the process, this can get partIndex. However, it is not necessary when setting externally because it is not frequently called.
        val parameterIndex = getParameterIndex(parameterId)
        return getParameterValue(parameterIndex)
    }

    fun getParameterValue(parameterIndex: Int): Float {
        if (notExistParameterIndices.contains(parameterIndex)) {
            val index = notExistParameterIndices.indexOf(parameterIndex)
            val value = notExistParameterValues[index]
            return value
        }

        // Detect whether partIndex is not out of bounds index
        assert(0 <= parameterIndex && parameterIndex < this.parameterCount)

        return parameterViews[parameterIndex].value
    }

    fun setParameterValue(parameterId: CubismId, value: Float, weight: Float = 1.0f) {
        val index = getParameterIndex(parameterId)
        setParameterValue(index, value, weight)
    }

    fun setParameterValue(parameterIndex: Int, value: Float, weight: Float = 1.0f) {
        var value = value
        if (notExistParameterIndices.contains(parameterIndex)) {
            val index = notExistParameterIndices.indexOf(parameterIndex)
            val parameterValue = notExistParameterValues[index]
            val weightedParameterValue = if (weight == 1.0f)
                value
            else
                (parameterValue * (1.0f - weight)) + (value * weight)
            notExistParameterValues[index] = weightedParameterValue
            return
        }


        // Detect whether partIndex is not out of bounds index
        assert(0 <= parameterIndex && parameterIndex < this.parameterCount)

        val parameter: CubismParameterView = parameterViews[parameterIndex]
        if (parameter.maximumValue < value) {
            value = parameter.maximumValue
        } else if (parameter.minimumValue > value) {
            value = parameter.minimumValue
        }

        val parameterValue: Float = parameter.value
        val weightedParameterValue = if (weight == 1.0f)
            value
        else
            (parameterValue * (1.0f - weight)) + (value * weight)
        parameter.value = weightedParameterValue
    }

    @JvmOverloads
    fun addParameterValue(parameterId: CubismId, value: Float, weight: Float = 1.0f) {
        val index = getParameterIndex(parameterId)
        addParameterValue(index, value, weight)
    }

    @JvmOverloads
    fun addParameterValue(parameterIndex: Int, value: Float, weight: Float = 1.0f) {
        setParameterValue(
            parameterIndex,
            getParameterValue(parameterIndex) + (value * weight)
        )
    }

    @JvmOverloads
    fun multiplyParameterValue(parameterId: CubismId, value: Float, weight: Float = 1.0f) {
        val index = getParameterIndex(parameterId)
        multiplyParameterValue(index, value, weight)
    }

    @JvmOverloads
    fun multiplyParameterValue(parameterIndex: Int, value: Float, weight: Float = 1.0f) {
        setParameterValue(
            parameterIndex,
            getParameterValue(parameterIndex) * (1.0f + (value - 1.0f) * weight)
        )
    }

    /* --------- *
     * DRAWABLES *
     * --------- */

    fun getDrawableIndex(drawableId: CubismId): Int {
        val drawableIndex: CubismDrawableView? = model.findDrawableView(drawableId.string)
        if (drawableIndex != null) {
            return drawableIndex.index
        }

        return -1
    }

    val drawableCount: Int
        get() = model.drawableViews.size

    val drawableRenderOrders: IntArray?
        get() {
            return if (model.drawableViews.isNotEmpty()) {
                model.drawableViews[0].drawables.renderOrders
            } else {
                IntArray(0)
            }
        }

    fun getDrawableTextureIndex(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].textureIndex
    }

    fun getDrawableVertexIndices(drawableIndex: Int): ShortArray? {
        return model.drawableViews[drawableIndex].indices
    }

    fun getDrawableVertexCount(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].vertexCount
    }

    fun getDrawableVertices(drawableIndex: Int): FloatArray? {
        return getDrawableVertexPositions(drawableIndex)
    }

    fun getDrawableVertexPositions(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].vertexPositions
    }

    fun getDrawableVertexUvs(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].vertexUvs
    }

    fun getDrawableOpacity(drawableIndex: Int): Float {
        return model.drawableViews[drawableIndex].opacity
    }

    fun getDrawableMultiplyColor(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].multiplyColors
    }

    fun getDrawableScreenColor(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].screenColors
    }

    fun getDrawableParentPartIndex(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].parentPartIndex
    }

    fun getDrawableBlendMode(drawableIndex: Int): CubismBlendMode {
        val constantFlag: Byte = model.drawableViews[drawableIndex].constantFlag
        return if (isBitSet(constantFlag, BLEND_ADDITIVE))
            CubismBlendMode.ADDITIVE
        else
            if (isBitSet(constantFlag, BLEND_MULTIPLICATIVE))
                CubismBlendMode.MULTIPLICATIVE
            else
                CubismBlendMode.NORMAL
    }

    fun getDrawableInvertedMask(drawableIndex: Int): Boolean {
        val constantFlag: Byte = model.drawableViews[drawableIndex].constantFlag

        return isBitSet(constantFlag, IS_INVERTED_MASK)
    }

    fun getDrawableDynamicFlagIsVisible(drawableIndex: Int): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, IS_VISIBLE)
    }

    fun getDrawableDynamicFlagVisibilityDidChange(drawableIndex: Int): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, VISIBILITY_DID_CHANGE)
    }

    fun getDrawableDynamicFlagOpacityDidChange(drawableIndex: Int): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, OPACITY_DID_CHANGE)
    }

    fun getDrawableDynamicFlagDrawOrderDidChange(drawableIndex: Int): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, DRAW_ORDER_DID_CHANGE)
    }

    fun getDrawableDynamicFlagRenderOrderDidChange(drawableIndex: Int): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, RENDER_ORDER_DID_CHANGE)
    }

    fun getDrawableDynamicFlagVertexPositionsDidChange(drawableIndex: Int): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, VERTEX_POSITIONS_DID_CHANGE)
    }

    fun getDrawableDynamicFlagBlendColorDidChange(drawableIndex: Int): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, BLEND_COLOR_DID_CHANGE)
    }

    val drawableMasks: Array<IntArray?>?
        get() {
            return if (model.drawableViews.isNotEmpty()) {
                model.drawableViews[0].drawables.masks
            } else {
                null
            }
        }

    val drawableMaskCounts: IntArray?
        get() {
            return if (model.drawableViews.isNotEmpty()) {
                model.drawableViews[0].drawables.maskCounts
            } else {
                null
            }
        }

    val isUsingMasking: Boolean
        get() {
            return this.drawableMaskCounts?.let {
                for (i in it) {
                    if (i <= 0) {
                        continue
                    }
                }
                true
            } ?: false
        }

    fun loadParameters() {
        var parameterCount = this.parameterCount
        val savedParameterCount = savedParameters.size

        if (parameterCount > savedParameterCount) {
            parameterCount = savedParameterCount
        }

        for (i in 0..<parameterCount) {
            parameterViews[i].value = savedParameters[i]
        }
    }

    fun saveParameters() {
        val parameterCount = this.parameterCount

        if (savedParameters.size < parameterCount) {
            savedParameters = FloatArray(parameterCount)
        }
        for (i in 0..<parameterCount) {
            savedParameters[i] = parameterViews[i].value
        }
    }

    fun getMultiplyColor(drawableIndex: Int): CubismRenderer.CubismTextureColor? {
        return if (getOverwriteFlagForDrawableMultiplyColors(drawableIndex)) {
            userDrawableMultiplyColors[drawableIndex]!!.color
        } else {
            getDrawableMultiplyColor(drawableIndex)?.let { color ->
                CubismRenderer.CubismTextureColor(
                    color[0],
                    color[1],
                    color[2],
                    color[3],
                )
            }
        }
    }

    fun getScreenColor(drawableIndex: Int): CubismRenderer.CubismTextureColor? {
        return if (getOverwriteFlagForDrawableScreenColors(drawableIndex)) {
            userDrawableScreenColors.get(drawableIndex)!!.color
        } else {
            getDrawableScreenColor(drawableIndex)?.let { color ->
                CubismRenderer.CubismTextureColor(
                    color[0],
                    color[1],
                    color[2],
                    color[3],
                )
            }
        }
    }

    fun setMultiplyColor(drawableIndex: Int, color: CubismRenderer.CubismTextureColor) {
        setMultiplyColor(drawableIndex, color.r, color.g, color.b, color.a)
    }

    fun setMultiplyColor(drawableIndex: Int, r: Float, g: Float, b: Float, a: Float) {
        userDrawableMultiplyColors.get(drawableIndex)!!.color.r = r
        userDrawableMultiplyColors.get(drawableIndex)!!.color.g = g
        userDrawableMultiplyColors.get(drawableIndex)!!.color.b = b
        userDrawableMultiplyColors.get(drawableIndex)!!.color.a = a
    }

    fun getPartMultiplyColor(partIndex: Int): CubismRenderer.CubismTextureColor {
        return userPartMultiplyColors.get(partIndex)!!.color
    }

    fun getPartScreenColor(partIndex: Int): CubismRenderer.CubismTextureColor {
        return userPartScreenColors.get(partIndex)!!.color
    }

    fun setPartMultiplyColor(partIndex: Int, color: CubismRenderer.CubismTextureColor) {
        setPartColor(
            partIndex,
            color.r,
            color.g,
            color.b,
            color.a,
            userPartMultiplyColors,
            userDrawableMultiplyColors
        )
    }

    fun setPartMultiplyColor(partIndex: Int, r: Float, g: Float, b: Float, a: Float) {
        setPartColor(partIndex, r, g, b, a, userPartMultiplyColors, userDrawableMultiplyColors)
    }

    fun setScreenColor(drawableIndex: Int, color: CubismRenderer.CubismTextureColor) {
        setScreenColor(drawableIndex, color.r, color.g, color.b, color.a)
    }

    fun setScreenColor(drawableIndex: Int, r: Float, g: Float, b: Float, a: Float) {
        userDrawableScreenColors.get(drawableIndex)!!.color.r = r
        userDrawableScreenColors.get(drawableIndex)!!.color.g = g
        userDrawableScreenColors.get(drawableIndex)!!.color.b = b
        userDrawableScreenColors.get(drawableIndex)!!.color.a = a
    }

    fun setPartScreenColor(partIndex: Int, color: CubismRenderer.CubismTextureColor) {
        setPartScreenColor(partIndex, color.r, color.g, color.b, color.a)
    }

    fun setPartScreenColor(partIndex: Int, r: Float, g: Float, b: Float, a: Float) {
        setPartColor(partIndex, r, g, b, a, userPartScreenColors, userDrawableScreenColors)
    }

    fun getOverwriteFlagForDrawableMultiplyColors(drawableIndex: Int): Boolean {
        return userDrawableMultiplyColors.get(drawableIndex)!!.isOverwritten
    }

    fun getOverwriteFlagForDrawableScreenColors(drawableIndex: Int): Boolean {
        return userDrawableScreenColors.get(drawableIndex)!!.isOverwritten
    }

    fun getOverwriteColorForPartMultiplyColors(partIndex: Int): Boolean {
        return userPartMultiplyColors.get(partIndex)!!.isOverwritten
    }

    fun getOverwriteColorForPartScreenColors(partIndex: Int): Boolean {
        return userPartScreenColors.get(partIndex)!!.isOverwritten
    }

    fun setOverwriteFlagForDrawableMultiplyColors(drawableIndex: Int, value: Boolean) {
        userDrawableMultiplyColors.get(drawableIndex)!!.isOverwritten = value
    }

    fun setOverwriteFlagForDrawableScreenColors(drawableIndex: Int, value: Boolean) {
        userDrawableScreenColors.get(drawableIndex)!!.isOverwritten = value
    }

    fun setOverwriteColorForPartMultiplyColors(partIndex: Int, value: Boolean) {
        userPartMultiplyColors.get(partIndex)!!.isOverwritten = value
        setOverwriteColorsForPartColors(
            partIndex,
            value,
            userPartMultiplyColors,
            userDrawableMultiplyColors
        )
    }

    fun setOverwriteColorForPartScreenColors(partIndex: Int, value: Boolean) {
        userPartScreenColors.get(partIndex)!!.isOverwritten = value
        setOverwriteColorsForPartColors(
            partIndex,
            value,
            userPartScreenColors,
            userDrawableScreenColors
        )
    }

    fun getDrawableCulling(drawableIndex: Int): Boolean {
        if (getOverwriteFlagForDrawableCullings(drawableIndex)) {
            return userDrawableCullings.get(drawableIndex)!!.isCulling
        }

        val constantFlag: Byte = model.drawableViews[drawableIndex].constantFlag
        return !isBitSet(constantFlag, IS_DOUBLE_SIDED)
    }

    fun setDrawableCulling(drawableIndex: Int, isCulling: Boolean) {
        userDrawableCullings.get(drawableIndex)!!.isCulling = isCulling
    }

    fun getOverwriteFlagForDrawableCullings(drawableIndex: Int): Boolean {
        return userDrawableCullings.get(drawableIndex)!!.isOverWritten
    }

    fun setOverwriteFlagForDrawableCullings(drawableIndex: Int, value: Boolean) {
        userDrawableCullings.get(drawableIndex)!!.isOverWritten = value
    }

    fun close() {
        model.close()
        model.moc.close()
    }

    /**
     * Initialize the model.
     */
    fun initialize() {
        checkNotNull(model)

        parameterViews = model.parameterViews
        partViews = model.partViews

        // MultiplyColors
        val mutiplyColor: CubismRenderer.CubismTextureColor = CubismRenderer.CubismTextureColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
        )

        // ScreenColors
        val screenColor: CubismRenderer.CubismTextureColor = CubismRenderer.CubismTextureColor(
            0.0f,
            0.0f,
            0.0f,
            1.0f
        )

        model.drawableViews.let { drawableViews ->
            userDrawableMultiplyColors = List(drawableViews.size) {
                DrawableColorData(mutiplyColor)
            }
            userDrawableScreenColors = List(drawableViews.size) {
                DrawableColorData(screenColor)
            }
            userDrawableCullings = List(drawableViews.size) {
                DrawableCullingData()
            }

            partChildDrawablesMap = HashMap(drawableViews.count { it.parentPartIndex >= 0 })
            drawableViews.forEach { drawableView ->
                // Bind parent Parts and child Drawables.
                drawableView.parentPartIndex.takeIf { it >= 0 }?.let { parentIndex ->
                    partChildDrawablesMap.getOrPut(parentIndex) {
                        mutableListOf(drawableView.index)
                    }
                }
            }
        }

        model.partViews.let { partViews ->
            userPartMultiplyColors = List(partViews.size) {
                PartColorData(mutiplyColor)
            }
            userPartScreenColors = List(partViews.size) {
                PartColorData(screenColor)
            }
        }
    }

    /**
     * Return true if the logical product of flag and mask matches the mask.
     *
     * @return Return true if the logical product of flag and mask matches the mask.
     */
    private fun isBitSet(flag: Byte, mask: Byte): Boolean {
        return (flag.toInt() and mask.toInt()) == mask.toInt()
    }

    /**
     * PartのOverwriteColorを設定する。
     *
     * @param partIndex 設定するPartのインデックス
     * @param r 赤
     * @param g 緑
     * @param b 青
     * @param a アルファ
     * @param partColors 設定するPartの上書き色のリスト
     * @param drawableColors Drawableの上書き色のリスト
     */
    private fun setPartColor(
        partIndex: Int,
        r: Float, g: Float, b: Float, a: Float,
        partColors: MutableList<PartColorData?>,
        drawableColors: MutableList<DrawableColorData?>
    ) {
        partColors.get(partIndex)!!.color.r = r
        partColors.get(partIndex)!!.color.g = g
        partColors.get(partIndex)!!.color.b = b
        partColors.get(partIndex)!!.color.a = a

        if (partColors.get(partIndex)!!.isOverwritten) {
            val childDrawables = partChildDrawablesMap!!.get(partIndex)
            if (childDrawables == null) return

            for (i in childDrawables.indices) {
                val drawableIndex: Int = childDrawables.get(i)!!

                drawableColors.get(drawableIndex)!!.color.r = r
                drawableColors.get(drawableIndex)!!.color.g = g
                drawableColors.get(drawableIndex)!!.color.b = b
                drawableColors.get(drawableIndex)!!.color.a = a
            }
        }
    }

    /**
     * PartのOverwriteFlagを設定する。
     *
     * @param partIndex 設定するPartのインデックス
     * @param value 真偽値
     * @param partColors 設定するPartの上書き色のリスト
     * @param drawableColors Drawableの上書き色のリスト
     */
    private fun setOverwriteColorsForPartColors(
        partIndex: Int,
        value: Boolean,
        partColors: MutableList<PartColorData?>,
        drawableColors: MutableList<DrawableColorData?>
    ) {
        partColors.get(partIndex)!!.isOverwritten = value

        val childDrawables = partChildDrawablesMap!!.get(partIndex)
        if (childDrawables == null) return

        for (i in childDrawables.indices) {
            val drawableIndex: Int = childDrawables.get(i)!!
            drawableColors.get(drawableIndex)!!.isOverwritten = value

            if (value) {
                drawableColors.get(drawableIndex)!!.color.r = partColors.get(partIndex)!!.color.r
                drawableColors.get(drawableIndex)!!.color.g = partColors.get(partIndex)!!.color.g
                drawableColors.get(drawableIndex)!!.color.b = partColors.get(partIndex)!!.color.b
                drawableColors.get(drawableIndex)!!.color.a = partColors.get(partIndex)!!.color.a
            }
        }
    }

    /**
     * List of opacities for non-existent parts
     */
    private var notExistPartOpacities = FloatArray(1)

    private val notExistPartIndices: MutableList<Int?> = ArrayList<Int?>()

    /**
     * List of IDs for non-existent parts
     */
    private val notExistPartIds: MutableMap<CubismId?, Int?> = HashMap<CubismId?, Int?>()

    /**
     * List of values for non-existent parameters
     */
    private var notExistParameterValues = FloatArray(1)

    private val notExistParameterIndices: MutableList<Int?> = ArrayList<Int?>()

    /**
     * List of IDs for non-existent parameters
     */
    private val notExistParameterIds: MutableMap<CubismId?, Int> = HashMap<CubismId?, Int>()

    /**
     * Saved parameters
     */
    private var savedParameters = FloatArray(1)

    /**
     * Get the model.
     *
     * @return model
     */

    private var parameterViews: Array<CubismParameterView>
    private var partViews: Array<CubismPartView>

    var modelOpacity: Float = 1.0f

    private lateinit var userDrawableMultiplyColors: List<DrawableColorData>
    private lateinit var userDrawableScreenColors: List<DrawableColorData>
    private lateinit var userDrawableCullings: List<DrawableCullingData>

    /**
     * Partとその子DrawableのListとのMap
     */
    private lateinit var partChildDrawablesMap: MutableMap<Int, MutableList<Int>>

    private lateinit var userPartMultiplyColors: List<PartColorData?>
    private lateinit var userPartScreenColors: List<PartColorData?>
}
