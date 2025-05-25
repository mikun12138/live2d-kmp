/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.model.CubismModel

/**
 * The manager class for playing motions. This is used to play ACubismMotion's subclasses such as CubismMotion's motion.
 *
 *
 * If another motion is done "StartMotion()" during playback, the motion changes smoothly to the new motion and the old motion is interrupted. When multiple motions are played back simultaneously (For example, separate motions for facial expressions, body motions, etc.), multiple CubismMotionQueueManager instances are used.
 */
open class CubismMotionQueueManager {
    /**
     * 引数で指定したモーションを再生する。同じタイプのモーションが既にある場合は、既存のモーションに終了フラグを立て、フェードアウトを開始する。
     *
     * @param motion 開始するモーション
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判定するisFinished()の引数として使用する。開始できない場合は「-1」を返す。
     */
    fun startMotion(motion: CubismMotion) {

        // 既にモーションがあれば終了フラグを立てる。
        for (entry in motions) {
            entry.setFadeOut(entry.motion.fadeOutSeconds)
        }

        motions.add(CubismMotionQueueEntry(motion))

        // began callback
        motion.beganMotionCallback(motion)
    }

    val isFinished: Boolean
        get() {
            // ---- Do processing ----
            // If there is already a motion, flag it as finished.

            // motionがnullならば要素をnullとする
            // 後でnull要素を全て削除する。
            for (motionQueueEntry in motions) {
                if (!motionQueueEntry.isFinished) {
                    return false
                }
            }

            motions.removeAll(mutableSetOf<Any?>(null))

            return true
        }

    /**
     * Stop all motions.
     */
    fun stopAllMotions() {
        motions.clear()
    }

    fun setEventCallback(callback: ICubismMotionEventFunction, customData: Any?) {
        eventCallback = callback
        eventCustomData = customData
    }

    /**
     * Update the motion and reflect the parameter values to the model.
     *
     * @param model target model
     * @param totalSeconds total delta time[s]
     * @return If reflecting the parameter value to the model(the motion is changed.) is successed, return true.
     */
    protected fun doUpdateMotion(model: CubismModel?, totalSeconds: Float): Boolean {
        // TODO:: make it a member
        var isUpdated = false

        // ---- Do processing ----
        // If there is already a motion, flag it as finished.

        // At first, remove the null elements from motions list.
        motions.removeAll(mutableSetOf<Any?>(null))

        for (i in motions.indices) {
            isUpdated = true
            val motionQueueEntry = motions[i]
            val motion: CubismMotion = motionQueueEntry.motion

            // 更新 model 参数
            run {
                motion.updateParameters(model, motionQueueEntry, totalSeconds)
            }

            // 触发 UserData 内的 event
            run {
                motion.getFiredEvent(
                    motionQueueEntry.lastCheckEventTime - motionQueueEntry.startTime,
                    totalSeconds - motionQueueEntry.startTime
                ).forEach { event ->
                    eventCallback.apply(this, event, eventCustomData)
                }
                motionQueueEntry.lastCheckEventTime = totalSeconds
            }

            // TODO::是不是该写到 entry ?
            run {
                // If any processes have already been finished, delete them.
                if (motionQueueEntry.isFinished) {
                    motions[i] = null
                } else {
                    if (motionQueueEntry.isTriggeredFadeOut) {
                        motionQueueEntry.startFadeOut(
                            motionQueueEntry.fadeOutSeconds,
                            totalSeconds
                        )
                    }
                }
            }
        }

        motions.removeAll(mutableSetOf<Any?>(null))

        return isUpdated
    }

    /**
     * total delta time[s]
     */
    protected var totalSeconds: Float = 0f

    /**
     * List of motions
     */
    val motions: MutableList<CubismMotionQueueEntry?> = ArrayList()

    private var eventCallback: ICubismMotionEventFunction? = null
    private var eventCustomData: Any? = null

}
