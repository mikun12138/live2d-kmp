/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.core.CubismDrawableFlag.ConstantFlag
import com.live2d.sdk.cubism.core.CubismDrawableFlag.DynamicFlag
import com.live2d.sdk.cubism.core.CubismDrawableView
import com.live2d.sdk.cubism.core.CubismModel
import com.live2d.sdk.cubism.core.CubismParameterView
import com.live2d.sdk.cubism.core.CubismPartView
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.rendering.CubismBlendMode
import com.live2d.sdk.cubism.framework.rendering.CubismTextureColor

class Model {
    lateinit var model: CubismModel
    fun init(model: CubismModel): Model {
        this.model = model

        val multiplyColor = CubismTextureColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
        )
        val screenColor = CubismTextureColor(
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

//        model.partViews.let { partViews ->
//            userPartMultiplyColors = List(partViews.size) {
//                PartColorData(mutiplyColor)
//            }
//            userPartScreenColors = List(partViews.size) {
//                PartColorData(screenColor)
//            }
//        }

        return this
    }

    /**
     * Inner class for handling texture colors in RGBA
     */
    data class DrawableColorData(
        var color: CubismTextureColor,
        var isOverwritten: Boolean = false,
    )

    data class PartColorData(
        var color: CubismTextureColor,
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


    private val modelColor = CubismTextureColor()

    fun getModelColorWithOpacity(opacity: Float): CubismTextureColor {
        return CubismTextureColor(
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

    fun getPartIndex(partId: CubismId): Int {
        val partView: CubismPartView? = model.findPartView(partId.value)
        if (partView != null) {
            return partView.index
        }

        // If the part does not exist in the model, it searches for it in the non-existent part ID list and returns its index.
        if (notExistPartIds.containsKey(partId)) {
            return notExistPartIds.get(partId)!!
        }

        // If the part does not exist in the non-existent part ID list, add newly the element.
        val partIndex = model.partViews.size + notExistPartIds.size
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

        model.partViews[partIndex].opacity = opacity
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

        return model.partViews[partIndex].opacity
    }

    /* ---------- *
     * PARAMETERS *
     * ---------- */

    fun getParameterIndex(parameterId: CubismId): Int {
        val parameterView: CubismParameterView? = model.findParameterView(parameterId.value)
        if (parameterView != null) {
            return parameterView.index
        }

        // If the parameter does not exist in the model, it searches for it in the non-existent parameter ID list and returns its index.
        if (notExistParameterIds.containsKey(parameterId)) {
            val index = checkNotNull(notExistParameterIds.get(parameterId))
            return index
        }

        // If the parameter does not exist in the non-existent parameter ID list, add newly the element.
        val parameterIndex = model.parameterViews.size + notExistParameterIds.size
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
        get() = model.parameterViews.size

    /**
     * Get the maximum value of parameters.
     *
     * @param parameterIndex parameter index
     * @return the maximum value of parameter
     */
    fun getParameterMaximumValue(parameterIndex: Int): Float {
        return model.parameterViews[parameterIndex].maximumValue
    }

    /**
     * Get the minimum value of parameters.
     *
     * @param parameterIndex parameter index
     * @return the minimum value of parameter
     */
    fun getParameterMinimumValue(parameterIndex: Int): Float {
        return model.parameterViews[parameterIndex].minimumValue
    }

    /**
     * Get the default value of parameters.
     *
     * @param parameterIndex parameter index
     * @return the default value of parameter
     */
    fun getParameterDefaultValue(parameterIndex: Int): Float {
        return model.parameterViews[parameterIndex].defaultValue
    }

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

        return model.parameterViews[parameterIndex].value
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
            val weightedParameterValue =
                if (weight == 1.0f)
                    value
                else
                    (parameterValue * (1.0f - weight)) + (value * weight)
            notExistParameterValues[index] = weightedParameterValue
            return
        }


        // Detect whether partIndex is not out of bounds index
        check(0 <= parameterIndex && parameterIndex < this.parameterCount)

        val parameter: CubismParameterView = model.parameterViews[parameterIndex]
        if (parameter.maximumValue < value) {
            value = parameter.maximumValue
        } else if (parameter.minimumValue > value) {
            value = parameter.minimumValue
        }

        val parameterValue: Float = parameter.value
        val weightedParameterValue =
            if (weight == 1.0f)
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

    val drawableCount: Int
        get() = model.drawableViews.size

    fun getDrawableIndex(drawableId: CubismId): Int {
        val drawableIndex: CubismDrawableView? = model.findDrawableView(drawableId.value)
        if (drawableIndex != null) {
            return drawableIndex.index
        }

        return -1
    }

    /*
        Drawable - ConstantFlag
     */
    private fun getDrawableConstantFlag(
        drawableIndex: Int,
        flag: ConstantFlag,
    ): Boolean {
        val constantFlag: Byte = model.drawableViews[drawableIndex].constantFlag
        return isBitSet(constantFlag, flag.value)
    }

    fun getDrawableBlendMode(drawableIndex: Int): CubismBlendMode {
        val constantFlag = model.drawableViews[drawableIndex].constantFlag
        return if (isBitSet(constantFlag, ConstantFlag.BLEND_ADDITIVE.value))
            CubismBlendMode.ADDITIVE
        else
            if (isBitSet(constantFlag, ConstantFlag.BLEND_MULTIPLICATIVE.value))
                CubismBlendMode.MULTIPLICATIVE
            else
                CubismBlendMode.NORMAL
    }

    fun getDrawableIsDoubleSided(drawableIndex: Int): Boolean {
        return getDrawableConstantFlag(drawableIndex, ConstantFlag.IS_DOUBLE_SIDED)
    }

    fun getDrawableInvertedMask(drawableIndex: Int): Boolean {
        val constantFlag = model.drawableViews[drawableIndex].constantFlag

        return isBitSet(constantFlag, ConstantFlag.IS_INVERTED_MASK.value)
    }

    /*
1        Drawable - DynamicFlag
     */

    private fun getDrawableDynamicFlag(drawableIndex: Int, flag: DynamicFlag): Boolean {
        val dynamicFlag: Byte = model.drawableViews[drawableIndex].dynamicFlag
        return isBitSet(dynamicFlag, flag.value)
    }

    fun getDrawableDynamicFlagIsVisible(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.IS_VISIBLE)
    }

    fun getDrawableDynamicFlagVertexPositionsDidChange(drawableIndex: Int): Boolean {
        return getDrawableDynamicFlag(drawableIndex, DynamicFlag.VERTEX_POSITIONS_DID_CHANGE)
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

    fun getDrawableMask(drawableIndex: Int): IntArray? {
        return model.drawableViews[drawableIndex].masks
    }

    fun getDrawableVertexCount(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].vertexCount
    }

    fun getDrawableVertexPositions(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].vertexPositions
    }

    fun getDrawableVertexUVs(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].vertexUvs
    }

