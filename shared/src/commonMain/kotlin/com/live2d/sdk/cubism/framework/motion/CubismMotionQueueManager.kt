/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

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
    fun startMotion(motion: ACubismMotion) {

        // 既にモーションがあれば終了フラグを立てる。
        for (entry in motionEntries) {
            entry.setFadeOut()
        }

        motionEntries.add(CubismMotionQueueEntry(motion))

        // began callback
        motion.beganMotionCallback(motion)
    }

    val isFinished: Boolean
        get() {
            // ---- Do processing ----
            // If there is already a motion, flag it as finished.

            // motionがnullならば要素をnullとする
            // 後でnull要素を全て削除する。
            for (motionQueueEntry in motionEntries) {
                if (!motionQueueEntry.isFinished) {
                    return false
                }
            }

            motionEntries.removeAll(mutableSetOf<Any?>(null))

            return true
        }

    /**
     * Stop all motions.
     */
    fun stopAllMotions() {
        motionEntries.clear()
    }

    fun setEventCallback(callback: ICubismMotionEventFunction, customData: Any?) {
        eventCallback = callback
        eventCustomData = customData
    }


    /**
     * total delta time[s]
     */
    var totalSeconds: Float = 0f
    var lastTotalSeconds: Float = 0f



    /**
     * List of motions
     */
    val motionEntries: MutableList<CubismMotionQueueEntry?> = ArrayList()

    private var eventCallback: ICubismMotionEventFunction? = null
    private var eventCustomData: Any? = null

}
