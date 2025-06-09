/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.physics

import com.live2d.sdk.cubism.framework.math.CubismMath
import com.live2d.sdk.cubism.framework.math.CubismVector2
import com.live2d.sdk.cubism.framework.model.Live2DModel
import com.live2d.sdk.cubism.framework.data.PhysicsJson
import com.live2d.sdk.cubism.framework.id.Live2DIdManager
import kotlinx.serialization.json.Json
import kotlin.math.pow

/**
 * Physics operation class.
 */
class Live2DPhysics {
    constructor(buffer: ByteArray) {
        parse(buffer)
        physicsRig.gravity.y = 0.0f

        options!!.gravity.set(0.0f, -1.0f)
        options!!.wind.setZero()
    }

    /**
     * Options of physics operation
     */
    class Options(
        val wind: CubismVector2 = CubismVector2(),
        val gravity: CubismVector2 = CubismVector2(),

        )

    /**
     * Output result of physics operations before applying to parameters
     */
    class PhysicsOutput(
        var outputs: FloatArray
    )

    /**
     * Reset parameters.
     */
    fun reset() {
        options!!.gravity.set(0.0f, -1.0f)
        options!!.wind.setZero()

        physicsRig.gravity.setZero()
        physicsRig.wind.setZero()

        initialize()
    }

    /**
     * 現在のパラメータ値で物理演算が安定化する状態を演算する。
     *
     * @param model 物理演算の結果を適用するモデル
     */
    fun stabilization(model: Live2DModel) {
        val totalAngle = FloatArray(1)
        var weight: Float
        var radAngle: Float
        var outputValue: Float
        val totalTranslation: CubismVector2 = CubismVector2()
        var i: Int
        var settingIndex: Int
        var particleIndex: Int
        var currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig

        val parameterValues = model.model.parameters.values
        val parameterMaximumValues = model.model.parameters.maximumValues
        val parameterMinimumValues = model.model.parameters.minimumValues
        val parameterDefaultValues = model.model.parameters.defaultValues

        if (parameterCaches.size < model.parameterCount) {
            parameterCaches = FloatArray(model.parameterCount)
        }
        if (parameterInputCaches.size < model.parameterCount) {
            parameterInputCaches = FloatArray(model.parameterCount)
        }

        for (j in 0..<model.parameterCount) {
            parameterCaches[j] = parameterValues[j]
            parameterInputCaches[j] = parameterValues[j]
        }

        settingIndex = 0
        while (settingIndex < physicsRig.subRigCount) {
            totalAngle[0] = 0.0f
            totalTranslation.setZero()

            currentSetting = physicsRig.settings.get(settingIndex)
            val baseInputIndex: Int = currentSetting.baseInputIndex
            val baseOutputIndex: Int = currentSetting.baseOutputIndex
            val baseParticleIndex: Int = currentSetting.baseParticleIndex

            // Load input parameters.
            i = 0
            while (i < currentSetting.inputCount) {
                val currentInput: Live2DPhysicsInternal.CubismPhysicsInput = physicsRig.inputs.get(baseInputIndex + i)

                weight = currentInput.weight / MAXIMUM_WEIGHT

                if (currentInput.sourceParameterIndex === -1) {
                    currentInput.sourceParameterIndex =
                        model.getParameterIndex(currentInput.source.Id)
                }

                currentInput.getNormalizedParameterValue!!.getNormalizedParameterValue(
                    totalTranslation,
                    totalAngle,
                    parameterValues[currentInput.sourceParameterIndex],
                    parameterMinimumValues[currentInput.sourceParameterIndex],
                    parameterMaximumValues[currentInput.sourceParameterIndex],
                    parameterDefaultValues[currentInput.sourceParameterIndex],
                    currentSetting.normalizationPosition,
                    currentSetting.normalizationAngle,
                    currentInput.reflect,
                    weight
                )

                parameterCaches[currentInput.sourceParameterIndex] =
                    parameterValues[currentInput.sourceParameterIndex]
                i++
            }

            radAngle = CubismMath.degreesToRadian(-totalAngle[0])

            totalTranslation.x =
                (totalTranslation.x * CubismMath.cosF(radAngle) - totalTranslation.y * CubismMath.sinF(
                    radAngle
                ))
            totalTranslation.y =
                (totalTranslation.x * CubismMath.sinF(radAngle) + totalTranslation.y * CubismMath.cosF(
                    radAngle
                ))

            // Calculate particles position.
            updateParticlesForStabilization(
                physicsRig.particles,
                baseParticleIndex,
                currentSetting.particleCount,
                totalTranslation,
                totalAngle[0],
                options!!.wind,
                MOVEMENT_THRESHOLD * currentSetting.normalizationPosition.maximumValue
            )

            // Update output parameters.
            i = 0
            while (i < currentSetting.outputCount) {
                val currentOutput: Live2DPhysicsInternal.CubismPhysicsOutput = physicsRig.outputs.get(baseOutputIndex + i)

                particleIndex = currentOutput.vertexIndex

                if (currentOutput.destinationParameterIndex === -1) {
                    currentOutput.destinationParameterIndex =
                        model.getParameterIndex(currentOutput.destination.Id)
                }

                if (particleIndex < 1 || particleIndex >= currentSetting.particleCount) {
                    i++
                    continue
                }

                val translation: CubismVector2 = CubismVector2()
                CubismVector2.subtract(
                    physicsRig.particles.get(baseParticleIndex + particleIndex).position,
                    physicsRig.particles.get(baseParticleIndex + particleIndex - 1).position,
                    translation
                )

                outputValue = currentOutput.getValue!!.getValue(
                    translation,
                    physicsRig.particles,
                    physicsRig.settings.get(i).baseParticleIndex,
                    particleIndex,
                    currentOutput.reflect,
                    options!!.gravity
                )

                currentRigOutputs.get(settingIndex)!!.outputs[i] = outputValue
                previousRigOutputs.get(settingIndex)!!.outputs[i] = outputValue

                updateOutputParameterValue(
                    parameterValues,
                    currentOutput.destinationParameterIndex,
                    parameterMinimumValues[currentOutput.destinationParameterIndex],
                    parameterMaximumValues[currentOutput.destinationParameterIndex],
                    outputValue,
                    currentOutput
                )

                parameterCaches[currentOutput.destinationParameterIndex] =
                    parameterValues[currentOutput.destinationParameterIndex]
                i++
            }
            settingIndex++
        }
    }

