/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.CubismFramework.idManager
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.utils.json.ExpressionJson
import kotlinx.serialization.json.Json

/**
 * A motion class for facial expressions.
 */
class CubismExpressionMotion : ACubismMotion {

    constructor(buffer: ByteArray) {
        parse(buffer)
    }

    private fun parse(exp3Json: ByteArray) {
        Json.decodeFromString<ExpressionJson>(String(exp3Json)).let { json ->
            fadeInSeconds = json.fadeInTime?.let {
                it
            } ?: DEFAULT_FADE_TIME
            fadeOutSeconds = json.fadeOutTime?.let {
                it
            } ?: DEFAULT_FADE_TIME

            // Each parameter setting
            for (param in json.parameters) {
                // Create a configuration object and add it to the list.
                this.parameters.add(
                    ExpressionParameter(
                        parameterId = idManager.id(
                            param.id
                        ),
                        blendType = when (param.blend) {
                            ExpressionBlendType.ADD.value -> {
                                ExpressionBlendType.ADD
                            }

                            ExpressionBlendType.MULTIPLY.value -> {
                                ExpressionBlendType.MULTIPLY
                            }

                            ExpressionBlendType.OVERWRITE.value -> {
                                ExpressionBlendType.OVERWRITE
                            }

                            else -> {
                                ExpressionBlendType.ADD
                            }
                        },
                        value = param.value
                    )
                )
            }
        }
    }

    enum class ExpressionBlendType(
        val value: String
    ) {
        ADD("Add"),
        MULTIPLY("Multiply"),
        OVERWRITE("Overwrite");
    }

    class ExpressionParameter(
        val parameterId: CubismId,
        val blendType: ExpressionBlendType,
        val value: Float,
    )

    /**
     * モデルの表情に関するパラメータを計算する。
     *
     * @param model 対象のモデル
     * @param userTimeSeconds デルタ時間の積算値[秒]
     * @param motionQueueEntry CubismMotionQueueManagerで管理されているモーション
     * @param expressionParameterValues モデルに適用する各パラメータの値
     * @param expressionIndex 表情のインデックス
     * @param fadeWeight 表情のウェイト
     */
    fun calculateExpressionParameters(
        model: CubismModel,
        userTimeSeconds: Float,
        motionQueueEntry: CubismMotionQueueEntry,
        expressionParameterValues: MutableList<CubismExpressionMotionManager.ExpressionParameterValue>,
        expressionIndex: Int,
        fadeWeight: Float
    ) {

        // モデルに適用する値を計算
        for (expParamValue in expressionParameterValues) {

            expParamValue.overwriteValue = model.getParameterValue(expParamValue.parameterId)

            val expressionParameters = this.parameters
            var parameterIndex = -1
            for (j in expressionParameters.indices) {
                if (expParamValue.parameterId !== expressionParameters.get(j).parameterId) {
                    continue
                }

                parameterIndex = j
                break
            }

            // 再生中のExpressionが参照していないパラメータは初期値を適用
            if (parameterIndex < 0) {
                if (expressionIndex == 0) {
                    expParamValue.additiveValue = DEFAULT_ADDITIVE_VALUE
                    expParamValue.multiplyValue = DEFAULT_MULTIPLY_VALUE
                    expParamValue.overwriteValue = expParamValue.overwriteValue
                } else {
                    expParamValue.additiveValue = calculateValue(
                        expParamValue.additiveValue,
                        DEFAULT_ADDITIVE_VALUE,
                        fadeWeight
                    )
                    expParamValue.multiplyValue = calculateValue(
                        expParamValue.multiplyValue,
                        DEFAULT_MULTIPLY_VALUE,
                        fadeWeight
                    )
                    expParamValue.overwriteValue = calculateValue(
                        expParamValue.overwriteValue,
                        expParamValue.overwriteValue,
                        fadeWeight
                    )
                }
                continue
            }

            // 値を計算
            val value = expressionParameters.get(parameterIndex).value
            val newAdditiveValue: Float
            val newMultiplyValue: Float
            val newOverwriteValue: Float

            when (expressionParameters.get(parameterIndex).blendType) {
                ExpressionBlendType.ADD -> {
                    newAdditiveValue = value
                    newMultiplyValue = DEFAULT_MULTIPLY_VALUE
                    newOverwriteValue = expParamValue.overwriteValue
                }

                ExpressionBlendType.MULTIPLY -> {
                    newAdditiveValue = DEFAULT_ADDITIVE_VALUE
                    newMultiplyValue = value
                    newOverwriteValue = expParamValue.overwriteValue
                }

                ExpressionBlendType.OVERWRITE -> {
                    newAdditiveValue = DEFAULT_ADDITIVE_VALUE
                    newMultiplyValue = DEFAULT_MULTIPLY_VALUE
                    newOverwriteValue = value
                }

                else -> return
            }

            if (expressionIndex == 0) {
                expParamValue.additiveValue = newAdditiveValue
                expParamValue.multiplyValue = newMultiplyValue
                expParamValue.overwriteValue = newOverwriteValue
            } else {
                expParamValue.additiveValue =
                    (expParamValue.additiveValue * (1.0f - fadeWeight)) + newAdditiveValue * fadeWeight
                expParamValue.multiplyValue =
                    (expParamValue.multiplyValue * (1.0f - fadeWeight)) + newMultiplyValue * fadeWeight
                expParamValue.overwriteValue =
                    (expParamValue.overwriteValue * (1.0f - fadeWeight)) + newOverwriteValue * fadeWeight
            }
        }
    }


    override fun doUpdateParameters(
        model: CubismModel,
        userTimeSeconds: Float,
        weight: Float,
        motionQueueEntry: CubismMotionQueueEntry
    ) {
        for (i in parameters.indices) {
            val parameter = parameters.get(i)
            when (parameter.blendType) {
                ExpressionBlendType.ADD -> model.addParameterValue(
                    parameter.parameterId,
                    parameter.value,
                    weight
                )

                ExpressionBlendType.MULTIPLY -> model.multiplyParameterValue(
                    parameter.parameterId,
                    parameter.value,
                    weight
                )

                ExpressionBlendType.OVERWRITE -> model.setParameterValue(
                    parameter.parameterId,
                    parameter.value,
                    weight
                )

                else -> {}
            }
        }
    }

    /**
     * 入力された値でブレンド計算をする。
     *
     * @param source 現在の値
     * @param destination 適用する値
     *
     * @return 計算されたブレンド値
     */
    private fun calculateValue(source: Float, destination: Float, fadeWeight: Float): Float {
        return (source * (1.0f - fadeWeight)) + (destination * fadeWeight)
    }

    val parameters: MutableList<ExpressionParameter> = ArrayList<ExpressionParameter>()

    companion object {

        const val DEFAULT_FADE_TIME: Float = 1.0f
        const val DEFAULT_ADDITIVE_VALUE: Float = 0.0f
        const val DEFAULT_MULTIPLY_VALUE: Float = 1.0f
    }
}
