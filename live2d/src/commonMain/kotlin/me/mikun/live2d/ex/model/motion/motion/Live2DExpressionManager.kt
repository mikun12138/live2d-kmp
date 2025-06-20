/**
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at https://www.live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.ex.model.motion.motion

import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.framework.math.CubismMath.getEasingSine
import me.mikun.live2d.framework.model.Live2DModel
import me.mikun.live2d.framework.motion.ALive2DMotion
import me.mikun.live2d.ex.model.motion.ALive2DMotionManager
import me.mikun.live2d.ex.model.motion.ALive2DMotionQueueEntry
import me.mikun.live2d.framework.motion.expression.Live2DExpressionMotion
import me.mikun.live2d.framework.utils.switchStateTo
import kotlin.math.min

class Live2DExpressionManager(
    override val motionEntries: MutableList<Live2DExpressionQueueEntry> = mutableListOf()
) : ALive2DMotionManager() {
    data class ExpressionParameterValue(
        val parameterId: Live2DId,
        var additiveValue: Float = 0f,
        var multiplyValue: Float = 0f,
        var overwriteValue: Float = 0f,
    )


    override fun doStartMotion(motion: ALive2DMotion) {
        check(motion is Live2DExpressionMotion)
        motionEntries.add(Live2DExpressionQueueEntry(this, motion))
    }

    override fun doUpdateMotion(model: Live2DModel, deltaTimeSeconds: Float) {

        var expressionWeight = 0.0f

        // ------ 処理を行う ------
        // 既に表情モーションがあれば終了フラグを立てる
        motionEntries.forEachIndexed { index, entry ->

            /*
                init
             */
            if (entry.state.inInit()) {

                // 再生中のExpressionが参照しているパラメータをすべてリストアップ
                for (parameter in (entry.motion as Live2DExpressionMotion).parameters) {
                    expressionParameterValues.find { it.parameterId == parameter.parameterId }
                        ?: run {
                            // パラメータがリストに存在しないなら新規追加
                            val item = ExpressionParameterValue(
                                parameterId = parameter.parameterId,
                                additiveValue = Live2DExpressionMotion.Companion.DEFAULT_ADDITIVE_VALUE,
                                multiplyValue = Live2DExpressionMotion.Companion.DEFAULT_MULTIPLY_VALUE,
                                overwriteValue = model.getParameterValue(parameter.parameterId),
                            )
                            expressionParameterValues.add(item)
                        }
                }

                entry.init(totalSeconds)
            }

            /*
                update
             */

            if (entry.state.inActive()) {

                entry.updateParameter(
                    model,
                    totalSeconds
                )

                val easingSine = if (entry.motion.fadeInSeconds <= 0.0f)
                    1.0f
                else
                    getEasingSine((totalSeconds - entry.startTimePoint) / entry.motion.fadeInSeconds)
                expressionWeight += easingSine

            }
        }

        applyParameterValues(model, expressionWeight)

        // 若最新的 motion 完全淡入 则删除前面的所有motion
        if (!motionEntries.isEmpty()) {
            if (motionEntries.last().calFadeWeight(totalSeconds) >= 1.0f) {
                motionEntries.subList(0, motionEntries.lastIndex).forEach { entry ->
                    entry switchStateTo ALive2DMotionQueueEntry.State.End
                }
            }
        }

    }

    private fun applyParameterValues(
        model: Live2DModel, expressionWeight: Float
    ) {
        // 将值应用于 model
        for (value in expressionParameterValues) {
            model.setParameterValue(
                value.parameterId,
                (value.overwriteValue + value.additiveValue) * value.multiplyValue, // 先加算后乘算
                min(expressionWeight, 1.0f)
            )
            value.additiveValue = Live2DExpressionMotion.Companion.DEFAULT_ADDITIVE_VALUE
            value.multiplyValue = Live2DExpressionMotion.Companion.DEFAULT_MULTIPLY_VALUE
        }
    }

    /**
     * モデルに適用する各パラメータの値
     */
    val expressionParameterValues: MutableList<ExpressionParameterValue> = mutableListOf()
}
