package me.mikun.live2d.framework.effect

import me.mikun.live2d.framework.model.Live2DModel

abstract class Live2DEffect {
    abstract fun update(model: Live2DModel, deltaSeconds: Float)

    object BuildIn {
        val breath = "breath-buildin"
        val eyeBlink = "eyeBlink-buildin"
        val lipSync = "lipSync-buildin"
    }

}