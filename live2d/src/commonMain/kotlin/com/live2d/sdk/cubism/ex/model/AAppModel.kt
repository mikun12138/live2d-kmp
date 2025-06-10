package com.live2d.sdk.cubism.ex.model

import com.live2d.sdk.cubism.ex.rendering.ALive2DTexture
import com.live2d.sdk.cubism.ex.model.Live2DUserModel
import com.live2d.sdk.cubism.framework.data.ModelJson
import com.live2d.sdk.cubism.framework.effect.Live2DBreath
import com.live2d.sdk.cubism.framework.effect.Live2DEyeBlink
import com.live2d.sdk.cubism.framework.effect.Live2DLipSync
import com.live2d.sdk.cubism.framework.id.Live2DDefaultParameterId
import com.live2d.sdk.cubism.framework.id.Live2DIdManager
import com.live2d.sdk.cubism.framework.math.CubismTargetPoint
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import com.live2d.sdk.cubism.framework.motion.expression.Live2DExpressionManager
import com.live2d.sdk.cubism.framework.motion.motion.Live2DMotion
import com.live2d.sdk.cubism.framework.motion.motion.Live2DMotionManager
import kotlinx.serialization.json.Json
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.io.path.readBytes

open class AAppModel : Live2DUserModel() {

    // TODO::
//    var isUsingHighPrecisionMask: Boolean = false
    protected var motionManager: Live2DMotionManager = Live2DMotionManager()
    protected var expressionManager: Live2DExpressionManager = Live2DExpressionManager()

    // effects
    protected var breath: Live2DBreath? = null
    protected var eyeBlink: Live2DEyeBlink? = null
    protected var lipSync: Live2DLipSync? = null

    protected var dragManager: CubismTargetPoint = CubismTargetPoint()


    fun init(dir: String, modelJsonFileName: String) {
        val buffer = Path(dir, modelJsonFileName).readBytes()
        val modelJson = Json.Default.decodeFromString<ModelJson>(String(buffer))
        setupModel(dir, modelJson)

        setupTextures(dir, modelJson)
    }

    private fun setupModel(dir: String, modelJson: ModelJson) {
        Path(dir, modelJson.fileReferences.moc).readBytes().let { buffer ->
            loadModel(buffer, true)
        }

        Path(dir, modelJson.fileReferences.pose).readBytes().let { buffer ->
            loadPose(buffer)
        }

        for ((name, motionGroup) in modelJson.fileReferences.motionGroups) {
            motionGroup.forEachIndexed { index, motion ->
                Path(dir, motion.file).readBytes().let { buffer ->
                    loadMotion(buffer)?.let {
                        it.fadeInSeconds = motion.fadeInTime
                        it.fadeOutSeconds = motion.fadeOutTime
                        it.setEffectIds(
                            eyeBlinkParameterIds =
                                modelJson.groups.find { it.name == "EyeBlink" }?.ids!!.map { value ->
                                    Live2DIdManager.id(
                                        value
                                    )
                                },
                            lipSyncParameterIds =
                                modelJson.groups.find { it.name == "LipSync" }?.ids!!.map { value ->
                                    Live2DIdManager.id(
                                        value
                                    )
                                }
                        )
                        name_2_motionList.getOrPut(name) {
                            mutableListOf()
                        }.add(it)
                    }
                }
            }
        }

        for (expression in modelJson.fileReferences.expressions) {
            Path(dir, expression.file).readBytes().let { buffer ->
                loadExpression(buffer)?.let { }
                name_2_expression.put(expression.name, loadExpression(buffer))
            }
        }

        Path(dir, modelJson.fileReferences.physics).readBytes().let { Buffer ->
            loadPhysics(Buffer)
        }

        Path(dir, modelJson.fileReferences.userData).readBytes().let { buffer ->
            loadUserData(buffer)
        }

        eyeBlink = Live2DEyeBlink(modelJson)

        breath = Live2DBreath(
            Live2DBreath.BreathParameterData(
                Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_X.id),
                0.0f,
                15.0f,
                6.5345f,
                0.5f
            ),
            Live2DBreath.BreathParameterData(
                Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_Y.id),
                0.0f,
                8.0f,
                3.5345f,
                0.5f
            ),
            Live2DBreath.BreathParameterData(
                Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_Z.id),
                0.0f,
                10.0f,
                5.5345f,
                0.5f
            ),
            Live2DBreath.BreathParameterData(
                Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.BODY_ANGLE_X.id),
                0.0f,
                4.0f,
                15.5345f,
                0.5f
            ),
            Live2DBreath.BreathParameterData(
                Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.BREATH.id),
                0.5f,
                0.5f,
                3.2345f,
                0.5f,
            )
        )

        // TODO:: layout
