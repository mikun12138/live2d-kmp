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
class CubismMotionQueueManager {
    /**
     * 引数で指定したモーションを再生する。同じタイプのモーションが既にある場合は、既存のモーションに終了フラグを立て、フェードアウトを開始する。
     *
     * @param motion 開始するモーション
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判定するisFinished()の引数として使用する。開始できない場合は「-1」を返す。
     */
    fun startMotion(motion: ACubismMotion?): Int {
        if (motion == null) {
            return -1
        }

        // 既にモーションがあれば終了フラグを立てる。
        for (i in motions.indices) {
            val entry: CubismMotionQueueEntry? = motions.get(i)

            if (entry == null) {
                continue
            }
            entry.setFadeOut(entry.getMotion().getFadeOutTime())
        }

        val motionQueueEntry: CubismMotionQueueEntry = CubismMotionQueueEntry()
        motionQueueEntry.setMotion(motion)

        motions.add(motionQueueEntry)

        if (motion.onBeganMotion != null) {
            motion.onBeganMotion.execute(motion)
        }

        return System.identityHashCode(motionQueueEntry)
    }

    /**
     * Start the specified motion. If there is already a motion of the same type, set the end flag for the existing motion and start fading out.
     *
     * @param motion motion to start
     * @param userTimeSeconds total user time[s]
     * @return Returns the identification number(OptionalInt) of the motion that has started. Used as an argument for isFinished(), which judges whether an individual motion has been completed. When it cannot be started, it returns an empty OptionalInt.
     *
     * @see .startMotion
     */
    @Deprecated(
        """第2引数userTimeSecondsを関数内で使用していないため非推奨。startMotion(ACubismMotion motion)を使用してください。
      """
    )
    fun startMotion(motion: ACubismMotion?, userTimeSeconds: Float): Int {
        if (motion == null) {
            return -1
        }

        // If there is already motion, flag it as finished.
        for (i in motions.indices) {
            val entry: CubismMotionQueueEntry? = motions.get(i)

            if (entry == null) {
                continue
            }
            entry.setFadeOut(entry.getMotion().getFadeOutTime())
        }

        val motionQueueEntry: CubismMotionQueueEntry = CubismMotionQueueEntry()
        motionQueueEntry.setMotion(motion)

        motions.add(motionQueueEntry)

        return System.identityHashCode(motionQueueEntry)
    }

    val isFinished: Boolean
        get() {
            // ---- Do processing ----
            // If there is already a motion, flag it as finished.

            // At first, remove the null elements from motions list.

            motions.removeAll(nullSet)

            // motionがnullならば要素をnullとする
            // 後でnull要素を全て削除する。
            for (i in motions.indices) {
                val motionQueueEntry: CubismMotionQueueEntry = motions.get(i)
                val motion: ACubismMotion? = motionQueueEntry.getMotion()

                if (motion == null) {
                    motions.set(i, null)
                    continue
                }

                if (!motionQueueEntry.isFinished()) {
                    return false
                }
            }

            motions.removeAll(nullSet)

            return true
        }

    fun isFinished(motionQueueEntryNumber: Int): Boolean {
        // ---- Do processing ----
        // If there is already a motion, flag it as finished.
        for (i in motions.indices) {
            val motionQueueEntry: CubismMotionQueueEntry? = motions.get(i)

            if (motionQueueEntry == null) {
                continue
            }

            if (System.identityHashCode(motionQueueEntry) == motionQueueEntryNumber && !motionQueueEntry.isFinished()) {
                return false
            }
        }
        return true
    }

    /**
     * Stop all motions.
     */
    fun stopAllMotions() {
        motions.clear()
    }

    /**
     * Get the specified CubismMotionQueueEntry instance.
     *
     * @param motionQueueEntryNumber identification number of the motion
     * @return specified CubismMotionQueueEntry object. If not found, empty Optional is returned.
     */
    fun getCubismMotionQueueEntry(motionQueueEntryNumber: Int): CubismMotionQueueEntry? {
        // ---- Do processing ----
        // If there is already a motion, flag it as finished.
        for (i in motions.indices) {
            val motionQueueEntry: CubismMotionQueueEntry? = motions.get(i)

            if (motionQueueEntry == null) {
                continue
            }

            if (System.identityHashCode(motionQueueEntry) == motionQueueEntryNumber) {
                return motionQueueEntry
            }
        }
        return null
    }

    val cubismMotionQueueEntries: MutableList<CubismMotionQueueEntry>
        /**
         * CubismMotionQueueEntryのリストを取得する。
         *
         * @return CubismMotionQueueEntryのリスト
         */
        get() = motions

    /**
     * Register the callback function to receive events.
     *
     * @param callback callback function
     * @param customData data to be given to callback
     */
    fun setEventCallback(callback: ICubismMotionEventFunction, customData: Any?) {
        eventCallback = callback
        eventCustomData = customData
    }

    /**
     * Update the motion and reflect the parameter values to the model.
     *
     * @param model target model
     * @param userTimeSeconds total delta time[s]
     * @return If reflecting the parameter value to the model(the motion is changed.) is successed, return true.
     */
    protected fun doUpdateMotion(model: CubismModel?, userTimeSeconds: Float): Boolean {
        var isUpdated = false

        // ---- Do processing ----
        // If there is already a motion, flag it as finished.

        // At first, remove the null elements from motions list.
        motions.removeAll(nullSet)

        for (i in motions.indices) {
            val motionQueueEntry: CubismMotionQueueEntry = motions.get(i)
            val motion: ACubismMotion? = motionQueueEntry.getMotion()

            if (motion == null) {
                motions.set(i, null)
                continue
            }

            motion.updateParameters(model, motionQueueEntry, userTimeSeconds)
            isUpdated = true

            // Inspect user-triggered events.
            val firedList: MutableList<String?> = motion.getFiredEvent(
                motionQueueEntry.getLastCheckEventTime() - motionQueueEntry.getStartTime(),
                userTimeSeconds - motionQueueEntry.getStartTime()
            )

            for (j in firedList.indices) {
                val event = firedList.get(j)
                eventCallback.apply(this, event, eventCustomData)
            }
            motionQueueEntry.setLastCheckEventTime(userTimeSeconds)

            // If any processes have already been finished, delete them.
            if (motionQueueEntry.isFinished()) {
                motions.set(i, null)
            } else {
                if (motionQueueEntry.isTriggeredFadeOut()) {
                    motionQueueEntry.startFadeOut(
                        motionQueueEntry.getFadeOutSeconds(),
                        userTimeSeconds
                    )
                }
            }
        }

        motions.removeAll(nullSet)

        return isUpdated
    }

    /**
     * total delta time[s]
     */
    protected var userTimeSeconds: Float = 0f

    /**
     * List of motions
     */
    private val motions: MutableList<CubismMotionQueueEntry> = ArrayList<CubismMotionQueueEntry>()

    /**
     * Callback function
     */
    private var eventCallback: ICubismMotionEventFunction? = null

    /**
     * Data to be given to the callback
     */
    private var eventCustomData: Any? = null

    // nullが格納されたSet. null要素だけListから排除する際に使用される。
    private val nullSet = mutableSetOf<Any?>(null)
}
