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
                // Parameter ID
                val parameterId: CubismId = idManager.id(
                    param.id
                )
                // Setting of calculation method.
                val blendType = when (param.blend) {
                    ExpressionBlendType.ADD.type -> {
                        ExpressionBlendType.ADD
                    }
                    ExpressionBlendType.MULTIPLY.type -> {
                        ExpressionBlendType.MULTIPLY
                    }
                    ExpressionBlendType.OVERWRITE.type -> {
                        ExpressionBlendType.OVERWRITE
                    }
                    else -> {
                        ExpressionBlendType.ADD
                    }
                }
                // Value
                val value = param.value

                // Create a configuration object and add it to the list.
                val item = ExpressionParameter(parameterId, blendType, value)
                this.expressionParameters.add(item)
            }
        }
    }

    enum class ExpressionBlendType(
         val type: String
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
        motionQueueEntry: CubismMotionQueueEntry?,
        expressionParameterValues: MutableList<CubismExpressionMotionManager.ExpressionParameterValue>?,
        expressionIndex: Int,
        fadeWeight: Float
    ) {
        if (motionQueueEntry == null || expressionParameterValues == null) {
            return
        }

        if (!motionQueueEntry.isAvailable()) {
            return
        }

        // CubismExpressionMotion.fadeWeight は廃止予定です。
        // 互換性のために処理は残りますが、実際には使用しておりません。
        this.fadeWeight = updateFadeWeight(motionQueueEntry, userTimeSeconds)

        // モデルに適用する値を計算
        for (i in expressionParameterValues.indices) {
            val expParamValue: CubismExpressionMotionManager.ExpressionParameterValue =
                expressionParameterValues.get(i)

            if (expParamValue.parameterId == null) {
                continue
            }

            expParamValue.overwriteValue = model.getParameterValue(expParamValue.parameterId)
            val currentParameterValue: Float = expParamValue.overwriteValue

            val expressionParameters =
                this.expressionParameters
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
                    expParamValue.overwriteValue = currentParameterValue
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
                        currentParameterValue,
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
                    newOverwriteValue = currentParameterValue
                }

                ExpressionBlendType.MULTIPLY -> {
                    newAdditiveValue = DEFAULT_ADDITIVE_VALUE
                    newMultiplyValue = value
                    newOverwriteValue = currentParameterValue
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


    protected override fun doUpdateParameters(
        model: CubismModel,
        userTimeSeconds: Float,
        weight: Float,
        motionQueueEntry: CubismMotionQueueEntry
    ) {
        for (i in expressionParameters.indices) {
            val parameter = expressionParameters.get(i)
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

    /**
     * Parameter information list for facial expressions
     */
    val expressionParameters: MutableList<ExpressionParameter> = ArrayList<ExpressionParameter>()

    /**
     * 表情の現在のウェイト
     *
     */
    @get:Deprecated(
        """CubismExpressionMotion.fadeWeightが削除予定のため非推奨。
      CubismExpressionMotionManager.getFadeWeight(int index) を使用してください。
      """
    )
    @Deprecated("不具合を引き起こす要因となるため非推奨。")
    var fadeWeight: Float = 0f
        private set

    companion object {

        const val DEFAULT_FADE_TIME: Float = 1.0f
        const val DEFAULT_ADDITIVE_VALUE: Float = 0.0f
        const val DEFAULT_MULTIPLY_VALUE: Float = 1.0f
    }
}