    fun getDrawableIndices(drawableIndex: Int): ShortArray? {
        return model.drawableViews[drawableIndex].indices
    }

    fun getDrawableMultiplyColors(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].multiplyColors
    }

    fun getDrawableScreenColors(drawableIndex: Int): FloatArray? {
        return model.drawableViews[drawableIndex].screenColors
    }

    fun getDrawableParentPartIndex(drawableIndex: Int): Int {
        return model.drawableViews[drawableIndex].parentPartIndex
    }

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
        return (flag.toInt() and mask.toInt()) == mask.toInt()
    }


    /**
     * List of IDs for non-existent parameters
     */
    private val notExistParameterIds: MutableMap<CubismId?, Int> = HashMap<CubismId?, Int>()
    private val notExistParameterIndices: MutableList<Int?> = ArrayList<Int?>()
    private var notExistParameterValues = FloatArray(1)
    private val notExistPartIds: MutableMap<CubismId?, Int?> = HashMap<CubismId?, Int?>()

    private val notExistPartIndices: MutableList<Int?> = ArrayList<Int?>()
    private var notExistPartOpacities = FloatArray(1)

    /**
     * Saved parameters
     */
    private var savedParameters = FloatArray(1)

    // 不知道干什么的
    var modelOpacity: Float = 1.0f

    private lateinit var userDrawableMultiplyColors: List<DrawableColorData>
    private lateinit var userDrawableScreenColors: List<DrawableColorData>
    private lateinit var userDrawableCullings: List<DrawableCullingData>

    /**
     * Partとその子DrawableのListとのMap
     */
    private lateinit var partChildDrawablesMap: MutableMap<Int, MutableList<Int>>

//    private lateinit var userPartMultiplyColors: List<PartColorData>
//    private lateinit var userPartScreenColors: List<PartColorData>
}

