/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.model.CubismModel

/**
 * Class for managing motion playback, used to play ACubismMotion subclasses such as CubismMotion motion.
 * * If another motion do startMotion() during playback, it will smoothly change to the new motion and the old motion will be suspended.
 * * Use multiple CubismMotionManager instances to play multiple motions at the same time, such as when motions for facial expressions and body motions are made separately.
 */
class CubismMotionManager : CubismMotionQueueManager() {
    fun startMotionPriority(motion: ACubismMotion, priority: Int) {
        if (priority == reservationPriority) {
            reservationPriority = 0 // Cancel the reservation.
        }

        // Set priority of the motion during playback.
        currentPriority = priority

        startMotion(motion)

    }

    fun updateMotion(model: CubismModel, deltaTimeSeconds: Float): Boolean {
        totalSeconds += deltaTimeSeconds
        val isUpdated = !motionEntries.isEmpty()

        motionEntries.forEachIndexed { index, entry ->

            if (entry.state.inInit()) {
                entry.setup()
                return@forEachIndexed
            }

            if (entry.state.inActive()) {
                val fadeWeight =
                    entry.motion.updateFadeWeight(entry, totalSeconds)
                //---- 全てのパラメータIDをループする ----
                // TODO:: level 0, change to CubismMotionQueueEntry.doUpdateParameters
                entry.motion.doUpdateParameters(
                    model,
                    totalSeconds,
                    fadeWeight,
                    entry
                )

                if (entry.isTriggeredFadeOut) {
                    entry.startFadeOut(
                        entry.motion.fadeOutSeconds,
                        totalSeconds
                    )
                }

                // 触发 UserData 内的 event
                run {
                    entry.motion.getFiredEvent(
                        entry.lastTotalSeconds - entry.startTimePoint,
                        totalSeconds - entry.startTimePoint
                    ).forEach { event ->
                        eventCallback.apply(this, event, eventCustomData)
                    }
                    entry.lastTotalSeconds = totalSeconds
                }

                // 後処理
                // 終了時刻を過ぎたら終了フラグを立てる（CubismMotionQueueManager）
                if (entry.endTimePoint > 0.0f && entry.endTimePoint < totalSeconds) {
                    entry.state = CubismMotionQueueEntry.State.End // 終了
                }
                return@forEachIndexed
            }


            if (entry.state.inEnd()) {
                motionEntries[index] = null
            }

        }

        motionEntries.removeAll(mutableSetOf<Any?>(null))

        if (isFinished) {
            currentPriority = 0 // 再生中モーションの優先度を解除
        }

        return isUpdated
    }

    /**
     * Reserve motion with a priority.
     *
     *
     * If the given priority is lower than the already existing reserved priority and the priority of the current motion, it is not reserved and "false" is returned.
     *
     *
     * @param priority motion's priority
     * @return If reserving the motion is successful, return true.
     */
    fun reserveMotion(priority: Int): Boolean {
        if (priority <= reservationPriority || priority <= currentPriority) {
            return false
        }
        reservationPriority = priority

        return true
    }

    /**
     * Priority of the currently playing motion.
     * 当前播放的 motion 的优先级
     */
    var currentPriority: Int = 0
        private set

    /**
     * Priority of the motion to be played. The value becomes 0 during playback. This is function for loading motion files in a separate thread.
     * 将要播放的 motion 的优先级
     */
    var reservationPriority: Int = 0
}