    /**
     * Evaluate a physics operation.
     *
     *
     * Pendulum interpolation weights
     *
     *
     * 振り子の計算結果は保存され、パラメータへの出力は保存された前回の結果で補間されます。
     * The result of the pendulum calculation is saved and
     * the output to the parameters is interpolated with the saved previous result of the pendulum calculation.
     *
     *
     * 図で示すと[1]と[2]で補間されます。
     * The figure shows the interpolation between [1] and [2].
     *
     *
     * 補間の重みは最新の振り子計算タイミングと次回のタイミングの間で見た現在時間で決定する。
     * The weight of the interpolation are determined by the current time seen between
     * the latest pendulum calculation timing and the next timing.
     *
     *
     * 図で示すと[2]と[4]の間でみた(3)の位置の重みになる。
     * Figure shows the weight of position (3) as seen between [2] and [4].
     *
     *
     * 解釈として振り子計算のタイミングと重み計算のタイミングがズレる。
     * As an interpretation, the pendulum calculation and weights are misaligned.
     *
     *
     * physics3.jsonにFPS情報が存在しない場合は常に前の振り子状態で設定される。
     * If there is no FPS information in physics3.json, it is always set in the previous pendulum state.
     *
     *
     * この仕様は補間範囲を逸脱したことが原因の震えたような見た目を回避を目的にしている。
     * The purpose of this specification is to avoid the quivering appearance caused by deviations from the interpolation range.
     *
     *
     * ------------ time --------------&gt;
     *
     *
     * |+++++|------| &lt;- weight
     * ==[1]====#=====[2]---(3)----(4)
     * ^ output contents
     *
     *
     * 1:_previousRigOutputs
     * 2:_currentRigOutputs
     * 3:_currentRemainTime (now rendering)
     * 4:next particles timing
     *
     * @param model Model to which the results of physics operation are applied
     * @param deltaTimeSeconds rendering delta time[s]
     */
    fun evaluate(model: Live2DModel, deltaTimeSeconds: Float) {
        var weight: Float
        var radAngle: Float
        var outputValue: Float
//        var i: Int
        var particleIndex: Int
        val inputs: MutableList<Live2DPhysicsInternal.CubismPhysicsInput> = physicsRig.inputs
        val outputs: MutableList<Live2DPhysicsInternal.CubismPhysicsOutput> = physicsRig.outputs

        if (0.0f >= deltaTimeSeconds) {
            return
        }

        val physicsDeltaTime: Float
        currentRemainTime += deltaTimeSeconds
        if (currentRemainTime > MAX_DELTA_TIME) {
            currentRemainTime = 0.0f
        }

        val parameterValues = model.model.parameters.values // input
        val parameterMaximumValues = model.model.parameters.maximumValues
        val parameterMinimumValues = model.model.parameters.minimumValues
        val parameterDefaultValues = model.model.parameters.defaultValues

        if (parameterCaches.size < model.parameterCount) {
            parameterCaches = FloatArray(model.parameterCount)
        }

        if (parameterInputCaches.size < model.parameterCount) {
            parameterInputCaches = FloatArray(model.parameterCount)
            if (model.parameterCount >= 0) System.arraycopy(
                parameterValues,
                0,
                parameterInputCaches,
                0,
                model.parameterCount
            )
        }

        if (physicsRig.fps > 0.0f) {
            physicsDeltaTime = 1.0f / physicsRig.fps
        } else {
            physicsDeltaTime = deltaTimeSeconds
        }

//        var currentSetting: CubismPhysicsInternal.CubismPhysicsSubRig
//        var currentInput: CubismPhysicsInternal.CubismPhysicsInput
        var currentOutput: Live2DPhysicsInternal.CubismPhysicsOutput
        var currentParticle: Live2DPhysicsInternal.CubismPhysicsParticle

//        var settingIndex: Int
        while (currentRemainTime >= physicsDeltaTime) {
            // copy RigOutputs: _currentRigOutputs to _previousRigOutputs
            repeat(physicsRig.subRigCount) { settingIndex ->
                repeat(physicsRig.settings[settingIndex].outputCount) { outputIndex ->
                    previousRigOutputs[settingIndex]!!.outputs[outputIndex] =
                        currentRigOutputs[settingIndex]!!.outputs[outputIndex]
                }
            }

            // 入力キャッシュとパラメータで線形補間してUpdateParticlesするタイミングでの入力を計算する。
            // Calculate the input at the timing to UpdateParticles by linear interpolation with the _parameterInputCaches and parameterValues.
            // _parameterCachesはグループ間での値の伝搬の役割があるので_parameterInputCachesとの分離が必要。
            // _parameterCaches needs to be separated from _parameterInputCaches because of its role in propagating values between groups.
            val inputWeight = physicsDeltaTime / currentRemainTime
            repeat(model.parameterCount) {
                parameterCaches[it] =
                    parameterInputCaches[it] * (1.0f - inputWeight) + parameterValues[it] * inputWeight
                parameterInputCaches[it] = parameterCaches[it]
            }

            repeat(physicsRig.subRigCount) { settingIndex ->

                totalAngle[0] = 0.0f
                totalTranslation.setZero()

                val currentSetting = physicsRig.settings[settingIndex]
                val particles: MutableList<Live2DPhysicsInternal.CubismPhysicsParticle> =
                    physicsRig.particles

                val baseInputIndex: Int = currentSetting.baseInputIndex
                val baseOutputIndex: Int = currentSetting.baseOutputIndex
                val baseParticleIndex: Int = currentSetting.baseParticleIndex

                // Load input parameters.
                repeat(currentSetting.inputCount) {
                    val currentInput = inputs[baseInputIndex + it]
                    weight = currentInput.weight / MAXIMUM_WEIGHT

                    if (currentInput.sourceParameterIndex == -1) {
                        currentInput.sourceParameterIndex =
                            model.getParameterIndex(currentInput.source.Id)
                    }

                    currentInput.getNormalizedParameterValue!!.getNormalizedParameterValue(
                        totalTranslation,
                        totalAngle,
                        parameterCaches[currentInput.sourceParameterIndex],
                        parameterMinimumValues[currentInput.sourceParameterIndex],
                        parameterMaximumValues[currentInput.sourceParameterIndex],
                        parameterDefaultValues[currentInput.sourceParameterIndex],
                        currentSetting.normalizationPosition,
                        currentSetting.normalizationAngle,
                        currentInput.reflect,
                        weight
                    )
                }

                radAngle = CubismMath.degreesToRadian(-totalAngle[0])

                totalTranslation.x =
                    (totalTranslation.x * CubismMath.cosF(radAngle) - totalTranslation.y * CubismMath.sinF(
                        radAngle
                    ))
                totalTranslation.y =
                    (totalTranslation.x * CubismMath.sinF(radAngle) + totalTranslation.y * CubismMath.cosF(
                        radAngle
                    ))


                // Calculate particles position.
                updateParticles(
                    particles,
                    baseParticleIndex,
                    currentSetting.particleCount,
                    totalTranslation,
                    totalAngle[0],
                    options!!.wind,
                    MOVEMENT_THRESHOLD * currentSetting.normalizationPosition.maximumValue,
                    physicsDeltaTime,
                    AIR_RESISTANCE
                )

                // Update output parameters.
                repeat(currentSetting.outputCount) {
                    currentOutput = outputs.get(baseOutputIndex + it)
                    particleIndex = currentOutput.vertexIndex


                    if (currentOutput.destinationParameterIndex == -1) {
                        currentOutput.destinationParameterIndex =
                            model.getParameterIndex(currentOutput.destination.Id)
                    }

                    if (particleIndex < 1 || particleIndex >= currentSetting.particleCount) {
                        return@repeat
                    }

                    currentParticle = particles.get(baseParticleIndex + particleIndex)
                    val previousParticle = particles.get(baseParticleIndex + particleIndex - 1)
                    CubismVector2.subtract(
                        currentParticle.position,
                        previousParticle.position,
                        translation
                    )

                    outputValue = currentOutput.getValue!!.getValue(
                        translation,
                        particles,
                        baseParticleIndex,
                        particleIndex,
                        currentOutput.reflect,
                        options!!.gravity
                    )

                    currentRigOutputs.get(settingIndex)!!.outputs[it] = outputValue

                    cache[0] = parameterCaches[currentOutput.destinationParameterIndex]

                    updateOutputParameterValue(
                        cache,
                        0,
                        parameterMinimumValues[currentOutput.destinationParameterIndex],
                        parameterMaximumValues[currentOutput.destinationParameterIndex],
                        outputValue,
                        currentOutput
                    )
                    parameterCaches[currentOutput.destinationParameterIndex] = cache[0]
                }
            }
            currentRemainTime -= physicsDeltaTime
        }

        val alpha = currentRemainTime / physicsDeltaTime
        interpolate(model, alpha)
    }

