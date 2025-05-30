/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.effect

import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.math.CubismMath.PI
import com.live2d.sdk.cubism.framework.model.Model
import kotlin.math.sin

/**
 * This class offers the breath function.
 */
class CubismBreath {
    data class BreathParameterData(
        /**
         * A parameter ID bound breath info.
         */
        val parameterId: CubismId,

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
     * @param deltaTimeSeconds the delta time[s]
     */
    fun updateParameters(model: Model, deltaTimeSeconds: Float) {
        userTimeSeconds += deltaTimeSeconds
        val t: Float = userTimeSeconds * 2.0f * PI

        for (breathData in _parameters) {

            val value: Float = breathData.offset + (breathData.peak * sin(t / breathData.cycle))

            model.addParameterValue(
                breathData.parameterId,
                value,
                breathData.weight
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

