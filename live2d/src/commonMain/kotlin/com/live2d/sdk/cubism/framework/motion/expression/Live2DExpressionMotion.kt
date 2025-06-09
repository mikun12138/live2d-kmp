/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.motion.expression

import com.live2d.sdk.cubism.framework.id.Live2DId
import com.live2d.sdk.cubism.framework.motion.ALive2DMotion
import com.live2d.sdk.cubism.framework.data.ExpressionJson
import com.live2d.sdk.cubism.framework.id.Live2DIdManager
import kotlinx.serialization.json.Json

/**
 * A motion class for facial expressions.
 */
class Live2DExpressionMotion : ALive2DMotion {

    constructor(buffer: ByteArray) {
        parse(buffer)
    }

    private fun parse(exp3Json: ByteArray) {
        Json.decodeFromString<ExpressionJson>(String(exp3Json)).let { json ->
            fadeInSeconds = json.fadeInTime?.let {
                it
            } ?: DEFAULT_FADE_TIME
            fadeOutSeconds = json.fadeOutTime?.let {
                it
            } ?: DEFAULT_FADE_TIME

            // Each parameter setting
            for (param in json.parameters) {
                // Create a configuration object and add it to the list.
                this.parameters.add(
                    ExpressionParameter(
                        parameterId = Live2DIdManager.id(
                            param.id
                        ),
                        blendType = when (param.blend) {
                            ExpressionBlendType.ADD.value -> {
                                ExpressionBlendType.ADD
                            }

                            ExpressionBlendType.MULTIPLY.value -> {
                                ExpressionBlendType.MULTIPLY
                            }

                            ExpressionBlendType.OVERWRITE.value -> {
                                ExpressionBlendType.OVERWRITE
                            }

                            else -> {
                                ExpressionBlendType.ADD
                            }
                        },
                        value = param.value
                    )
                )
            }
        }
    }

    enum class ExpressionBlendType(
        val value: String
    ) {
        ADD("Add"),
        MULTIPLY("Multiply"),
        OVERWRITE("Overwrite");
    }

    class ExpressionParameter(
        val parameterId: Live2DId,
        val blendType: ExpressionBlendType,
        val value: Float,
    )




    // TODO:: never used
//    override fun doUpdateParameters(
//        model: CubismModel,
//        userTimeSeconds: Float,
//        weight: Float,
//        motionQueueEntry: CubismMotionQueueEntry
//    ) {
//        for (i in parameters.indices) {
//            val parameter = parameters.get(i)
//            when (parameter.blendType) {
//                ExpressionBlendType.ADD -> model.addParameterValue(
//                    parameter.parameterId,
//                    parameter.value,
//                    weight
//                )
//
//                ExpressionBlendType.MULTIPLY -> model.multiplyParameterValue(
//                    parameter.parameterId,
//                    parameter.value,
//                    weight
//                )
//
//                ExpressionBlendType.OVERWRITE -> model.setParameterValue(
//                    parameter.parameterId,
//                    parameter.value,
//                    weight
//                )
//
//            }
//        }
//    }


    val parameters: MutableList<ExpressionParameter> = ArrayList<ExpressionParameter>()

    companion object {

        const val DEFAULT_FADE_TIME: Float = 1.0f
        const val DEFAULT_ADDITIVE_VALUE: Float = 0.0f
        const val DEFAULT_MULTIPLY_VALUE: Float = 1.0f
    }
}