    // There are only used by 'evaluate' method.
    // Avoid creating a new float array and CubismVector2 instance.
    private val totalAngle = FloatArray(1)
    private val cache = FloatArray(1)
    private val totalTranslation: CubismVector2 = CubismVector2()
    private val translation: CubismVector2 = CubismVector2()

    /**
     * Set an option.
     *
     * @param options a physics operation of option
     */
    fun setOptions(options: Options?) {
        if (options == null) {
            return
        }
        this.options = options
    }

    /**
     * Get the physics operation of option.
     *
     * @return the physics operation of option
     */
    fun getOptions(): Options? {
        return options
    }

    // -----private constants-----
    // Physics types tags
    private enum class PhysicsTypeTag(
        val tag: String
    ) {
        X("X"),
        Y("Y"),
        ANGLE("Angle");

    }

    /**
     * Parse a physics3.json data.
     *
     * @param physicsJson a buffer where physics3.json is loaded.
     */
    private fun parse(physicsJson: ByteArray) {
        physicsRig = Live2DPhysicsInternal.CubismPhysicsRig()

        val json = Json.decodeFromString<PhysicsJson>(String(physicsJson))

        physicsRig.gravity = CubismVector2(
            json.meta.effectiveForces.gravity.x.toFloat(),
            json.meta.effectiveForces.gravity.y.toFloat()
        )
        physicsRig.wind = CubismVector2(
            json.meta.effectiveForces.wind.x.toFloat(),
            json.meta.effectiveForces.wind.y.toFloat()
        )
        physicsRig.subRigCount = json.meta.physicsSettingCount

        physicsRig.fps = json.meta.fps.toFloat()

        physicsRig.settings =
            ArrayList(physicsRig.subRigCount)

        physicsRig.inputs =
            ArrayList(json.meta.totalInputCount)

        physicsRig.outputs =
            ArrayList(json.meta.totalOutputCount)

        physicsRig.particles =
            ArrayList(json.meta.vertexCount)

        currentRigOutputs.clear()
        previousRigOutputs.clear()

        var inputIndex = 0
        var outputIndex = 0
        var particleIndex = 0

        for (i in 0..<physicsRig.subRigCount) {
            val setting = Live2DPhysicsInternal.CubismPhysicsSubRig()

            // Setting
            setting.baseInputIndex = inputIndex
            setting.baseOutputIndex = outputIndex
            setting.baseParticleIndex = particleIndex
            parseSetting(json, setting, i)

            // Input
            parseInputs(json, i, setting.inputCount)
            inputIndex += setting.inputCount

            // Output
            parseOutputs(json, i, setting.outputCount)
            outputIndex += setting.outputCount

            // Particle
            parseParticles(json, i, setting.particleCount)
            particleIndex += setting.particleCount
        }
        initialize()
    }

