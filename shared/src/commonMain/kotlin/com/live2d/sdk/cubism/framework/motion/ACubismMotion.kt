/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion

import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.math.CubismMath.getEasingSine
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogError
import java.util.Collections

/**
 * Abstract base class for motion.
 * This class manages motion playback by MotionQueueManager.
 */
abstract class ACubismMotion {
    /**
     * Update model's parameters.
     *
     * @param model target model
     * @param motionQueueEntry motion managed by CubismMotionQueueManager
     * @param userTimeSeconds total delta time[s]
     */
    fun updateParameters(
        model: CubismModel?,
        motionQueueEntry: CubismMotionQueueEntry,
        userTimeSeconds: Float
    ) {
        if (!motionQueueEntry.isAvailable || motionQueueEntry.isFinished) {
            return
        }

        setupMotionQueueEntry(motionQueueEntry, userTimeSeconds)

        val fadeWeight = updateFadeWeight(motionQueueEntry, userTimeSeconds)

        //---- 全てのパラメータIDをループする ----
        doUpdateParameters(model, userTimeSeconds, fadeWeight, motionQueueEntry)

        // 後処理
        // 終了時刻を過ぎたら終了フラグを立てる（CubismMotionQueueManager）
        if (motionQueueEntry.endTime > 0.0f && motionQueueEntry.endTime < userTimeSeconds) {
            motionQueueEntry.isFinished(true) // 終了
        }
    }

    /**
     * モーションの再生を開始するためのセットアップを行う。
     *
     * @param motionQueueEntry CubismMotionQueueManagerによって管理されるモーション
     * @param userTimeSeconds 総再生時間（秒）
     */
    fun setupMotionQueueEntry(
        motionQueueEntry: CubismMotionQueueEntry,
        userTimeSeconds: Float
    ) {
        if (!motionQueueEntry.isAvailable || motionQueueEntry.isFinished) {
            return
        }

        if (motionQueueEntry.isStarted) {
            return
        }

        motionQueueEntry.isStarted(true)

        // Record the start time of the motion.
        motionQueueEntry.startTime = userTimeSeconds - offsetSeconds
        // Record the start time of fade-in
        motionQueueEntry.fadeInStartTime = userTimeSeconds

        // Deal with the case where the status is set "end" before it has started.
        if (motionQueueEntry.endTime < 0) {
            adjustEndTime(motionQueueEntry)
        }
    }

    /**
     * モーションフェードのウェイト値を更新する。
     *
     * @param motionQueueEntry CubismMotionQueueManagerで管理されているモーション
     * @param userTimeSeconds デルタ時間の積算値[秒]
     * @return 更新されたウェイト値
     */
    fun updateFadeWeight(motionQueueEntry: CubismMotionQueueEntry?, userTimeSeconds: Float): Float {
        if (motionQueueEntry == null) {
            cubismLogError("motionQueueEntry is null.")
        }

        var fadeWeight = weight // 現在の値と掛け合わせる割合

        // ---- フェードイン・アウトの処理 ----
        // 単純なサイン関数でイージングする。
        val fadeIn = if (this.fadeInSeconds == 0.0f)
            1.0f
        else
            getEasingSine((userTimeSeconds - motionQueueEntry!!.fadeInStartTime) / this.fadeInSeconds)
        val fadeOut = if (this.fadeOutSeconds == 0.0f || motionQueueEntry!!.endTime < 0.0f)
            1.0f
        else
            getEasingSine((motionQueueEntry.endTime - userTimeSeconds) / this.fadeOutSeconds)
        fadeWeight = fadeWeight * fadeIn * fadeOut
        motionQueueEntry!!.setState(userTimeSeconds, fadeWeight)

        assert(0.0f <= fadeWeight && fadeWeight <= 1.0f)

        return fadeWeight
    }


    open val duration: Float
        /**
         * Get the duration of the motion.
         *
         * @return duration of motion[s]
         * (If it is a loop, "-1".
         * Override if it is not a loop.
         * If the value is positive, the process ends at the time it is retrieved.
         * When the value is "-1", the process will not end unless there is a stop command from outside)
         */
        get() = -1.0f

    open val loopDuration: Float
        /**
         * Get the duration of one motion loop.
         *
         * @return duration of one motion loop[s]
         *
         *
         * (If it does not loop, it returns the same value as GetDuration().
         * Return "-1" in case the duration of one loop cannot be defined (e.g., a subclass that keeps moving programmatically)).
         */
        get() = -1.0f

    /**
     * Set the start time for motion playback.
     *
     * @param offsetSeconds start time for motion playback[s]
     */
    fun setOffsetTime(offsetSeconds: Float) {
        this.offsetSeconds = offsetSeconds
    }

    /**
     * Check for event firing.
     * The input time reference is set to zero at the called motion timing.
     *
     * @param beforeCheckTimeSeconds last event check time [s]
     * @param motionTimeSeconds playback time this time [s]
     * @return list of events that have fired
     */
    open fun getFiredEvent(
        beforeCheckTimeSeconds: Float,
        motionTimeSeconds: Float
    ): MutableList<String?> {
        return Collections.unmodifiableList<String?>(firedEventValues)
    }

