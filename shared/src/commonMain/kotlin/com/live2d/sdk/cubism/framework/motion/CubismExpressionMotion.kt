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
import com.live2d.sdk.cubism.framework.utils.jsonparser.ACubismJsonValue
import com.live2d.sdk.cubism.framework.utils.jsonparser.CubismJson

/**
 * A motion class for facial expressions.
 */
class CubismExpressionMotion
/**
 * デフォルトコンストラクタ
 */
protected constructor() : ACubismMotion() {
    /**
     * Calculation method of facial expression parameter values.
     */
    enum class ExpressionBlendType(type: String) {
        /**
         * Addition
         */
        ADD("Add"),

        /**
         * Multiplication
         */
        MULTIPLY("Multiply"),

        /**
         * Overwriting
         */
        OVERWRITE("Overwrite");

        private val type: String?

        init {
            this.type = type
        }
    }

    /**
     * Internal class for expression parameter information.
     */
    class ExpressionParameter(id: CubismId, method: ExpressionBlendType, value: Float) {
        /**
         * Parameter ID
         */
        val parameterId: CubismId

        /**
         * Type of parameter calculation
         */
        val blendType: ExpressionBlendType

        /**
         * Value
         */
        val value: Float

        init {
            require(!(id == null || method == null)) { "id or method is null." }
            this.parameterId = id
            this.blendType = method
            this.value = value
        }
    }

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
        motionQueueEntry: CubismMotionQueueEntry?
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
     * exp3.jsonをパースする。
     *
     * @param exp3Json exp3.jsonが読み込まれているbyte配列
     */
    protected fun parse(exp3Json: ByteArray?) {
        val json = CubismJson.create(exp3Json!!)

        setFadeInTime(json.root.get(ExpressionKey.FADE_IN.key).toFloat(DEFAULT_FADE_TIME))
        setFadeOutTime(json.root.get(ExpressionKey.FADE_OUT.key).toFloat(DEFAULT_FADE_TIME))

        val jsonParameters = json.root.get(ExpressionKey.PARAMETERS.key)
        // Each parameter setting
        for (i in 0..<jsonParameters.size()) {
            val param = jsonParameters.get(i)

            // Parameter ID
            val parameterId: CubismId = idManager.getId(param.get(ExpressionKey.ID.key).string)
            // Setting of calculation method.
            val blendType = getBlendMethod(param)
            // Value
            val value = param.get(ExpressionKey.VALUE.key).toFloat()

            // Create a configuration object and add it to the list.
            val item = ExpressionParameter(parameterId, blendType, value)
            this.expressionParameters.add(item)
        }
    }

    /**
     * Key of exp3.json.
     */
    private enum class ExpressionKey(key: String) {
        FADE_IN("FadeInTime"),
        FADE_OUT("FadeOutTime"),
        PARAMETERS("Parameters"),
        ID("Id"),
        VALUE("Value"),
        BLEND("Blend");

        private val key: String?

        init {
            this.key = key
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
     * 表情が参照しているパラメータを取得する。
     *
     * @return 表情が参照しているパラメータ
     */
    /**
     * Parameter information list for facial expressions
     */
    val expressionParameters: MutableList<ExpressionParameter> = ArrayList<ExpressionParameter>()

    /**
     * 現在の表情のフェードのウェイト値を取得する。
     *
     * @return 表情のフェードのウェイト値
     *
     * @see CubismExpressionMotionManager.getFadeWeight
     */
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
        /**
         * Default fade duration.
         */
        const val DEFAULT_FADE_TIME: Float = 1.0f

        /**
         * 加算適用の初期値
         */
        const val DEFAULT_ADDITIVE_VALUE: Float = 0.0f

        /**
         * 乗算適用の初期値
         */
        const val DEFAULT_MULTIPLY_VALUE: Float = 1.0f

        /**
         * Create an ACubismMotion instance.
         *
         * @param buffer buffer where exp3.json file is loaded
         * @return created instance
         */
        fun create(buffer: ByteArray?): CubismExpressionMotion {
            val expression = CubismExpressionMotion()
            expression.parse(buffer)

            return expression
        }

        /**
         * Get the calculation method for the parameter values of expressions set in JSON.
         *
         * @param parameter JSON parameter value
         * @return calculation method set in JSON
         */
        private fun getBlendMethod(parameter: ACubismJsonValue): ExpressionBlendType {
            val method = parameter.get(ExpressionKey.BLEND.key).string

            if (method == ExpressionBlendType.ADD.type) {
                return ExpressionBlendType.ADD
            } else if (method == ExpressionBlendType.MULTIPLY.type) {
                return ExpressionBlendType.MULTIPLY
            } else if (method == ExpressionBlendType.OVERWRITE.type) {
                return ExpressionBlendType.OVERWRITE
            } else {
                return ExpressionBlendType.ADD
            }
        }
    }
}