    private fun parseSetting(
        json: PhysicsJson,
        setting: Live2DPhysicsInternal.CubismPhysicsSubRig,
        settingIndex: Int
    ) {
        setting.normalizationPosition.minimumValue =
            json.physicsSettings[settingIndex].normalization.position.minimum.toFloat()
        setting.normalizationPosition.maximumValue =
            json.physicsSettings[settingIndex].normalization.position.maximum.toFloat()
        setting.normalizationPosition.defaultValue =
            json.physicsSettings[settingIndex].normalization.position.default.toFloat()

        setting.normalizationAngle.minimumValue =
            json.physicsSettings[settingIndex].normalization.angle.minimum.toFloat()
        setting.normalizationAngle.maximumValue =
            json.physicsSettings[settingIndex].normalization.angle.maximum.toFloat()
        setting.normalizationAngle.defaultValue =
            json.physicsSettings[settingIndex].normalization.angle.default.toFloat()

        setting.inputCount = json.physicsSettings[settingIndex].input.size
        setting.outputCount = json.physicsSettings[settingIndex].output.size
        setting.particleCount = json.physicsSettings[settingIndex].vertices.size

        physicsRig.settings.add(setting)
    }

    private fun parseInputs(json: PhysicsJson, settingIndex: Int, inputCount: Int) {
        for (inputIndex in 0..<inputCount) {
            val input = Live2DPhysicsInternal.CubismPhysicsInput()

            input.sourceParameterIndex = -1
            input.weight = json.physicsSettings[settingIndex].input[inputIndex].weight.toFloat()
            input.reflect = json.physicsSettings[settingIndex].input[inputIndex].reflect

            val tag: String = json.physicsSettings[settingIndex].input[inputIndex].type

            if (tag == PhysicsTypeTag.X.tag) {
                input.type = Live2DPhysicsInternal.CubismPhysicsSource.X
                input.getNormalizedParameterValue =
                    Live2DPhysicsFunctions.GetInputTranslationXFromNormalizedParameterValue()
            } else if (tag == PhysicsTypeTag.Y.tag) {
                input.type = Live2DPhysicsInternal.CubismPhysicsSource.Y
                input.getNormalizedParameterValue =
                    Live2DPhysicsFunctions.GetInputTranslationYFromNormalizedParameterValue()
            } else if (tag == PhysicsTypeTag.ANGLE.tag) {
                input.type = Live2DPhysicsInternal.CubismPhysicsSource.ANGLE
                input.getNormalizedParameterValue =
                    Live2DPhysicsFunctions.GetInputAngleFromNormalizedParameterValue()
            }

            input.source.targetType = Live2DPhysicsInternal.CubismPhysicsTargetType.PARAMETER

            input.source.Id =
                Live2DIdManager.id(json.physicsSettings[settingIndex].input[inputIndex].source.id)

            physicsRig.inputs.add(input)
        }
    }

