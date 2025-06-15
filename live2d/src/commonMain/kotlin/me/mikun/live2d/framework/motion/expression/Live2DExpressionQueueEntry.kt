package me.mikun.live2d.framework.motion.expression

import me.mikun.live2d.framework.model.Live2DModel
import me.mikun.live2d.framework.motion.ALive2DMotionQueueEntry
import me.mikun.live2d.framework.motion.expression.Live2DExpressionMotion.Companion.DEFAULT_ADDITIVE_VALUE
import me.mikun.live2d.framework.motion.expression.Live2DExpressionMotion.Companion.DEFAULT_MULTIPLY_VALUE
import me.mikun.live2d.framework.motion.expression.Live2DExpressionMotion.ExpressionBlendType

class Live2DExpressionQueueEntry(
    override val manager: Live2DExpressionManager,
    override val motion: Live2DExpressionMotion
): ALive2DMotionQueueEntry(
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