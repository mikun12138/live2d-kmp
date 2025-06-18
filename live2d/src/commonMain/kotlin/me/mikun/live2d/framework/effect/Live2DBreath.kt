/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.effect

import me.mikun.live2d.framework.id.Live2DId
import com.live2d.sdk.cubism.framework.math.CubismMath.PI
import me.mikun.live2d.framework.model.Live2DModel
import kotlin.math.sin

/**
 * This class offers the breath function.
 */
class Live2DBreath {
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

    constructor(vararg parameterData: BreathParameterData) {
        _parameters.addAll(parameterData)
    }

    /**
     * Updates the parameters of the model.
     *
     * @param model the target model
     * @param deltaSeconds the delta time[s]
     */
    fun update(model: Live2DModel, deltaSeconds: Float) {
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