    private fun parseOutputs(json: PhysicsJson, settingIndex: Int, outputCount: Int) {
        val count: Int = physicsRig.settings.get(settingIndex).outputCount

        currentRigOutputs.add(PhysicsOutput(FloatArray(count)))
        previousRigOutputs.add(PhysicsOutput(FloatArray(count)))

        for (outputIndex in 0..<outputCount) {
            val output = Live2DPhysicsInternal.CubismPhysicsOutput()

            output.destinationParameterIndex = -1
            output.vertexIndex = json.physicsSettings[settingIndex].output[outputIndex].vertexIndex
            output.angleScale = json.physicsSettings[settingIndex].output[outputIndex].scale
            output.weight = json.physicsSettings[settingIndex].output[outputIndex].weight
            output.destination.targetType = Live2DPhysicsInternal.CubismPhysicsTargetType.PARAMETER

            output.destination.Id = Live2DIdManager.id(
                json.physicsSettings[settingIndex].output[outputIndex].destination.id
            )

            val tag: String = json.physicsSettings[settingIndex].output[outputIndex].type
            if (tag == PhysicsTypeTag.X.tag) {
                output.type = Live2DPhysicsInternal.CubismPhysicsSource.X
                output.getValue = Live2DPhysicsFunctions.GetOutputTranslationX()
                output.getScale = Live2DPhysicsFunctions.GetOutputScaleTranslationX()
            } else if (tag == PhysicsTypeTag.Y.tag) {
                output.type = Live2DPhysicsInternal.CubismPhysicsSource.Y
                output.getValue = Live2DPhysicsFunctions.GetOutputTranslationY()
                output.getScale = Live2DPhysicsFunctions.GetOutputScaleTranslationY()
            } else if (tag == PhysicsTypeTag.ANGLE.tag) {
                output.type = Live2DPhysicsInternal.CubismPhysicsSource.ANGLE
                output.getValue = Live2DPhysicsFunctions.GetOutputAngle()
                output.getScale = Live2DPhysicsFunctions.GetOutputScaleAngle()
            }

            output.reflect = json.physicsSettings[settingIndex].output[outputIndex].reflect

            physicsRig.outputs.add(output)
        }
    }

    private fun parseParticles(json: PhysicsJson, settingIndex: Int, particleCount: Int) {
        for (particleIndex in 0..<particleCount) {
            val particle = Live2DPhysicsInternal.CubismPhysicsParticle()

            particle.mobility = json.physicsSettings[settingIndex].vertices[particleIndex].mobility
            particle.delay = json.physicsSettings[settingIndex].vertices[particleIndex].delay
            particle.acceleration =
                json.physicsSettings[settingIndex].vertices[particleIndex].acceleration
            particle.radius = json.physicsSettings[settingIndex].vertices[particleIndex].radius
            particle.position = CubismVector2(
                json.physicsSettings[settingIndex].vertices[particleIndex].position.x,
                json.physicsSettings[settingIndex].vertices[particleIndex].position.y,
            )

            physicsRig.particles.add(particle)
        }
    }

