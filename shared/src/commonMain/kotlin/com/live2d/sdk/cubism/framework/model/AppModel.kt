package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.CubismFramework.idManager
import com.live2d.sdk.cubism.framework.ParameterId
import com.live2d.sdk.cubism.framework.effect.CubismBreath
import com.live2d.sdk.cubism.framework.effect.CubismBreath.BreathParameterData
import com.live2d.sdk.cubism.framework.effect.CubismEyeBlink
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotion
import com.live2d.sdk.cubism.framework.motion.CubismMotion
import com.live2d.sdk.cubism.framework.utils.json.ModelJson
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.readBytes

class AppModel : CubismUserModel() {
    private val idParamAngleX: CubismId = idManager.id(ParameterId.ANGLE_X.id)
    private val idParamAngleY: CubismId = idManager.id(ParameterId.ANGLE_Y.id)
    private val idParamAngleZ: CubismId = idManager.id(ParameterId.ANGLE_Z.id)
    private val idParamBodyAngleX: CubismId = idManager.id(ParameterId.BODY_ANGLE_X.id)
    private val idParamEyeBallX: CubismId = idManager.id(ParameterId.EYE_BALL_X.id)
    private val idParamEyeBallY: CubismId = idManager.id(ParameterId.EYE_BALL_Y.id)

    private val motionMap: MutableMap<String, CubismMotion> = mutableMapOf()
    private val expressionMap: MutableMap<String, CubismExpressionMotion?> = mutableMapOf()


    fun init(dir: String, modelJsonFileName: String) {
        val buffer = Path(dir, modelJsonFileName).readBytes()
        val modelJson = Json.decodeFromString<ModelJson>(String(buffer))
        setupModel(dir, modelJson)

        setupRenderer()
        setupTextures()

    }

    fun setupModel(dir: String, modelJson: ModelJson) {
        Path(dir, modelJson.fileReferences.moc).readBytes().let { buffer ->
            loadModel(buffer, true)
        }

        Path(dir, modelJson.fileReferences.physics).readBytes().let { Buffer ->
            loadPhysics(Buffer)
        }

        Path(dir, modelJson.fileReferences.pose).readBytes().let { buffer ->
            loadPose(buffer)
        }

        for (expression in modelJson.fileReferences.expressions) {
            Path(dir, expression.file).readBytes().let { buffer ->
                expressionMap.put(expression.name, loadExpression(buffer))
            }
        }

        Path(dir, modelJson.fileReferences.userData).readBytes().let { buffer ->
            loadUserData(buffer)
        }

        // Load eye blink data
        // TODO:: save it
        CubismEyeBlink(modelJson)

        // Load Breath Data
        // TODO:: save it
        CubismBreath(
            BreathParameterData(idParamAngleX, 0.0f, 15.0f, 6.5345f, 0.5f),
            BreathParameterData(idParamAngleY, 0.0f, 8.0f, 3.5345f, 0.5f),
            BreathParameterData(idParamAngleZ, 0.0f, 10.0f, 5.5345f, 0.5f),
            BreathParameterData(idParamBodyAngleX, 0.0f, 4.0f, 15.5345f, 0.5f),
            BreathParameterData(
                idManager.id(ParameterId.BREATH.id),
                0.5f,
                0.5f,
                3.2345f,
                0.5f,
            )
        )

        // TODO:: layout
//        modelJson.layout

        model!!.saveParameters()

        // Just preload motions
        for ((name, motionGroup) in modelJson.fileReferences.motionGroups) {
            motionGroup.forEachIndexed { index, motion ->
                Path(dir, motion.file).readBytes().let { buffer ->
                    loadMotion(buffer)?.let {
                        it.fadeInSeconds = motion.fadeInTime
                        it.fadeOutSeconds = motion.fadeOutTime
                        // TODO::
                        it.setEffectIds()

                        motionMap.put("${name}_${index}", it)
                    }
                }
            }
        }

        motionManager.stopAllMotions()

    }

}