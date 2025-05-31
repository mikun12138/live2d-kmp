/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.model.Model
import com.live2d.sdk.cubism.util.switchStateTo

/**
 * The manager class for playing motions. This is used to play ACubismMotion's subclasses such as CubismMotion's motion.
 *
 *
 * If another motion is done "StartMotion()" during playback, the motion changes smoothly to the new motion and the old motion is interrupted. When multiple motions are played back simultaneously (For example, separate motions for facial expressions, body motions, etc.), multiple CubismMotionQueueManager instances are used.
 */
abstract class AMotionManager(
    open val motionEntries: MutableList<out AMotionQueueEntry> = mutableListOf(),
) {

    fun startMotionPriority(motion: ACubismMotion, priority: Int) {
        if (priority == reservePriority) {
            reservePriority = 0 // 予約を解除
        }
        currentPriority = priority // 再生中モーションの優先度を設定

        startMotion(motion)
    }

    /**
     * 引数で指定したモーションを再生する。同じタイプのモーションが既にある場合は、既存のモーションに終了フラグを立て、フェードアウトを開始する。
     *
     * @param motion 開始するモーション
     * @return 開始したモーションの識別番号を返す。個別のモーションが終了したか否かを判定するisFinished()の引数として使用する。開始できない場合は「-1」を返す。
     */
    fun startMotion(motion: ACubismMotion) {
        doStartMotion(motion)
    }

    protected abstract fun doStartMotion(motion: ACubismMotion)

    fun updateMotion(model: Model, deltaTimeSeconds: Float): Boolean {
        totalSeconds += deltaTimeSeconds
        val isUpdated = !motionEntries.isEmpty()

        motionEntries.lastOrNull()?.takeIf { it.state.inInit() }?.let {
            motionEntries.dropLast(1).forEach { entry ->
                entry switchStateTo AMotionQueueEntry.State.FadeOut
            }
        }

        run {
            doUpdateMotion(model, deltaTimeSeconds)
        }

        motionEntries.removeIf { it.state.inEnd() }


        return isUpdated

    }

    protected abstract fun doUpdateMotion(model: Model, deltaTimeSeconds: Float)


    val isFinished: Boolean
        get() = motionEntries.isEmpty()

    /**
     * Stop all motions.
     */
    fun stopAllMotions() {
        motionEntries.clear()
    }

    /**
     * total delta time[s]
     */
    var totalSeconds: Float = 0f
    var lastTotalSeconds: Float = 0f

    /**
     * Priority of the currently playing motion.
     * 当前播放的 motion 的优先级
     */
    var currentPriority: Int = 0
        get() = if (isFinished)
            0
        else
            field

    /**
     * Priority of the motion to be played. The value becomes 0 during playback. This is function for loading motion files in a separate thread.
     * 将要播放的 motion 的优先级
     */
    var reservePriority: Int = 0
        get() = if (isFinished)
            0
        else
            field


}
