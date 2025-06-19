/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.physics

import me.mikun.live2d.framework.id.Live2DId
import com.live2d.sdk.cubism.framework.math.CubismVector2

/**
 * Internal data of CubismPhysics.
 */
class Live2DPhysicsInternal {

    class CubismPhysicsRig {
        var subRigCount: Int = 0

        var settings = ArrayList<CubismPhysicsSubRig>()

        /**
         * all of inputs
         */
        var inputs: MutableList<CubismPhysicsInput> = ArrayList()

        /**
         * all of outputs
         */
        var outputs: MutableList<CubismPhysicsOutput> = ArrayList()

        /**
         * List of particles for physics operation
         */
        var particles: MutableList<CubismPhysicsParticle> = ArrayList()

        /**
         * Gravity
         */
        var gravity: CubismVector2 = CubismVector2()

        /**
         * Wind
         */
        var wind: CubismVector2 = CubismVector2()

        /**
         * Physics operation FPS
         */
        var fps: Float = 0f
    }


    data class CubismPhysicsSubRig(
        /**
         * First index of inputs
         */
        val baseInputIndex: Int,

        /**
         * number of inputs
         */
        val inputCount: Int,

        /**
         * First index of outputs
         */
        val baseOutputIndex: Int,

        /**
         * number of outputs
         */
        val outputCount: Int,

        /**
         * First index of particles
         */
        val baseParticleIndex: Int,

        /**
         * number of particles
         */
        val particleCount: Int,

        /**
         * Normalized position
         */
        val normalizationPosition: CubismPhysicsNormalization,

        /**
         * Normalized angle
         */
        var normalizationAngle: CubismPhysicsNormalization,
    )

    /**
     * Normalization information for physics operations.
     * @see me.mikun.live2d.framework.data.PhysicsJson.PhysicsSetting.Normalization
     */
    data class CubismPhysicsNormalization(
        val maximumValue: Float,
        val minimumValue: Float,
        val defaultValue: Float,
    )


    /**
     * Input information for physics operations.
     * @see me.mikun.live2d.framework.data.PhysicsJson.PhysicsSetting.Input
     */
    class CubismPhysicsInput {
        lateinit var source: CubismPhysicsParameter
        var reflect: Boolean = false
        var type: CubismPhysicsSource? = null
        var weight: Float = 0f

        // TODO:: remove it
        /**
         * Index of input source parameter
         */
        var sourceParameterIndex: Int = 0

        /**
         * Function to get normalized parameter values
         */
        var getNormalizedParameterValue: NormalizedPhysicsParameterValueGetter? = null
    }

    /**
     * Output information for physics operations.
     * @see me.mikun.live2d.framework.data.PhysicsJson.PhysicsSetting.Output
     */
    class CubismPhysicsOutput {
        lateinit var destination: CubismPhysicsParameter
        var reflect: Boolean = false
        var scale: Float = 0f
        var type: CubismPhysicsSource? = null
        var vertexIndex: Int = 0
        var weight: Float = 0f

        /**
         * transition scale
         */
        var transitionScale: CubismVector2 = CubismVector2()

        /**
         * Index of output destination parameter
         */
        // TODO:: remove it
        var destinationParameterIndex: Int = 0

        /**
         * Value when the value is below the minimum value
         */
        var valueBelowMinimum: Float = 0f

        /**
         * Value when the maximum value is exceeded.
         */
        var valueExceededMaximum: Float = 0f

        /**
         * Function to get the value for physics operation.
         */
        var getValue: PhysicsValueGetter? = null

        /**
         * Function to get the scale value for physics operation
         */
        var getScale: PhysicsScaleGetter? = null
    }

    /**
     * Parameter information for physics operations.
     */
    class CubismPhysicsParameter(
        val id: Live2DId,
        val targetType: CubismPhysicsTargetType
    )

    enum class CubismPhysicsTargetType {
        PARAMETER
    }

    enum class CubismPhysicsSource {
        X,
        Y,
        ANGLE
    }

    /**
     * Functional interface with a function which gets normalized parameters.
     */
    interface NormalizedPhysicsParameterValueGetter {
        /**
         * Get normalized parameters.
         *
         * @param targetTransition the move value of the calculation result
         * @param targetAngle the angle of the calculation result
         * @param value the value of the parameter
         * @param parameterMinimumValue the minimum value of the parameter
         * @param parameterMaximumValue the maximum value of the parameter
         * @param parameterDefaultValue the default value of the parameter
         * @param normalizationPosition the normalized position
         * @param normalizationAngle the normalized angle
         * @param isInverted Whether the value is inverted
         * @param weight a weight
         */
        fun getNormalizedParameterValue(
            targetTransition: CubismVector2,
            targetAngle: FloatArray,
            value: Float,
            parameterMinimumValue: Float,
            parameterMaximumValue: Float,
            parameterDefaultValue: Float,
            normalizationPosition: CubismPhysicsNormalization,
            normalizationAngle: CubismPhysicsNormalization,
            isInverted: Boolean,
            weight: Float,
        )
    }

    /**
     * Functional interface with a function for getting values of physics operations.
     */
    interface PhysicsValueGetter {
        /**
         * Get values of physics operations.
         *
         * @param transition a transition value
         * @param particles a particles list
         * @param particleIndex a particle index
         * @param isInverted Whether the value is inverted
         * @param parentGravity a gravity
         * @return the value
         */
        fun getValue(
            transition: CubismVector2,
            particles: MutableList<CubismPhysicsParticle>,
            baseParticleIndex: Int,
            particleIndex: Int,
            isInverted: Boolean,
            parentGravity: CubismVector2,
        ): Float
    }

    /**
     * Functional interface with a function for getting the scale of physics operations.
     */
    interface PhysicsScaleGetter {
        /**
         * Get a scale of physics operations.
         *
         * @param transitionScale transition scale
         * @param angleScale angle scale
         * @return scale value
         */
        fun getScale(transitionScale: CubismVector2, angleScale: Float): Float
    }


    /**
     * External forces used in physics operations.
     */
    class PhysicsJsonEffectiveForces {
        /**
         * Gravity
         */
        var gravity: CubismVector2? = null

        /**
         * Wind
         */
        var wind: CubismVector2? = null
    }


    /**
     * Information on a particle used for physics operations.
     */
    class CubismPhysicsParticle(
        val position: CubismVector2,
        val mobility: Float,
        val delay: Float,
        val acceleration: Float,
        val radius: Float,
    ) {

        /**
         * Initial position
         */
        var initialPosition: CubismVector2 = CubismVector2()

        /**
         * Last position
         */
        var lastPosition: CubismVector2 = CubismVector2()

        /**
         * Last gravity
         */
        var lastGravity: CubismVector2 = CubismVector2()

        /**
         * Current force
         */
        var force: CubismVector2 = CubismVector2()

        /**
         * Current velocity
         */
        var velocity: CubismVector2 = CubismVector2()
    }


}