    /**
     * Registers a motion playback start callback.
     * It is not called in the following states:
     * 1. when the currently playing motion is set as "loop"
     * 2. when null is registered in the callback
     *
     * @param onBeganMotionHandler start-of-motion playback callback function
     */
    fun setBeganMotionHandler(onBeganMotionHandler: IBeganMotionCallback?) {
        this.beganMotionCallback = onBeganMotionHandler
    }

    /**
     * Registers a motion playback end callback.
     * It is called when the isFinished flag is set.
     * It is not called in the following states:
     * 1. when the currently playing motion is set as "loop"
     * 2. when null is registered in the callback
     *
     * @param onFinishedMotionHandler end-of-motion playback callback function
     */
    fun setFinishedMotionHandler(onFinishedMotionHandler: IFinishedMotionCallback?) {
        this.finishedMotionCallback = onFinishedMotionHandler
    }

    open val isExistModelOpacity: Boolean
        /**
         * Check to see if a transparency curve exists.
         *
         * @return return true if the key exists
         */
        get() = false


    open val modelOpacityIndex: Int
        /**
         * Return the index of the transparency curve.
         *
         * @return success: index of the transparency curve
         */
        get() = -1

    /**
     * Return the ID of the transparency curve.
     *
     * @return success: atransparency curve
     */
    open fun getModelOpacityId(index: Int): CubismId? {
        return null
    }

    /**
     * Perform parameter updates for the model.
     *
     * @param model target model
     * @param userTimeSeconds total delta time[s]
     * @param weight weight of motion
     * @param motionQueueEntry motion managed by CubismMotionQueueManager
     */
    protected abstract fun doUpdateParameters(
        model: CubismModel?,
        userTimeSeconds: Float,
        weight: Float,
        motionQueueEntry: CubismMotionQueueEntry?
    )

    protected fun adjustEndTime(motionQueueEntry: CubismMotionQueueEntry) {
        val duration = this.duration

        // duration == -1 の場合はループする
        val endTime = if (duration <= 0)
            -1f
        else
            motionQueueEntry.startTime + duration

        motionQueueEntry.endTime = endTime
    }

    protected open val modelOpacityValue: Float
        /**
         * 指定時間の透明度の値を返す。
         * NOTE: 更新後の値を取るには`updateParameters()` の後に呼び出す。
         *
         * @return success : モーションの当該時間におけるOpacityの値
         */
        get() = 1.0f

    /**
     * Get the time it takes to fade in.
     *
     * @return time for fade in[s]
     */
    /**
     * Set the time it takes to fade in.
     *
     * @param this.fadeInTime time for fade in [s]
     */
    /**
     * Time for fade-in [s]
     */
    var fadeInSeconds: Float = -1.0f
    /**
     * Get the time it takes to fade out.
     *
     * @return time for fade out[s]
     */
    /**
     * Set a time it takes to fade out.
     *
     * @param this.fadeOutTime time for fade out[s]
     */
    /**
     * Time for fade-out[s]
     */
    var fadeOutSeconds: Float = -1.0f
    /**
     * Get the weight to be applied to the motion.
     *
     * @return weight(0.0 - 1.0)
     */
    /**
     * Set the weight to be applied to the motion.
     *
     * @param weight weight(0.0 - 1.0)
     */
    /**
     * Weight of motion
     */
    var weight: Float = 1.0f

    /**
     * Start time for motion playback[s]
     */
    protected var offsetSeconds: Float = 0f

    /**
     * Checks whether the motion is set to loop.
     *
     * @return true if the motion is set to loop; otherwise false.
     */
    /**
     * Sets whether the motion should loop.
     *
     * @param loop true to set the motion to loop
     */
    /**
     * Enable/Disable loop
     */
    var loop: Boolean = false
    /**
     * Checks the setting for fade-in of looping motion.
     *
     * @return true if fade-in for looping motion is set; otherwise false.
     */
    /**
     * Sets whether to perform fade-in for looping motion.
     *
     * @param loopFadeIn true to perform fade-in for looping motion
     */
    /**
     * flag whether fade-in is enabled at looping. Default value is true.
     */
    var loopFadeIn: Boolean = true

    /**
     * The previous state of `_isLoop`.
     */
    protected var previousLoopState: Boolean = this.loop

    /**
     * List of events that have fired
     */
    protected var firedEventValues: MutableList<String?> = ArrayList<String?>()

    /**
     * Get the start-of-motion playback callback function.
     *
     * @return registered start-of-motion playback callback function; if null, no function is registered
     */
    /**
     * Start-of-motion playback callback function
     */
    var beganMotionCallback: IBeganMotionCallback? = null
        protected set

    /**
     * Get the end-of-motion playback callback function.
     *
     * @return registered end-of-motion playback callback function; if null, no function is registered
     */
    /**
     * End-of-motion playback callback function
     */
    var finishedMotionCallback: IFinishedMotionCallback? = null
        protected set
}
