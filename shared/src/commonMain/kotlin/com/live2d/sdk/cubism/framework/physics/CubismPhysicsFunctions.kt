/*
 *
 *  * Copyright(c) Live2D Inc. All rights reserved.
 *  *
 *  * Use of this source code is governed by the Live2D Open Software license
 *  * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 *
 */
package com.live2d.sdk.cubism.framework.physics

import com.live2d.sdk.cubism.framework.math.CubismMath
import com.live2d.sdk.cubism.framework.math.CubismVector2
import com.live2d.sdk.cubism.framework.physics.CubismPhysicsInternal.NormalizedPhysicsParameterValueGetter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * This is the set of algorithms used in CubismPhysics class.
 *
 *
 * In Cubism SDK for Java Framework's CubismPhysics, employs a design pattern called the "Strategy Pattern". This class is a collection of those 'strategies'.
 */
internal object CubismPhysicsFunctions {
    private val tmpGravity = CubismVector2()

    private fun normalizeParameterValue(
        value: Float,
        parameterMinimum: Float,
        parameterMaximum: Float,
        parameterDefault: Float,
        normalizedMinimum: Float,
        normalizedMaximum: Float,
        normalizedDefault: Float,
        isInverted: Boolean
    ): Float {
        var value = value
        var result = 0.0f

        val maxValue = max(parameterMaximum, parameterMinimum)

        if (maxValue < value) {
            value = maxValue
        }

        val minValue = min(parameterMaximum, parameterMinimum)

        if (minValue > value) {
            value = minValue
        }

        val minNormValue = min(normalizedMinimum, normalizedMaximum)
        val maxNormValue = max(normalizedMinimum, normalizedMaximum)
        val middleNormValue = normalizedDefault

        val middleValue = getDefaultValue(minValue, maxValue)
        val paramValue = value - middleValue


        when (sign(paramValue).toInt()) {
            1 -> {
                val nLength = maxNormValue - middleNormValue
                val pLength = maxValue - middleValue

                if (pLength != 0.0f) {
                    result = paramValue * (nLength / pLength)
                    result += middleNormValue
                }
            }

            -1 -> {
                val nLength = minNormValue - middleNormValue
                val pLength = minValue - middleValue

                if (pLength != 0.0f) {
                    result = paramValue * (nLength / pLength)
                    result += middleNormValue
                }
            }

            0 -> {
                result = middleNormValue
            }

            else -> {}
        }
        return if (isInverted)
            result
        else
            (result * (-1.0f))
    }

    private fun getRangeValue(min: Float, max: Float): Float {
        val maxValue = max(min, max)
        val minValue = min(min, max)

        return CubismMath.absF(maxValue - minValue)
    }

    private fun getDefaultValue(min: Float, max: Float): Float {
        val minValue = min(min, max)
        return minValue + (getRangeValue(min, max) / 2.0f)
    }

    class GetInputTranslationXFromNormalizedParameterValue : NormalizedPhysicsParameterValueGetter {
        override fun getNormalizedParameterValue(
            targetTranslation: CubismVector2,
            targetAngle: FloatArray,
            value: Float,
            parameterMinimumValue: Float,
            parameterMaximumValue: Float,
            parameterDefaultValue: Float,
            normalizationPosition: CubismPhysicsInternal.CubismPhysicsNormalization,
            normalizationAngle: CubismPhysicsInternal.CubismPhysicsNormalization,
            isInverted: Boolean,
            weight: Float
        ) {
            targetTranslation.x += normalizeParameterValue(
                value,
                parameterMinimumValue,
                parameterMaximumValue,
                parameterDefaultValue,
                normalizationPosition.minimumValue,
                normalizationPosition.maximumValue,
                normalizationPosition.defaultValue,
                isInverted
            ) * weight
        }
    }

