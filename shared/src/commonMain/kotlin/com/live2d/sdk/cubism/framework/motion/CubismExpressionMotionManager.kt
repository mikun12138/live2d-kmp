/**
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at https://www.live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.math.CubismMath.getEasingSine
import com.live2d.sdk.cubism.framework.model.CubismModel
import kotlin.math.min

class CubismExpressionMotionManager : CubismMotionQueueManager() {
    data class ExpressionParameterValue(
        val parameterId: CubismId,
        var additiveValue: Float = 0f,
        var multiplyValue: Float = 0f,
        var overwriteValue: Float = 0f,
    )

    fun startMotionPriority(motion: ACubismMotion, priority: Int) {
        if (priority == reservePriority) {
            reservePriority = 0 // 予約を解除
        }
        currentPriority = priority // 再生中モーションの優先度を設定

        startMotion(motion)
    }

    /**
     * 表情モーションを更新して、モデルにパラメータ値を反映する。
     *
     * @param model 対象のモデル
     * @param deltaTimeSeconds デルタ時間[秒]
     * @return 表情モーションが更新されたかどうか。更新されたならtrue。
     */
    fun updateMotion(model: CubismModel, deltaTimeSeconds: Float): Boolean {
        totalSeconds += deltaTimeSeconds

        // 予めnull要素を全て削除
        motionEntries.removeAll(nullSet)
        val isUpdated = !motionEntries.isEmpty()

        var expressionWeight = 0.0f

        // ------ 処理を行う ------
        // 既に表情モーションがあれば終了フラグを立てる
        motionEntries.forEachIndexed { index, entry ->

            val motion = entry.motion as CubismExpressionMotion

            // 再生中のExpressionが参照しているパラメータをすべてリストアップ
            for (parameter in motion.parameters) {
                expressionParameterValues.find { it.parameterId == parameter.parameterId }
                    ?: run {
                        // パラメータがリストに存在しないなら新規追加
                        val item = ExpressionParameterValue(
                            parameterId = parameter.parameterId,
                            additiveValue = CubismExpressionMotion.DEFAULT_ADDITIVE_VALUE,
                            multiplyValue = CubismExpressionMotion.DEFAULT_MULTIPLY_VALUE,
                            overwriteValue = model.getParameterValue(parameter.parameterId),
                        )
                        expressionParameterValues.add(item)
                    }
            }

            // ------ 値を計算する ------
            // TODO:: ??? 循环里放初始化
            motion.setupMotionQueueEntry(entry, totalSeconds)

            entry.fadeWeight = motion.updateFadeWeight(entry, totalSeconds)

            motion.calculateExpressionParameters(
                model,
                expressionParameterValues,
                index == 0,
                entry.fadeWeight
            )

            val easingSine = if (motion.fadeInSeconds <= 0.0f)
                1.0f
            else
                getEasingSine((totalSeconds - entry.fadeInStartTime) / motion.fadeInSeconds)
            expressionWeight += easingSine


            run {
                if (entry.isTriggeredFadeOut) {
                    // フェードアウト開始
                    entry.startFadeOut(
                        entry.fadeOutSeconds, totalSeconds
                    )
                }
            }
        }

        // 若最新的 motion 完全淡入 则删除前面的所有motion
        if (!motionEntries.isEmpty()) {
            if (motionEntries.last().fadeWeight >= 1.0f) {
                motionEntries.subList(0, motionEntries.lastIndex).clear()
            }
        }

        // 将值应用于 model
        for (value in expressionParameterValues) {

            model.setParameterValue(
                value.parameterId,
                (value.overwriteValue + value.additiveValue) * value.multiplyValue, // 先加算后乘算
                min(expressionWeight, 1.0f)
            )
            value.additiveValue = CubismExpressionMotion.DEFAULT_ADDITIVE_VALUE
            value.multiplyValue = CubismExpressionMotion.DEFAULT_MULTIPLY_VALUE
        }

        return isUpdated
    }

    /**
     * モデルに適用する各パラメータの値
     */
    private val expressionParameterValues: MutableList<ExpressionParameterValue> =
        ArrayList<ExpressionParameterValue>()

    /**
     * 現在再生中の表情モーションの優先度
     */
    var currentPriority: Int = 0
        private set

    /**
     * 再生予定の表情モーションの優先度。再生中は0になる。
     * 表情モーションファイルを別スレッドで読み込むときの機能。
     */
    var reservePriority: Int = 0

    companion object {
        // nullが格納されたSet。null要素だけListから排除する際に使用される。
        private val nullSet = mutableSetOf<Any?>(null)
    }
}
