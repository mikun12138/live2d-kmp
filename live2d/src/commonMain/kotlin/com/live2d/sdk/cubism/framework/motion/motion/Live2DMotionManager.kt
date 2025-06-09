package com.live2d.sdk.cubism.framework.motion.motion

import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.motion.ALive2DMotion
import com.live2d.sdk.cubism.framework.motion.ALive2DMotionManager
import com.live2d.sdk.cubism.framework.motion.ALive2DMotionQueueEntry

/**
 * Class for managing motion playback, used to play ACubismMotion subclasses such as CubismMotion motion.
 * * If another motion do startMotion() during playback, it will smoothly change to the new motion and the old motion will be suspended.
 * * Use multiple CubismMotionManager instances to play multiple motions at the same time, such as when motions for facial expressions and body motions are made separately.
 */
class Live2DMotionManager (
    override val motionEntries: MutableList<Live2DMotionQueueEntry> = mutableListOf()
) : ALive2DMotionManager() {

    override fun doStartMotion(motion: ALive2DMotion) {
        check(motion is Live2DMotion)
        motionEntries.add(Live2DMotionQueueEntry(this, motion))
    }

    override fun doUpdateMotion(model: Live2DModel, deltaTimeSeconds: Float) {

        motionEntries.forEachIndexed { index, entry ->

            if (entry.state.inInit()) {
                entry.init(totalSeconds)
                return@forEachIndexed
            }

            if (entry.state.inActive()) {
                //---- 全てのパラメータIDをループする ----
                entry.doUpdateParameters(
                    model,
                    totalSeconds,
                )

                // TODO:: level: 114514
//                // 触发 UserData 内的 event
//                run {
//                    entry.firedEvents.forEach { event ->
//                        eventCallback.apply(this, event, eventCustomData)
//                    }
//                    lastTotalSeconds = totalSeconds
//                }

                // 後処理
                // 終了時刻を過ぎたら終了フラグを立てる（CubismMotionQueueManager）
                if (entry.endTimePoint > 0.0f && entry.endTimePoint < totalSeconds) {
                    entry.state = ALive2DMotionQueueEntry.State.End // 終了
                }
                return@forEachIndexed
            }
        }
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
        if (priority <= reservePriority || priority <= currentPriority) {
            return false
        }
        reservePriority = priority

        return true
    }


}