    class GetInputTranslationYFromNormalizedParameterValue : NormalizedPhysicsParameterValueGetter {
        override fun getNormalizedParameterValue(
            targetTranslation: CubismVector2,
            targetAngle: FloatArray,
            value: Float,
            parameterMinimumValue: Float,
            parameterMaximumValue: Float,
            parameterDefaultValue: Float,
            normalizationPosition: CubismPhysicsInternal.CubismPhysicsNormalization,
            normalizationAngle: CubismPhysicsInternal.CubismPhysicsNormalization,
            isInverted: Boolean,
            weight: Float
        ) {
            targetTranslation.y += normalizeParameterValue(
                value,
                parameterMinimumValue,
                parameterMaximumValue,
                parameterDefaultValue,
                normalizationPosition.minimumValue,
                normalizationPosition.maximumValue,
                normalizationPosition.defaultValue,
                isInverted
            ) * weight
        }
    }

    class GetInputAngleFromNormalizedParameterValue : NormalizedPhysicsParameterValueGetter {
        override fun getNormalizedParameterValue(
            targetTranslation: CubismVector2,
            targetAngle: FloatArray,
            value: Float,
            parameterMinimumValue: Float,
            parameterMaximumValue: Float,
            parameterDefaultValue: Float,
            normalizationPosition: CubismPhysicsInternal.CubismPhysicsNormalization,
            normalizationAngle: CubismPhysicsInternal.CubismPhysicsNormalization,
            isInverted: Boolean,
            weight: Float
        ) {
            targetAngle[0] += normalizeParameterValue(
                value,
                parameterMinimumValue,
                parameterMaximumValue,
                parameterDefaultValue,
                normalizationAngle.minimumValue,
                normalizationAngle.maximumValue,
                normalizationAngle.defaultValue,
                isInverted
            ) * weight
        }
    }

    class GetOutputTranslationX : CubismPhysicsInternal.PhysicsValueGetter {
        override fun getValue(
            translation: CubismVector2,
            particles: MutableList<CubismPhysicsInternal.CubismPhysicsParticle>,
            baseParticleIndex: Int,
            particleIndex: Int,
            isInverted: Boolean,
            parentGravity: CubismVector2
        ): Float {
            var outputValue: Float = translation.x

            if (isInverted) {
                outputValue *= -1.0f
            }

            return outputValue
        }
    }

    class GetOutputTranslationY : CubismPhysicsInternal.PhysicsValueGetter {
        override fun getValue(
            translation: CubismVector2,
            particles: MutableList<CubismPhysicsInternal.CubismPhysicsParticle>,
            baseParticleIndex: Int,
            particleIndex: Int,
            isInverted: Boolean,
            parentGravity: CubismVector2
        ): Float {
            var outputValue: Float = translation.y

            if (isInverted) {
                outputValue *= -1.0f
            }

            return outputValue
        }
    }

    class GetOutputAngle : CubismPhysicsInternal.PhysicsValueGetter {
        override fun getValue(
            translation: CubismVector2,
            particles: MutableList<CubismPhysicsInternal.CubismPhysicsParticle>,
            baseParticleIndex: Int,
            particleIndex: Int,
            isInverted: Boolean,
            parentGravity: CubismVector2
        ): Float {
            var outputValue: Float
            tmpGravity.x = parentGravity.x
            tmpGravity.y = parentGravity.y

            if (particleIndex >= 2) {
                tmpGravity.x =
                    particles.get(baseParticleIndex + particleIndex - 1).position.x - particles.get(
                        baseParticleIndex + particleIndex - 2
                    ).position.x
                tmpGravity.y =
                    particles.get(baseParticleIndex + particleIndex - 1).position.y - particles.get(
                        baseParticleIndex + particleIndex - 2
                    ).position.y
            } else {
                tmpGravity.multiply(-1.0f)
            }

            outputValue = CubismMath.directionToRadian(tmpGravity, translation)

            if (isInverted) {
                outputValue *= -1.0f
            }

            return outputValue
        }
    }

    class GetOutputScaleTranslationX : CubismPhysicsInternal.PhysicsScaleGetter {
        override fun getScale(translationScale: CubismVector2, angleScale: Float): Float {
            return translationScale.x
        }
    }

    class GetOutputScaleTranslationY : CubismPhysicsInternal.PhysicsScaleGetter {
        override fun getScale(translationScale: CubismVector2, angleScale: Float): Float {
            return translationScale.y
        }
    }

    class GetOutputScaleAngle : CubismPhysicsInternal.PhysicsScaleGetter {
        override fun getScale(translationScale: CubismVector2, angleScale: Float): Float {
            return angleScale
        }
    }
}
