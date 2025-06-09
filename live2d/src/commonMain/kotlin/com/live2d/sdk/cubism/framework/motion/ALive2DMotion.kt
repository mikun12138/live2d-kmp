/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

abstract class ALive2DMotion {

//    fun updateParameters(
//        model: CubismModel,
//        motionQueueEntry: CubismMotionQueueEntry,
//        userTimeSeconds: Float
//    ) {
//        if (!motionQueueEntry.isFinished) {
//            setupMotionQueueEntry(motionQueueEntry, userTimeSeconds)
//
//            val fadeWeight = updateFadeWeight(motionQueueEntry, userTimeSeconds)
//
//            //---- 全てのパラメータIDをループする ----
//            doUpdateParameters(model, userTimeSeconds, fadeWeight, motionQueueEntry)
//
//            // 後処理
//            // 終了時刻を過ぎたら終了フラグを立てる（CubismMotionQueueManager）
//            if (motionQueueEntry.endTime > 0.0f && motionQueueEntry.endTime < userTimeSeconds) {
//                motionQueueEntry.isFinished = true // 終了
//            }
//        }
//    }

//    /**
//     * Perform parameter updates for the model.
//     *
//     * @param model target model
//     * @param userTimeSeconds total delta time[s]
//     * @param weight weight of motion
//     * @param motionQueueEntry motion managed by CubismMotionQueueManager
//     */
//    abstract fun doUpdateParameters(
//        model: CubismModel,
//        userTimeSeconds: Float,
//        weight: Float,
//        motionQueueEntry: CubismMotionQueueEntry
//    )

    var fadeInSeconds: Float = -1.0f
    var fadeOutSeconds: Float = -1.0f



}
