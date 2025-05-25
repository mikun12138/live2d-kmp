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
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogWarning

class CubismExpressionMotionManager : CubismMotionQueueManager() {
    data class ExpressionParameterValue(
        val parameterId: CubismId,
        var additiveValue: Float = 0f,
        var multiplyValue: Float = 0f,
        var overwriteValue: Float = 0f,
    )

    fun getFadeWeight(index: Int): Float {
        require(!fadeWeights.isEmpty()) { "No motion during playback." }

        require(!(fadeWeights.size <= index || index < 0)) { "The index is an invalid value." }

        return fadeWeights[index]!!
    }

    /**
     * 優先度を設定して表情モーションを開始する。
     *
     * @param motion 開始する表情モーション
     * @param priority 優先度
     * @return 開始した表情モーションの識別番号。個別のモーションが終了したか否かを判断するisFinished()の引数で使用する。開始できないときは「-1」を返します。
     */
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
        var isUpdated = false
        val motions: MutableList<CubismMotionQueueEntry> = getCubismMotionQueueEntries()

        var expressionWeight = 0.0f
        var expressionIndex = 0

        // motionQueueEntryの中にあるmotionインスタンスがnullの場合、motionQueueEntryインスタンス自体をnullにする
        // for文でnullを順次削除する方式だと例外を出してしまうため。
        for (i in motions.indices) {
            val entry: CubismMotionQueueEntry = motions.get(i)
            val expressionMotion: CubismExpressionMotion? =
                entry.getCubismMotion() as CubismExpressionMotion?

            if (expressionMotion == null) {
                motions.set(i, null)
            }
        }

        // 予めnull要素を全て削除
        motions.removeAll(nullSet)

        while (fadeWeights.size < motions.size) {
            fadeWeights.add(0.0f)
        }

        // ------ 処理を行う ------
        // 既に表情モーションがあれば終了フラグを立てる
        for (i in motions.indices) {
            val motionQueueEntry: CubismMotionQueueEntry = motions.get(i)
            val expressionMotion: CubismExpressionMotion =
                motionQueueEntry.getCubismMotion() as CubismExpressionMotion
            val expressionParameters: MutableList<CubismExpressionMotion.ExpressionParameter?> =
                expressionMotion.getExpressionParameters()

            if (motionQueueEntry.isAvailable()) {
                // 再生中のExpressionが参照しているパラメータをすべてリストアップ
                for (paramIndex in expressionParameters.indices) {
                    if (expressionParameters.get(paramIndex).parameterId == null) {
                        continue
                    }

                    var index = -1
                    // リストにパラメータIDが存在するか検索
                    for (j in expressionParameterValues.indices) {
                        if (expressionParameterValues.get(j).parameterId !== expressionParameters.get(
                                paramIndex
                            ).parameterId
                        ) {
                            continue
                        }

                        index = j
                        break
                    }

                    if (index >= 0) {
                        continue
                    }

                    // パラメータがリストに存在しないなら新規追加
                    val item = ExpressionParameterValue(
                        parameterId = expressionParameters.get(paramIndex).parameterId,
                        additiveValue = CubismExpressionMotion.DEFAULT_ADDITIVE_VALUE,
                        multiplyValue = CubismExpressionMotion.DEFAULT_MULTIPLY_VALUE,
                        overwriteValue = model.getParameterValue(item.parameterId!!),
                    )
                    expressionParameterValues.add(item)
                }
            }

            // ------ 値を計算する ------
            expressionMotion.setupMotionQueueEntry(motionQueueEntry, totalSeconds)
            setFadeWeight(
                expressionIndex,
                expressionMotion.updateFadeWeight(motionQueueEntry, totalSeconds)
            )
            expressionMotion.calculateExpressionParameters(
                model,
                totalSeconds,
                motionQueueEntry,
                expressionParameterValues,
                expressionIndex,
                getFadeWeight(expressionIndex)
            )

            val easingSine = if (expressionMotion.getFadeInTime() === 0.0f)
                1.0f
            else
                getEasingSine((totalSeconds - motionQueueEntry.getFadeInStartTime()) / expressionMotion.getFadeInTime())
            expressionWeight += easingSine

            isUpdated = true

            if (motionQueueEntry.isTriggeredFadeOut()) {
                // フェードアウト開始
                motionQueueEntry.startFadeOut(motionQueueEntry.getFadeOutSeconds(), totalSeconds)
            }

            expressionIndex++
        }

        // ------ 最新のExpressionのフェードが完了していればそれ以前を削除する ------
        if (motions.size > 1) {
            val latestFadeWeight = getFadeWeight(fadeWeights.size - 1)

            if (latestFadeWeight >= 1.0f) {
                // 配列の最後の要素は削除しない
                for (i in motions.size - 2 downTo 0) {
                    // forでremoveすることはできない。nullをセットしておいて後で削除する。
                    motions.set(i, null)

                    fadeWeights.removeAt(i)
                }
                motions.removeAll(nullSet)
            }
        }

        if (expressionWeight > 1.0f) {
            expressionWeight = 1.0f
        }

        // モデルに各値を適用
        for (i in expressionParameterValues.indices) {
            val v = expressionParameterValues.get(i)

            model.setParameterValue(
                v.parameterId!!,
                (v.overwriteValue + v.additiveValue) * v.multiplyValue,
                expressionWeight
            )
            v.additiveValue = CubismExpressionMotion.DEFAULT_ADDITIVE_VALUE
            v.multiplyValue = CubismExpressionMotion.DEFAULT_MULTIPLY_VALUE
        }

        return isUpdated
    }

    /**
     * Set the weight of expression fade.
     *
     * @param index index of the expression motion to be set
     * @param expressionFadeWeight weight value of expression fade
     */
    private fun setFadeWeight(index: Int, expressionFadeWeight: Float) {
        if (index < 0 || fadeWeights.isEmpty() || fadeWeights.size <= index) {
            cubismLogWarning("Failed to set the fade weight value. The element at that index does not exist.")
            return
        }
        fadeWeights.set(index, expressionFadeWeight)
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

    /**
     * 再生中の表情モーションのウェイトのリスト
     */
    private val fadeWeights: MutableList<Float> = mutableListOf()

    companion object {
        // nullが格納されたSet。null要素だけListから排除する際に使用される。
        private val nullSet = mutableSetOf<Any?>(null)
    }
}
