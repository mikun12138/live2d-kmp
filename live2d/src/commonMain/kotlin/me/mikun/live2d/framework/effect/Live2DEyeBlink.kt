/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.effect

import me.mikun.live2d.framework.id.Live2DId
import me.mikun.live2d.framework.model.Live2DModel
import me.mikun.live2d.framework.data.ModelJson
import me.mikun.live2d.framework.id.Live2DIdManager

/**
 * This class offers auto eyeblink function.
 */
class Live2DEyeBlink {
    constructor(modelJson: ModelJson) {
        modelJson.groups.find { it.name == "EyeBlink" }?.let { group ->
            parameterIds.addAll(group.ids.map { Live2DIdManager.id(it) })
        }
    }

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

    fun update(model: Live2DModel, deltaSeconds: Float) {
        userTimeSeconds += deltaSeconds

        when (blinkingState) {
            EyeState.CLOSING -> updateParametersClosing(model)
            EyeState.CLOSED -> updateParametersClosed(model)
            EyeState.OPENING -> updateParametersOpening(model)
            EyeState.INTERVAL -> updateParametersInterval(model)
            EyeState.FIRST -> updateParametersFirst(model)
        }
    }

    private fun updateParametersClosing(model: Live2DModel) {
        var time = (userTimeSeconds - stateStartTimeSeconds) / closingSeconds

        if (time >= 1.0f) {
            time = 1.0f
            blinkingState = EyeState.CLOSED
            stateStartTimeSeconds = userTimeSeconds
        }

        val parameterValue = 1.0f - time
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersClosed(model: Live2DModel) {
        val time = (userTimeSeconds - stateStartTimeSeconds) / closedSeconds

        if (time >= 1.0f) {
            blinkingState = EyeState.OPENING
            stateStartTimeSeconds = userTimeSeconds
        }

        val parameterValue = 0.0f
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersOpening(model: Live2DModel) {
        var time = (userTimeSeconds - stateStartTimeSeconds) / openingSeconds

        if (time >= 1.0f) {
            time = 1.0f
            blinkingState = EyeState.INTERVAL
            nextBlinkingTime = determineNextBlinkingTiming()
        }

        val parameterValue = time
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersInterval(model: Live2DModel) {
        if (nextBlinkingTime < userTimeSeconds) {
            blinkingState = EyeState.CLOSING
            stateStartTimeSeconds = userTimeSeconds
        }

        val parameterValue = 1.0f
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun updateParametersFirst(model: Live2DModel) {
        blinkingState = EyeState.INTERVAL
        nextBlinkingTime = determineNextBlinkingTiming()

        val parameterValue = 1.0f
        setParameterValueToAllIds(model, parameterValue)
    }

    private fun setParameterValueToAllIds(model: Live2DModel, value: Float) {
        for (id in parameterIds) {
            model.setParameterValue(id!!, value)
        }
    }

    private fun determineNextBlinkingTiming(): Float {
        val r = Math.random().toFloat()
        return userTimeSeconds + r * (2.0f * blinkingIntervalSeconds - 1.0f)
    }

    private var blinkingState = EyeState.FIRST

    private var parameterIds: MutableList<Live2DId> = mutableListOf()

    private var nextBlinkingTime = 0f

    private var stateStartTimeSeconds = 0f

    private var blinkingIntervalSeconds = 4.0f

    private var closingSeconds = 0.1f

    private var closedSeconds = 0.05f

    private var openingSeconds = 0.15f

    private var userTimeSeconds = 0f

    companion object {

        /**
         * This constant is becomes "true" if the eye blinking parameter specified by ID is specified to close when the value is 0, and "false" if the parameter is specified to close when the value is 1.
         */
        private const val CLOSE_IF_ZERO = true
    }
}
