/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.effect

import com.live2d.sdk.cubism.framework.ICubismModelSetting
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.model.CubismModel
import java.util.Collections

/**
 * This class offers auto eyeblink function.
 */
class CubismEyeBlink private constructor(modelSetting: ICubismModelSetting) {
    /**
     * Eyeblink states
     */
    enum class EyeState {
        /**
         * Initial state
         */
        FIRST,

        /**
         * Non-eyeblink state
         */
        INTERVAL,

        /**
         * Closing-eye state
         */
        CLOSING,

        /**
         * Closed-eye state
         */
        CLOSED,

        /**
         * Opening-eye state
         */
        OPENING
    }

    /**
     * Update model's parameters.
     *
     * @param model the target model
     * @param deltaTimeSeconds delta time[s]
     */
    fun updateParameters(model: CubismModel, deltaTimeSeconds: Float) {
        userTimeSeconds += deltaTimeSeconds

        when (blinkingState) {
            EyeState.CLOSING -> updateParametersClosing(model)
            EyeState.CLOSED -> updateParametersClosed(model)
            EyeState.OPENING -> updateParametersOpening(model)
            EyeState.INTERVAL -> updateParametersInterval(model)
            EyeState.FIRST -> updateParametersFirst(model)
        }
    }

    private fun updateParametersClosing(model: CubismModel) {
        var time = (userTimeSeconds - stateStartTimeSeconds) / closingSeconds

        if (time >= 1.0f) {
            time = 1.0f
            blinkingState = EyeState.CLOSED
            stateStartTimeSeconds = userTimeSeconds
        }

        val parameterValue = 1.0f - time
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersClosed(model: CubismModel) {
        val time = (userTimeSeconds - stateStartTimeSeconds) / closedSeconds

        if (time >= 1.0f) {
            blinkingState = EyeState.OPENING
            stateStartTimeSeconds = userTimeSeconds
        }

        val parameterValue = 0.0f
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersOpening(model: CubismModel) {
        var time = (userTimeSeconds - stateStartTimeSeconds) / openingSeconds

        if (time >= 1.0f) {
            time = 1.0f
            blinkingState = EyeState.INTERVAL
            nextBlinkingTime = determineNextBlinkingTiming()
        }

        val parameterValue = time
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersInterval(model: CubismModel) {
        if (nextBlinkingTime < userTimeSeconds) {
            blinkingState = EyeState.CLOSING
            stateStartTimeSeconds = userTimeSeconds
        }

        val parameterValue = 1.0f
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersFirst(model: CubismModel) {
        blinkingState = EyeState.INTERVAL
        nextBlinkingTime = determineNextBlinkingTiming()

        val parameterValue = 1.0f
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun setParameterValueToAllIds(model: CubismModel, value: Float) {
        for (id in parameterIds) {
            model.setParameterValue(id!!, value)
        }
    }

    private fun determineNextBlinkingTiming(): Float {
        val r = Math.random().toFloat()
        return userTimeSeconds + r * (2.0f * blinkingIntervalSeconds - 1.0f)
    }

    private var blinkingState = EyeState.FIRST

    private var parameterIds: MutableList<CubismId?> = mutableListOf()

    private var nextBlinkingTime = 0f

    private var stateStartTimeSeconds = 0f

    private var blinkingIntervalSeconds = 4.0f

    private var closingSeconds = 0.1f

    private var closedSeconds = 0.05f

    private var openingSeconds = 0.15f

    private var userTimeSeconds = 0f


    init {
        for (i in 0..<modelSetting.eyeBlinkParameterCount) {
            val eyeBlinkId = modelSetting.getEyeBlinkParameterId(i)

            if (eyeBlinkId != null) {
                parameterIds.add(eyeBlinkId)
            }
        }
    }

    companion object {

        /**
         * This constant is becomes "true" if the eye blinking parameter specified by ID is specified to close when the value is 0, and "false" if the parameter is specified to close when the value is 1.
         */
        private const val CLOSE_IF_ZERO = true
    }
}
