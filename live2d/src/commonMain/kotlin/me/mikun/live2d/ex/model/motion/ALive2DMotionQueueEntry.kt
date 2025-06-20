package me.mikun.live2d.ex.model.motion

import me.mikun.live2d.framework.math.CubismMath
import me.mikun.live2d.framework.motion.ALive2DMotion
import me.mikun.live2d.framework.utils.IState
import me.mikun.live2d.framework.utils.StateContext
import me.mikun.live2d.framework.utils.switchStateTo

/**
 * Manager class for each motion being played by CubismMotionQueueManager.
 */

abstract class ALive2DMotionQueueEntry(
    open val manager: ALive2DMotionManager,
    open val motion: ALive2DMotion,

    override var state: State = State.Init,
) : StateContext<ALive2DMotionQueueEntry, ALive2DMotionQueueEntry.State> {

    fun init(
        totalSeconds: Float,
    ) {
        // Record the start time of the motion.
        startTimePoint = totalSeconds

        this switchStateTo State.FadeIn

        doInit()
    }

    protected abstract fun doInit()

    fun calFadeWeight(totalSeconds: Float): Float {
        val fadeIn = if (motion.fadeInSeconds < 0.0f)
            1.0f
        else
            CubismMath.getEasingSine(
                (totalSeconds - startTimePoint) / motion.fadeInSeconds
            )
        val fadeOut = if (motion.fadeOutSeconds < 0.0f || endTimePoint < 0.0f)
            1.0f
        else
            CubismMath.getEasingSine((endTimePoint - totalSeconds) / motion.fadeOutSeconds)

        check(fadeIn * fadeOut in 0.0f..1.0f)

        return fadeIn * fadeOut
    }

    var startTimePoint: Float = -1.0f

    // TODO:: 你知道我要todo什么
//    /**
//     * Fade-in start time[s] (When in a loop, only the first time.)
//     */
//    var fadeInStartTime: Float = -1.0f
    var endTimePoint: Float = -1.0f

    /**
     * flag whether fade-in is enabled at looping. Default value is true.
     */
    var loopFadeIn: Boolean = true

    //    /**
//     * 再生中の表情モーションのウェイトのリスト
//     * 0为开始 fade, >=1为完成fade
//     */
//    var fadeWeight: Float = 0.0f
//


    enum class State(
        override val onEnter: (ALive2DMotionQueueEntry, State) -> Unit = { _, _ -> },
        override val onExit: (ALive2DMotionQueueEntry, State) -> Unit = { _, _ -> },
    ) : IState<ALive2DMotionQueueEntry, State> {
        Init,
        FadeIn,
        Playing,
        FadeOut(
            onEnter = { context, lastState ->
                with(context) {
                    val newEndTimeSeconds = manager.totalSeconds + motion.fadeOutSeconds
                    if (this.endTimePoint !in 0.0f..newEndTimeSeconds) {
                        this.endTimePoint = newEndTimeSeconds
                    }
                }
            }
        ),
        End
        ;

        fun inInit() = this == Init
        fun inActive() = this == FadeIn || this == Playing || this == FadeOut
        fun inEnd() = this == End
    }
}