    /**
     * Initializes physics
     */
    private fun initialize() {
        for (settingIndex in 0..<physicsRig.subRigCount) {
            val currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig = physicsRig.settings.get(settingIndex)

            val baseIndex: Int = currentSetting.baseParticleIndex
            val baseParticle: Live2DPhysicsInternal.CubismPhysicsParticle = physicsRig.particles.get(baseIndex)

            // Initialize the top of particle
            baseParticle.initialPosition = CubismVector2()
            baseParticle.lastPosition = CubismVector2(baseParticle.initialPosition)
            baseParticle.lastGravity = CubismVector2(0.0f, 1.0f)
            baseParticle.velocity = CubismVector2()
            baseParticle.force = CubismVector2()

            // Initialize particles
            for (i in 1..<currentSetting.particleCount) {
                val currentParticle: Live2DPhysicsInternal.CubismPhysicsParticle = physicsRig.particles.get(baseIndex + i)

                val radius: CubismVector2 = CubismVector2(0.0f, currentParticle.radius)

                val previousPosition: CubismVector2 =
                    CubismVector2(physicsRig.particles.get(baseIndex + i - 1).initialPosition)
                currentParticle.initialPosition = previousPosition.add(radius)

                currentParticle.position = CubismVector2(currentParticle.initialPosition)
                currentParticle.lastPosition = CubismVector2(currentParticle.initialPosition)
                currentParticle.lastGravity = CubismVector2(0.0f, 1.0f)
                currentParticle.velocity = CubismVector2()
                currentParticle.force = CubismVector2()
            }
        }
    }

    /**
     * 引数で与えられたモデルに、振り子演算の最新の結果とその1つ前の結果、及び与えられた重みから算出された物理演算の結果を適用する。
     * Apply the result of the physics operation calculated from the latest result of the pendulum operation, the previous one, and the given weights, to the model given in the argument.
     *
     * @param model model applied the result of physics operation
     * @param weight weight of the latest result
     */
    private fun interpolate(model: Live2DModel, weight: Float) {
        for (settingIndex in 0..<physicsRig.subRigCount) {
            val currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig = physicsRig.settings.get(settingIndex)
            val outputs: MutableList<Live2DPhysicsInternal.CubismPhysicsOutput> = physicsRig.outputs
            val baseOutputIndex: Int = currentSetting.baseOutputIndex

            val parameterValues = model.model.parameters.values
            val parameterMaximumValues = model.model.parameters.maximumValues
            val parameterMinimumValues = model.model.parameters.minimumValues

            tmpValue[0] = 0.0f

            for (i in 0..<currentSetting.outputCount) {
                val currentOutput: Live2DPhysicsInternal.CubismPhysicsOutput = outputs.get(baseOutputIndex + i)

                if (currentOutput.destinationParameterIndex === -1) {
                    continue
                }

                tmpValue[0] = parameterValues[currentOutput.destinationParameterIndex]

                updateOutputParameterValue(
                    tmpValue,
                    0,
                    parameterMinimumValues[currentOutput.destinationParameterIndex],
                    parameterMaximumValues[currentOutput.destinationParameterIndex],
                    previousRigOutputs.get(settingIndex)!!.outputs[i] * (1 - weight) + currentRigOutputs.get(
                        settingIndex
                    )!!.outputs[i] * weight,
                    currentOutput
                )
                parameterValues[currentOutput.destinationParameterIndex] = tmpValue[0]
            }
        }
    }

    // This is only used by 'interpolate' method.
    // Avoid creating a new float array instance.
    private val tmpValue = FloatArray(1)