//        modelJson.layout

        model.saveParameters()
    }

    fun setupTextures(dir: String, modelJson: ModelJson) {
        modelJson.fileReferences.textures.forEachIndexed { index, texturePath ->
            Path(dir, texturePath).readBytes().let { bytes ->
                model,
                ALive2DTexture(
                    bytes
                )
            }
        }
    }

    override fun doUpdate(deltaSeconds: Float) {

        // Pose Setting
        pose?.updateParameters(model, deltaSeconds)

        // モーションによるパラメーター更新の有無
        var isMotionUpdated = false
        model.loadParameters()
        run {
            if (motionManager.isFinished) {
                startRandomMotion(
                    MotionGroup.IDLE,
                    MotionPriority.IDLE
                )
            } else {
                isMotionUpdated = motionManager.updateMotion(model, deltaSeconds)
            }
        }
        model.saveParameters()

        // expression
        expressionManager.updateMotion(model, deltaSeconds)

        // physics
        physics?.evaluate(model, deltaSeconds)

        // userData
        // TODO::

        // eye blink
        if (!isMotionUpdated) {
            eyeBlink?.updateParameters(model, deltaSeconds)
        }

        // Breath Function
        breath?.updateParameters(model, deltaSeconds)


        /*
            drag
            TODO:: move to dragManager?
        */
        /*
                run {
                    // ドラッグ追従機能
                    // ドラッグによる顔の向きの調整
                    model.addParameterValue(
                        CubismIdManager.id(CubismDefaultParameterId.ParameterId.ANGLE_X.id),
                        dragManager.x * 30
                    ) // -30から30の値を加える
                    model.addParameterValue(
                        CubismIdManager.id(CubismDefaultParameterId.ParameterId.ANGLE_Y.id),
                        dragManager.y * 30
                    )
                    model.addParameterValue(
                        CubismIdManager.id(CubismDefaultParameterId.ParameterId.ANGLE_Z.id),
                        dragManager.x * dragManager.y * (-30)
                    )

                    // ドラッグによる体の向きの調整
                    model.addParameterValue(
                        CubismIdManager.id(CubismDefaultParameterId.ParameterId.BODY_ANGLE_X.id),
                        dragManager.x * 10
                    ) // -10から10の値を加える

                    // ドラッグによる目の向きの調整
                    model.addParameterValue(
                        CubismIdManager.id(CubismDefaultParameterId.ParameterId.EYE_BALL_X.id),
                        dragManager.x
                    ) // -1から1の値を加える
                    model.addParameterValue(
                        CubismIdManager.id(CubismDefaultParameterId.ParameterId.EYE_BALL_Y.id),
                        dragManager.y
                    )
                }
            */
        dragManager.update(deltaSeconds)
    }

    fun startRandomMotion(
        motionGroupName: String,
        priority: MotionPriority,
        onBeganMotionHandler: IBeganMotionCallback = IBeganMotionCallback { },
        onFinishedMotionHandler: IFinishedMotionCallback = IFinishedMotionCallback { },
    ) {
        name_2_motionList[motionGroupName]?.let {
            startMotion(
                it.random(),
                priority,
                onBeganMotionHandler,
                onFinishedMotionHandler
            )
        } ?: error("Failed to start motion: Unknown motion group name($motionGroupName)")
    }

    fun startMotion(
        motion: Live2DMotion,
        priority: MotionPriority,
        onBeganMotionHandler: IBeganMotionCallback = IBeganMotionCallback { },
        onFinishedMotionHandler: IFinishedMotionCallback = IFinishedMotionCallback { },
    ) {
        if (priority == MotionPriority.FORCE) {
            motionManager.reservePriority = priority.value
        } else if (!motionManager.reserveMotion(priority.value)) {
            // TODO:: log
            return
        }

        motion.apply {
            beganMotionCallback = onBeganMotionHandler
            finishedMotionCallback = onFinishedMotionHandler


            //TODO:: sound
        }
        println("[APP] start motion: ${model}")


        motionManager.startMotionPriority(motion, priority.value)
    }

    object MotionGroup {
        const val IDLE = "Idle"
        const val TAP_BODY = "TapBody"
    }

    enum class MotionPriority(
        val value: Int,
    ) {
        NONE(0),
        IDLE(1),
        NORMAL(2),
        FORCE(3);
    }

}