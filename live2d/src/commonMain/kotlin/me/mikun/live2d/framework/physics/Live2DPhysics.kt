/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.physics

import com.live2d.sdk.cubism.framework.math.CubismMath
import com.live2d.sdk.cubism.framework.math.CubismVector2
import me.mikun.live2d.framework.model.Live2DModel
import me.mikun.live2d.framework.data.PhysicsJson
import me.mikun.live2d.framework.id.Live2DIdManager
import kotlinx.serialization.json.Json
import me.mikun.live2d.framework.physics.Live2DPhysicsInternal.CubismPhysicsNormalization
import me.mikun.live2d.framework.physics.Live2DPhysicsInternal.CubismPhysicsParameter
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
            val setting = Live2DPhysicsInternal.CubismPhysicsSubRig(
                baseInputIndex = inputIndex,
                baseOutputIndex = outputIndex,
                baseParticleIndex = particleIndex,
                inputCount = json.physicsSettings[i].input.size,
                outputCount = json.physicsSettings[i].output.size,
                particleCount = json.physicsSettings[i].vertices.size,
                normalizationPosition = CubismPhysicsNormalization(
                    minimumValue = json.physicsSettings[i].normalization.position.minimum.toFloat(),
                    maximumValue = json.physicsSettings[i].normalization.position.maximum.toFloat(),
                    defaultValue = json.physicsSettings[i].normalization.position.default.toFloat(),
                ),
                normalizationAngle = CubismPhysicsNormalization(
                    minimumValue = json.physicsSettings[i].normalization.angle.minimum.toFloat(),
                    maximumValue = json.physicsSettings[i].normalization.angle.maximum.toFloat(),
                    defaultValue = json.physicsSettings[i].normalization.angle.default.toFloat(),
                )
            ).also {
                physicsRig.settings.add(it)
            }

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

            input.source = CubismPhysicsParameter(
                id = Live2DIdManager.id(
                    json.physicsSettings[settingIndex].input[inputIndex].source.id
                ),
                targetType = Live2DPhysicsInternal.CubismPhysicsTargetType.PARAMETER
            )
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
            output.scale = json.physicsSettings[settingIndex].output[outputIndex].scale
            output.weight = json.physicsSettings[settingIndex].output[outputIndex].weight
            output.destination = CubismPhysicsParameter(
                id = Live2DIdManager.id(
                    json.physicsSettings[settingIndex].output[outputIndex].destination.id
                ),
                targetType = Live2DPhysicsInternal.CubismPhysicsTargetType.PARAMETER
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
            val particle = Live2DPhysicsInternal.CubismPhysicsParticle(
                position = CubismVector2(
                    json.physicsSettings[settingIndex].vertices[particleIndex].position.x,
                    json.physicsSettings[settingIndex].vertices[particleIndex].position.y,
                ),
                mobility = json.physicsSettings[settingIndex].vertices[particleIndex].mobility,
                delay = json.physicsSettings[settingIndex].vertices[particleIndex].delay,
                acceleration = json.physicsSettings[settingIndex].vertices[particleIndex].acceleration,
                radius = json.physicsSettings[settingIndex].vertices[particleIndex].radius
            )
            physicsRig.particles.add(particle)
        }
    }


    /**
     * Initializes physics
     */
    private fun initialize() {
        for (settingIndex in 0..<physicsRig.subRigCount) {
            val currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig =
                physicsRig.settings.get(settingIndex)

            val baseIndex: Int = currentSetting.baseParticleIndex
            val baseParticle: Live2DPhysicsInternal.CubismPhysicsParticle =
                physicsRig.particles.get(baseIndex)

            // Initialize the top of particle
            baseParticle.initialPosition = CubismVector2()
            baseParticle.lastPosition = CubismVector2(baseParticle.initialPosition)
            baseParticle.lastGravity = CubismVector2(0.0f, 1.0f)
            baseParticle.velocity = CubismVector2()
            baseParticle.force = CubismVector2()

            // Initialize particles
            for (i in 1..<currentSetting.particleCount) {
                val currentParticle: Live2DPhysicsInternal.CubismPhysicsParticle =
                    physicsRig.particles.get(baseIndex + i)

                val radius = CubismVector2(0.0f, currentParticle.radius)

                val previousPosition =
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


    private enum class PhysicsTypeTag(
        val tag: String,
    ) {
        X("X"),
        Y("Y"),
        ANGLE("Angle");

    }


    /**
     * Options of physics operation
     * @see PhysicsJson.Meta.EffectiveForces
     */
    class Options(
        val wind: CubismVector2 = CubismVector2(),
        val gravity: CubismVector2 = CubismVector2(),

        )

    /**
     * Output result of physics operations before applying to parameters
     */
    class PhysicsOutput(
        var outputs: FloatArray,
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
        val totalTranslation = CubismVector2()
        var i: Int
        var settingIndex: Int
        var particleIndex: Int
        var currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig

        if (parameterCaches.size < model.parameterCount) {
            parameterCaches = FloatArray(model.parameterCount)
        }
        if (parameterInputCaches.size < model.parameterCount) {
            parameterInputCaches = FloatArray(model.parameterCount)
        }

        for (j in 0..<model.parameterCount) {
            model.getParameterValue(j).let {
                parameterCaches[j] = it
                parameterInputCaches[j] = it
            }
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
                val currentInput: Live2DPhysicsInternal.CubismPhysicsInput =
                    physicsRig.inputs.get(baseInputIndex + i)

                weight = currentInput.weight / MAXIMUM_WEIGHT

                if (currentInput.sourceParameterIndex == -1) {
                    currentInput.sourceParameterIndex =
                        model.getParameterIndex(currentInput.source.id)
                }

                currentInput.getNormalizedParameterValue!!.getNormalizedParameterValue(
                    totalTranslation,
                    totalAngle,
                    model.getParameterValue(currentInput.sourceParameterIndex),
                    model.getParameterMinimumValue(currentInput.sourceParameterIndex),
                    model.getParameterMaximumValue(currentInput.sourceParameterIndex),
                    model.getParameterDefaultValue(currentInput.sourceParameterIndex),
                    currentSetting.normalizationPosition,
                    currentSetting.normalizationAngle,
                    currentInput.reflect,
                    weight
                )

                parameterCaches[currentInput.sourceParameterIndex] =
                    model.getParameterValue(currentInput.sourceParameterIndex)

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
                val currentOutput: Live2DPhysicsInternal.CubismPhysicsOutput =
                    physicsRig.outputs.get(baseOutputIndex + i)

                particleIndex = currentOutput.vertexIndex

                if (currentOutput.destinationParameterIndex == -1) {
                    currentOutput.destinationParameterIndex =
                        model.getParameterIndex(currentOutput.destination.id)
                }

                if (particleIndex < 1 || particleIndex >= currentSetting.particleCount) {
                    i++
                    continue
                }

                val translation = CubismVector2()
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

                // TODO:: 真的需要在这里set吗
                model.setParameterValue(
                    currentOutput.destinationParameterIndex,
                    updateOutputParameterValue(
                        model.getParameterValue(currentOutput.destinationParameterIndex),
                        model.getParameterMinimumValue(currentOutput.destinationParameterIndex),
                        model.getParameterMaximumValue(currentOutput.destinationParameterIndex),
                        outputValue,
                        currentOutput
                    )
                )

                parameterCaches[currentOutput.destinationParameterIndex] =
                    model.getParameterValue(currentOutput.destinationParameterIndex)

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
     * @param deltaSeconds rendering delta time[s]
     */
    fun update(model: Live2DModel, deltaSeconds: Float) {

        /*
            更新计时器
         */
        run {
            if (deltaSeconds <= 0.0f) {
                return
            }

            currentRemainTime += deltaSeconds
            if (currentRemainTime > MAX_DELTA_TIME) {
                currentRemainTime = 0.0f
            }
        }

        if (parameterCaches.size < model.parameterCount) {
            parameterCaches = FloatArray(model.parameterCount)
        }

        if (parameterInputCaches.size < model.parameterCount) {
            parameterInputCaches = FloatArray(model.parameterCount) {
                model.getParameterValue(it)
            }
        }

        val physicsDeltaTime =
            if (physicsRig.fps > 0.0f) {
                1.0f / physicsRig.fps
            } else {
                deltaSeconds
            }

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
                    parameterInputCaches[it] * (1.0f - inputWeight) + model.getParameterValue(it) * inputWeight
                parameterInputCaches[it] = parameterCaches[it]
            }

            repeat(physicsRig.subRigCount) { settingIndex ->
                val totalAngle = FloatArray(1)
                val totalTranslation = CubismVector2(0.0f, 0.0f)

                val currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig =
                    physicsRig.settings[settingIndex]
                val particles: MutableList<Live2DPhysicsInternal.CubismPhysicsParticle> =
                    physicsRig.particles

                val baseInputIndex: Int = currentSetting.baseInputIndex
                val baseOutputIndex: Int = currentSetting.baseOutputIndex
                val baseParticleIndex: Int = currentSetting.baseParticleIndex

                // Load input parameters.
                repeat(currentSetting.inputCount) {
                    val currentInput: Live2DPhysicsInternal.CubismPhysicsInput =
                        physicsRig.inputs[baseInputIndex + it]
                    val weight = currentInput.weight / MAXIMUM_WEIGHT

                    if (currentInput.sourceParameterIndex == -1) {
                        currentInput.sourceParameterIndex =
                            model.getParameterIndex(currentInput.source.id)
                    }

                    currentInput.getNormalizedParameterValue!!.getNormalizedParameterValue(
                        totalTranslation,
                        totalAngle,
                        parameterCaches[currentInput.sourceParameterIndex],
                        model.getParameterMinimumValue(currentInput.sourceParameterIndex),
                        model.getParameterMaximumValue(currentInput.sourceParameterIndex),
                        model.getParameterDefaultValue(currentInput.sourceParameterIndex),
                        currentSetting.normalizationPosition,
                        currentSetting.normalizationAngle,
                        currentInput.reflect,
                        weight
                    )
                }

                val radAngle = CubismMath.degreesToRadian(-totalAngle[0])

                totalTranslation.x =
                    totalTranslation.x * CubismMath.cosF(radAngle) - totalTranslation.y * CubismMath.sinF(
                        radAngle
                    )
                totalTranslation.y =
                    totalTranslation.x * CubismMath.sinF(radAngle) + totalTranslation.y * CubismMath.cosF(
                        radAngle
                    )


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
                    val currentOutput: Live2DPhysicsInternal.CubismPhysicsOutput =
                        physicsRig.outputs[baseOutputIndex + it]
                    val particleIndex = currentOutput.vertexIndex

                    if (currentOutput.destinationParameterIndex == -1) {
                        currentOutput.destinationParameterIndex =
                            model.getParameterIndex(currentOutput.destination.id)
                    }

                    if (particleIndex < 1 || particleIndex >= currentSetting.particleCount) {
                        return@repeat
                    }

                    val currentParticle: Live2DPhysicsInternal.CubismPhysicsParticle =
                        particles[baseParticleIndex + particleIndex]
                    val previousParticle = particles[baseParticleIndex + particleIndex - 1]

                    val translation = CubismVector2()
                    CubismVector2.subtract(
                        currentParticle.position,
                        previousParticle.position,
                        translation
                    )

                    val outputValue = currentOutput.getValue!!.getValue(
                        translation,
                        particles,
                        baseParticleIndex,
                        particleIndex,
                        currentOutput.reflect,
                        options!!.gravity
                    )

                    currentRigOutputs[settingIndex]!!.outputs[it] = outputValue

                    parameterCaches[currentOutput.destinationParameterIndex] =
                        updateOutputParameterValue(
                            parameterCaches[currentOutput.destinationParameterIndex],
                            model.getParameterMinimumValue(currentOutput.destinationParameterIndex),
                            model.getParameterMaximumValue(currentOutput.destinationParameterIndex),
                            outputValue,
                            currentOutput
                        )
                }
            }
            currentRemainTime -= physicsDeltaTime
        }

        val alpha = currentRemainTime / physicsDeltaTime
        interpolate(model, alpha)
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
            val currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig =
                physicsRig.settings[settingIndex]

            for (i in 0..<currentSetting.outputCount) {
                val currentOutput: Live2DPhysicsInternal.CubismPhysicsOutput =
                    physicsRig.outputs[currentSetting.baseOutputIndex + i]

                if (currentOutput.destinationParameterIndex == -1) {
                    continue
                }

                model.setParameterValue(
                    currentOutput.destinationParameterIndex,

                    updateOutputParameterValue(
                        model.getParameterValue(currentOutput.destinationParameterIndex),
                        model.getParameterMinimumValue(currentOutput.destinationParameterIndex),
                        model.getParameterMaximumValue(currentOutput.destinationParameterIndex),
                        previousRigOutputs.get(settingIndex)!!.outputs[i] * (1 - weight) + currentRigOutputs.get(
                            settingIndex
                        )!!.outputs[i] * weight,
                        currentOutput
                    )
                )
            }
        }
    }

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
        settingIndex: Int,
    ): Float {
        val currentSetting: Live2DPhysicsInternal.CubismPhysicsSubRig =
            physicsRig.settings.get(settingIndex)

        val totalAngle = FloatArray(1)

        for (i in 0..<currentSetting.inputCount) {
            val currentInput: Live2DPhysicsInternal.CubismPhysicsInput =
                physicsRig.inputs.get(currentSetting.baseInputIndex + i)
            val weight: Float = currentInput.weight / MAXIMUM_WEIGHT

            if (currentInput.sourceParameterIndex == -1) {
                currentInput.sourceParameterIndex = model.getParameterIndex(currentInput.source.id)
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
    var options: Options? = Options()

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
            airResistance: Float,
        ) {
            var delay: Float
            var radian: Float

            strand.get(baseParticleIndex).position.set(totalTranslation.x, totalTranslation.y)

            val totalRadian = CubismMath.degreesToRadian(totalAngle)
            CubismMath.radianToDirection(totalRadian, currentGravity).normalize()

            for (i in 1..<strandCount) {
                val currentParticle: Live2DPhysicsInternal.CubismPhysicsParticle =
                    strand.get(baseParticleIndex + i)
                val previousParticle: Live2DPhysicsInternal.CubismPhysicsParticle =
                    strand.get(baseParticleIndex + i - 1)

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
            thresholdValue: Float,
        ) {

            strand.get(baseParticleIndex).position.set(totalTranslation.x, totalTranslation.y)

            val totalRadian: Float = CubismMath.degreesToRadian(totalAngle)
            CubismMath.radianToDirection(totalRadian, currentGravityForStablization).normalize()

            var i = 1
            while (i < strandCount) {
                val particle: Live2DPhysicsInternal.CubismPhysicsParticle =
                    strand.get(baseParticleIndex + i)
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
            destinationParameterValue: Float,
            parameterValueMinimum: Float,
            parameterValueMaximum: Float,
            translation: Float,
            output: Live2DPhysicsInternal.CubismPhysicsOutput,
        ): Float {

            val outputScale = output.getScale!!.getScale(
                output.transitionScale,
                output.scale
            )

            var value = translation * outputScale

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

            val weight = output.weight / MAXIMUM_WEIGHT

            if (!(weight >= 1.0f)) {
                value =
                    (destinationParameterValue * (1.0f - weight)) + (value * weight)
            }

            return value
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