    /**
     * Load input parameters.
     * (Used for only evaluate() method.)
     *
     * @param model model instance
     * @param transition total amount of model's transition
     * @param settingIndex index of setting
     * @return total amount of model's angle
     */
    private fun loadInputParameters(
        model: Live2DModel,
        transition: CubismVector2,
        settingIndex: Int
    ): Float {
        val currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig = physicsRig.settings.get(settingIndex)

        val totalAngle = FloatArray(1)

        for (i in 0..<currentSetting.inputCount) {
            val currentInput: Live2DPhysicsInternal.CubismPhysicsInput =
                physicsRig.inputs.get(currentSetting.baseInputIndex + i)
            val weight: Float = currentInput.weight / MAXIMUM_WEIGHT

            if (currentInput.sourceParameterIndex === -1) {
                currentInput.sourceParameterIndex = model.getParameterIndex(currentInput.source.Id)
            }

            val parameterValue: Float = model.getParameterValue(currentInput.sourceParameterIndex)
            val parameterMaximumValue: Float =
                model.getParameterMaximumValue(currentInput.sourceParameterIndex)
            val parameterMinimumValue: Float =
                model.getParameterMinimumValue(currentInput.sourceParameterIndex)
            val parameterDefaultValue: Float =
                model.getParameterDefaultValue(currentInput.sourceParameterIndex)

            currentInput.getNormalizedParameterValue!!.getNormalizedParameterValue(
                transition,
                totalAngle,
                parameterValue,
                parameterMinimumValue,
                parameterMaximumValue,
                parameterDefaultValue,
                currentSetting.normalizationPosition,
                currentSetting.normalizationAngle,
                currentInput.reflect,
                weight
            )
        }
        return totalAngle[0]
    }


    /**
     * Physics operation data
     */
    private lateinit var physicsRig: Live2DPhysicsInternal.CubismPhysicsRig

    /**
     * Options of physics operation
     */
    private var options: Options? = Options()

    /**
     * Result of the latest pendulum calculation
     */
    private val currentRigOutputs: MutableList<PhysicsOutput?> = ArrayList<PhysicsOutput?>()

    /**
     * Result of one previous pendulum calculation
     */
    private val previousRigOutputs: MutableList<PhysicsOutput?> = ArrayList<PhysicsOutput?>()

    /**
     * Time not processed by physics
     */
    private var currentRemainTime = 0f

    /**
     * Cache of parameter used in 'Evaluate' method
     */
    private var parameterCaches = FloatArray(1)

    /**
     * Cache of parameter input in 'UpdateParticles' method
     */
    private var parameterInputCaches = FloatArray(1)

