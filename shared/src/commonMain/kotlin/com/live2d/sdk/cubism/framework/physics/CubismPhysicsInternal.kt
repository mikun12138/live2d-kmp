/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.physics

import com.live2d.sdk.cubism.framework.id.Live2DId
import com.live2d.sdk.cubism.framework.math.CubismVector2

/**
 * Internal data of CubismPhysics.
 */
class CubismPhysicsInternal {
    /**
     * Types of physics operations to be applied.
     */
    enum class CubismPhysicsTargetType {
        /**
         * Apply physics operation to parameters
         */
        PARAMETER
    }

    /**
     * Types of input for physics operations.
     */
    enum class CubismPhysicsSource {
        /**
         * From X-axis
         */
        X,

        /**
         * From Y-axis
         */
        Y,

        /**
         * From angle
         */
        ANGLE
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
     * Parameter information for physics operations.
     */
    class CubismPhysicsParameter {
        /**
         * Parameter ID
         */
        lateinit var Id: Live2DId

        /**
         * Type of destination
         */
        var targetType: CubismPhysicsTargetType? = null
    }

    /**
     * Normalization information for physics operations.
     */
    class CubismPhysicsNormalization {
        /**
         * Minimum value
         */
        var minimumValue: Float = 0f

        /**
         * Maximum value
         */
        var maximumValue: Float = 0f

        /**
         * Default value
         */
        var defaultValue: Float = 0f
    }

    /**
     * Information on a particle used for physics operations.
     */
    class CubismPhysicsParticle {
        /**
         * Initial position
         */
        var initialPosition: CubismVector2 = CubismVector2()

        /**
         * Mobility
         */
        var mobility: Float = 0f

        /**
         * Delay
         */
        var delay: Float = 0f

        /**
         * Acceleration
         */
        var acceleration: Float = 0f

        /**
         * Distance
         */
        var radius: Float = 0f

        /**
         * Current position
         */
        var position: CubismVector2 = CubismVector2()

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

    /**
     * Manager of phycal points in physics operations.
     */
    class CubismPhysicsSubRig {
        /**
         * number of inputs
         */
        var inputCount: Int = 0

        /**
         * number of outputs
         */
        var outputCount: Int = 0

        /**
         * number of particles
         */
        var particleCount: Int = 0

        /**
         * First index of inputs
         */
        var baseInputIndex: Int = 0

        /**
         * First index of outputs
         */
        var baseOutputIndex: Int = 0

        /**
         * First index of particles
         */
        var baseParticleIndex: Int = 0

        /**
         * Normalized position
         */
        var normalizationPosition: CubismPhysicsNormalization = CubismPhysicsNormalization()

        /**
         * Normalized angle
         */
        var normalizationAngle: CubismPhysicsNormalization = CubismPhysicsNormalization()
    }

    /**
     * Input information for physics operations.
     */
    class CubismPhysicsInput {
        /**
         * Input source parameter
         */
        var source: CubismPhysicsParameter = CubismPhysicsParameter()

        /**
         * Index of input source parameter
         */
        var sourceParameterIndex: Int = 0

        /**
         * Weight
         */
        var weight: Float = 0f

        /**
         * Type of input
         */
        var type: CubismPhysicsSource? = null

        /**
         * Whether the value is inverted.
         */
        var reflect: Boolean = false

        /**
         * Function to get normalized parameter values
         */
        var getNormalizedParameterValue: NormalizedPhysicsParameterValueGetter? = null
    }

    /**
     * Output information for physics operations.
     */
    class CubismPhysicsOutput {
        /**
         * Output destination parameter
         */
        var destination: CubismPhysicsParameter = CubismPhysicsParameter()

        /**
         * Index of output destination parameter
         */
        var destinationParameterIndex: Int = 0

        /**
         * Pendulum index
         */
        var vertexIndex: Int = 0

        /**
         * transition scale
         */
        var transitionScale: CubismVector2 = CubismVector2()

        /**
         * Angle scale
         */
        var angleScale: Float = 0f

        /**
         * Weight
         */
        var weight: Float = 0f

        /**
         * Type of output
         */
        var type: CubismPhysicsSource? = null

        /**
         * Whether the value is inverted
         */
        var reflect: Boolean = false

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
     * Physics operation data
     */
    class CubismPhysicsRig {
        /**
         * Number of physics point for physics operation
         */
        var subRigCount: Int = 0

        /**
         * List of physics point management for physics operation
         */
        var settings = ArrayList<CubismPhysicsSubRig>()

        /**
         * List of inputs for physics operation
         */
        var inputs: MutableList<CubismPhysicsInput> = ArrayList()

        /**
         * List of outputs for physics operation
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
}
