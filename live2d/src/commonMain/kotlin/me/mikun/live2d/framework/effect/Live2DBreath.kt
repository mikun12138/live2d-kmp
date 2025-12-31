/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.effect

import me.mikun.live2d.framework.id.Live2DDefaultParameterId
import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.framework.id.Live2DIdManager
import me.mikun.live2d.framework.math.CubismMath.PI
import me.mikun.live2d.framework.model.Live2DModel
import kotlin.math.sin

/**
 * This class offers the breath function.
 */
class Live2DBreath: Live2DEffect {
    data class BreathParameterData(
        /**
         * A parameter ID bound breath info.
         */
        val parameterId: Live2DId,

        /**
         * A wave offset when breathing is taken as a sine wave
         */
        val offset: Float,

        /**
         * A wave height when breathing is taken as a sine wave
         */
        val peak: Float,

        /**
         * A wave cycle when breathing is taken as a sine wave
         */
        val cycle: Float,

        /**
         * A weight to a parameter
         */
        val weight: Float
    )

    constructor(): this(
        BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_X.id),
            0.0f,
            15.0f,
            6.5345f,
            0.5f
        ), BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_Y.id),
            0.0f,
            8.0f,
            3.5345f,
            0.5f
        ), BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_Z.id),
            0.0f,
            10.0f,
            5.5345f,
            0.5f
        ), BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.BODY_ANGLE_X.id),
            0.0f,
            4.0f,
            15.5345f,
            0.5f
        ), BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.BREATH.id),
            0.5f,
            0.5f,
            3.2345f,
            0.5f,
        )
    )

    constructor(vararg parameterData: BreathParameterData) {
        _parameters.addAll(parameterData)
    }

    /**
     * Updates the parameters of the model.
     *
     * @param model the target model
     * @param deltaSeconds the delta time[s]
     */
    override fun update(model: Live2DModel, deltaSeconds: Float) {
        userTimeSeconds += deltaSeconds
        val t: Float = userTimeSeconds * 2.0f * PI

        for (breathData in _parameters) {

            val value: Float = breathData.offset + (breathData.peak * sin(t / breathData.cycle))

            model.setParameterValue(
                breathData.parameterId,
                value * breathData.weight
            )
        }
    }

    private var _parameters: MutableList<BreathParameterData> = mutableListOf()
    val parameters: List<BreathParameterData>
        get() = _parameters

    /**
     * total elapsed time[s]
     */
    private var userTimeSeconds = 0f

}