    companion object {

        /**
         * Updates particles
         *
         * @param strand Target array of particle
         * @param strandCount Count of particle
         * @param totalTranslation Total translation value
         * @param totalAngle Total angle
         * @param windDirection Direction of wind
         * @param thresholdValue Threshold of movement
         * @param deltaTimeSeconds Delta time
         * @param airResistance Air resistance
         */
        private fun updateParticles(
            strand: MutableList<Live2DPhysicsInternal.CubismPhysicsParticle>,
            baseParticleIndex: Int,
            strandCount: Int,
            totalTranslation: CubismVector2,
            totalAngle: Float,
            windDirection: CubismVector2,
            thresholdValue: Float,
            deltaTimeSeconds: Float,
            airResistance: Float
        ) {
            val totalRadian: Float
            var delay: Float
            var radian: Float

            strand.get(baseParticleIndex).position.set(totalTranslation.x, totalTranslation.y)

            totalRadian = CubismMath.degreesToRadian(totalAngle)
            CubismMath.radianToDirection(totalRadian, currentGravity).normalize()

            for (i in 1..<strandCount) {
                val currentParticle: Live2DPhysicsInternal.CubismPhysicsParticle = strand.get(baseParticleIndex + i)
                val previousParticle: Live2DPhysicsInternal.CubismPhysicsParticle = strand.get(baseParticleIndex + i - 1)

                currentParticle.lastPosition.set(
                    currentParticle.position.x,
                    currentParticle.position.y
                )

                run {
                    CubismVector2.multiply(
                        Companion.currentGravity,
                        currentParticle.acceleration,
                        currentParticle.force
                    ).add(windDirection)
                    delay = currentParticle.delay * deltaTimeSeconds * 30.0f
                    CubismVector2.subtract(
                        currentParticle.position,
                        previousParticle.position,
                        Companion.direction
                    )
                }
                run {
                    radian = CubismMath.directionToRadian(
                        currentParticle.lastGravity,
                        Companion.currentGravity
                    ) / airResistance
                    Companion.direction.x =
                        ((CubismMath.cosF(radian) * Companion.direction.x) - (CubismMath.sinF(radian) * Companion.direction.y))
                    Companion.direction.y =
                        ((CubismMath.sinF(radian) * Companion.direction.x) + (Companion.direction.y * CubismMath.cosF(
                            radian
                        )))
                }
                run {
                    CubismVector2.add(
                        previousParticle.position,
                        Companion.direction,
                        currentParticle.position
                    )
                    CubismVector2.multiply(currentParticle.velocity, delay, Companion.velocity)
                    CubismVector2.multiply(currentParticle.force, delay, Companion.force)
                        .multiply(delay)
                    currentParticle.position.add(Companion.velocity).add(Companion.force)
                }
                run {
                    var newDirectionX: Float =
                        currentParticle.position.x - previousParticle.position.x
                    var newDirectionY: Float =
                        currentParticle.position.y - previousParticle.position.y
                    val length =
                        (((newDirectionX * newDirectionX) + (newDirectionY * newDirectionY)).toDouble()
                            .pow(0.5)).toFloat()
                    newDirectionX /= length
                    newDirectionY /= length

                    currentParticle.position.x =
                        previousParticle.position.x + (newDirectionX * currentParticle.radius)
                    currentParticle.position.y =
                        previousParticle.position.y + (newDirectionY * currentParticle.radius)
                }

                if (CubismMath.absF(currentParticle.position.x) < thresholdValue) {
                    currentParticle.position.x = 0.0f
                }

                if (delay != 0.0f) {
                    CubismVector2.subtract(
                        currentParticle.position,
                        currentParticle.lastPosition,
                        currentParticle.velocity
                    )

                    currentParticle.velocity.divide(delay)
                    currentParticle.velocity.multiply(currentParticle.mobility)
                }
                currentParticle.force.setZero()
                currentParticle.lastGravity.set(currentGravity.x, currentGravity.y)
            }
        }

        // There are only used by 'updateParticles' method.
        // Avoid creating a new CubismVector2 instance.
        private val direction: CubismVector2 = CubismVector2()
        private val velocity: CubismVector2 = CubismVector2()
        private val force: CubismVector2 = CubismVector2()
        private val currentGravity: CubismVector2 = CubismVector2()

        private fun updateParticlesForStabilization(
            strand: MutableList<Live2DPhysicsInternal.CubismPhysicsParticle>,
            baseParticleIndex: Int,
            strandCount: Int,
            totalTranslation: CubismVector2,
            totalAngle: Float,
            windDirection: CubismVector2,
            thresholdValue: Float
        ) {
            var i: Int
            val totalRadian: Float

            strand.get(baseParticleIndex).position.set(totalTranslation.x, totalTranslation.y)

            totalRadian = CubismMath.degreesToRadian(totalAngle)
            CubismMath.radianToDirection(totalRadian, currentGravityForStablization).normalize()

            i = 1
            while (i < strandCount) {
                val particle: Live2DPhysicsInternal.CubismPhysicsParticle = strand.get(baseParticleIndex + i)
                CubismVector2.multiply(
                    currentGravityForStablization,
                    particle.acceleration,
                    particle.force
                ).add(windDirection)

                particle.lastPosition.set(particle.position.x, particle.position.y)
                particle.velocity.setZero()

                forceForStabilization.set(particle.force.x, particle.force.y)
                forceForStabilization.normalize()

                forceForStabilization.multiply(particle.radius)
                CubismVector2.add(
                    strand.get(baseParticleIndex + i - 1).position,
                    forceForStabilization,
                    particle.position
                )

                if (CubismMath.absF(particle.position.x) < thresholdValue) {
                    particle.position.x = 0.0f
                }

                particle.force.setZero()
                particle.lastGravity.set(
                    currentGravityForStablization.x,
                    currentGravityForStablization.y
                )
                i++
            }
        }

        // updateParticlesForStabilization関数内でのみ使われるキャッシュ変数
        private val currentGravityForStablization: CubismVector2 = CubismVector2()
        private val forceForStabilization: CubismVector2 = CubismVector2()

        private fun updateOutputParameterValue(
            parameterValue: FloatArray,
            destinationParameterIndex: Int,
            parameterValueMinimum: Float,
            parameterValueMaximum: Float,
            translation: Float,
            output: Live2DPhysicsInternal.CubismPhysicsOutput
        ) {
            val outputScale: Float
            var value: Float
            val weight: Float

            outputScale = output.getScale!!.getScale(
                output.transitionScale,
                output.angleScale
            )

            value = translation * outputScale

            if (value < parameterValueMinimum) {
                if (value < output.valueBelowMinimum) {
                    output.valueBelowMinimum = value
                }

                value = parameterValueMinimum
            } else if (value > parameterValueMaximum) {
                if (value > output.valueExceededMaximum) {
                    output.valueExceededMaximum = value
                }

                value = parameterValueMaximum
            }

            weight = output.weight / MAXIMUM_WEIGHT

            if (!(weight >= 1.0f)) {
                value =
                    (parameterValue[destinationParameterIndex] * (1.0f - weight)) + (value * weight)
            }
            parameterValue[destinationParameterIndex] = value
        }

        /**
         * Constant of air resistance
         */
        private const val AIR_RESISTANCE = 5.0f

        /**
         * Constant of maximum weight of input and output ratio
         */
        private const val MAXIMUM_WEIGHT = 100.0f

        /**
         * Constant of threshold of movement
         */
        private const val MOVEMENT_THRESHOLD = 0.001f

        /**
         * Constant of maximum allowed delta time
         */
        private const val MAX_DELTA_TIME = 5.0f
    }
}
