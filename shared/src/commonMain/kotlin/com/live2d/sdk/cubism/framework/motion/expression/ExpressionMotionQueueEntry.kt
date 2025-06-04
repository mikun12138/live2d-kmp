package com.live2d.sdk.cubism.framework.motion.expression

import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.motion.AMotionQueueEntry
import com.live2d.sdk.cubism.framework.motion.expression.CubismExpressionMotion.Companion.DEFAULT_ADDITIVE_VALUE
import com.live2d.sdk.cubism.framework.motion.expression.CubismExpressionMotion.Companion.DEFAULT_MULTIPLY_VALUE
import com.live2d.sdk.cubism.framework.motion.expression.CubismExpressionMotion.ExpressionBlendType

class ExpressionMotionQueueEntry(
    override val manager: CubismExpressionMotionManager,
    override val motion: CubismExpressionMotion
): AMotionQueueEntry(
    manager,
    motion
) {

    override fun doInit() {
        /*
            Do nothing others
         */
    }

    fun updateParameter(
        model: Live2DModel,
        totalSeconds: Float,
    ) {
        val fadeWeight = calFadeWeight(totalSeconds)
        val isFirstEntry = manager.motionEntries[0] === this

        manager.expressionParameterValues.forEach { parameterValue ->

            parameterValue.overwriteValue = model.getParameterValue(parameterValue.parameterId)

            motion.parameters.find { it.parameterId == parameterValue.parameterId }
                ?.let {

                    // 値を計算
                    val newAdditiveValue: Float
                    val newMultiplyValue: Float
                    val newOverwriteValue: Float

                    when (it.blendType) {
                        ExpressionBlendType.ADD -> {
                            newAdditiveValue = it.value
                            newMultiplyValue = DEFAULT_MULTIPLY_VALUE
                            newOverwriteValue = parameterValue.overwriteValue
                        }

                        ExpressionBlendType.MULTIPLY -> {
                            newAdditiveValue = DEFAULT_ADDITIVE_VALUE
                            newMultiplyValue = it.value
                            newOverwriteValue = parameterValue.overwriteValue
                        }

                        ExpressionBlendType.OVERWRITE -> {
                            newAdditiveValue = DEFAULT_ADDITIVE_VALUE
                            newMultiplyValue = DEFAULT_MULTIPLY_VALUE
                            newOverwriteValue = it.value
                        }
                    }

                    if (isFirstEntry) {
                        parameterValue.additiveValue = newAdditiveValue
                        parameterValue.multiplyValue = newMultiplyValue
                        parameterValue.overwriteValue = newOverwriteValue
                    } else {
                        parameterValue.additiveValue =
                            (parameterValue.additiveValue * (1.0f - fadeWeight)) + newAdditiveValue * fadeWeight
                        parameterValue.multiplyValue =
                            (parameterValue.multiplyValue * (1.0f - fadeWeight)) + newMultiplyValue * fadeWeight
                        parameterValue.overwriteValue =
                            (parameterValue.overwriteValue * (1.0f - fadeWeight)) + newOverwriteValue * fadeWeight
                    }
                } ?: run {
                // 再生中のExpressionが参照していないパラメータは初期値を適用
                if (isFirstEntry) {
                    parameterValue.additiveValue = DEFAULT_ADDITIVE_VALUE
                    parameterValue.multiplyValue = DEFAULT_MULTIPLY_VALUE
                } else {
                    parameterValue.additiveValue = calculateValue(
                        parameterValue.additiveValue,
                        DEFAULT_ADDITIVE_VALUE,
                        fadeWeight
                    )
                    parameterValue.multiplyValue = calculateValue(
                        parameterValue.multiplyValue,
                        DEFAULT_MULTIPLY_VALUE,
                        fadeWeight
                    )
                    parameterValue.overwriteValue = calculateValue(
                        parameterValue.overwriteValue,
                        parameterValue.overwriteValue,
                        fadeWeight
                    )
                }
            }
        }
    }

    private fun calculateValue(source: Float, destination: Float, fadeWeight: Float): Float {
        return (source * (1.0f - fadeWeight)) + (destination * fadeWeight)
    }
}