package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.id.CubismDefaultParameterId
import com.live2d.sdk.cubism.framework.data.ModelJson
import com.live2d.sdk.cubism.framework.effect.CubismBreath
import com.live2d.sdk.cubism.framework.effect.CubismBreath.BreathParameterData
import com.live2d.sdk.cubism.framework.effect.CubismEyeBlink
import com.live2d.sdk.cubism.framework.id.CubismIdManager
import com.live2d.sdk.cubism.framework.model.AAppModel.MotionGroup.IDLE
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import com.live2d.sdk.cubism.framework.motion.expression.CubismExpressionMotion
import com.live2d.sdk.cubism.framework.motion.motion.CubismMotion
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.readBytes

abstract class AAppModel : CubismUserModel() {

    var isUsingHighPrecisionMask: Boolean = false


//    private val eyeBlinkIds: MutableList<CubismId> = mutableListOf()
//    private val lipSyncIds: MutableList<CubismId> = mutableListOf()

    private val name_2_motionList: MutableMap<String, MutableList<CubismMotion>> = mutableMapOf()
    private val name_2_expression: MutableMap<String, CubismExpressionMotion?> = mutableMapOf()


    fun init(dir: String, modelJsonFileName: String) {
        val buffer = Path(dir, modelJsonFileName).readBytes()
        val modelJson = Json.decodeFromString<ModelJson>(String(buffer))
        setupModel(dir, modelJson)

        setupTextures(dir, modelJson)

// TODO::        initClip / mask()
    }

    abstract fun setupTextures(dir: String, modelJson: ModelJson)


    private fun setupModel(dir: String, modelJson: ModelJson) {
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
            Path(dir, expression!!.file).readBytes().let { buffer ->
                name_2_expression.put(expression!!.name, loadExpression(buffer))
            }
        }

        Path(dir, modelJson.fileReferences.userData).readBytes().let { buffer ->
            loadUserData(buffer)
        }

        // Load eye blink data
        // TODO:: save it
        eyeBlink = CubismEyeBlink(modelJson)

        // Load Breath Data
        // TODO:: save it
        breath = CubismBreath(
            BreathParameterData(
                CubismIdManager.id(CubismDefaultParameterId.ParameterId.ANGLE_X.id),
                0.0f,
                15.0f,
                6.5345f,
                0.5f
            ),
            BreathParameterData(
                CubismIdManager.id(CubismDefaultParameterId.ParameterId.ANGLE_Y.id),
                0.0f,
                8.0f,
                3.5345f,
                0.5f
            ),
            BreathParameterData(
                CubismIdManager.id(CubismDefaultParameterId.ParameterId.ANGLE_Z.id),
                0.0f,
                10.0f,
                5.5345f,
                0.5f
            ),
            BreathParameterData(
                CubismIdManager.id(CubismDefaultParameterId.ParameterId.BODY_ANGLE_X.id),
                0.0f,
                4.0f,
                15.5345f,
                0.5f
            ),
            BreathParameterData(
                CubismIdManager.id(CubismDefaultParameterId.ParameterId.BREATH.id),
                0.5f,
                0.5f,
                3.2345f,
                0.5f,
            )
        )

        // TODO:: layout
//        modelJson.layout

        model.saveParameters()

        // Just preload motions
        for ((name, motionGroup) in modelJson.fileReferences.motionGroups) {
            motionGroup.forEachIndexed { index, motion ->
                Path(dir, motion.file).readBytes().let { buffer ->
                    loadMotion(buffer)?.let {
                        it.fadeInSeconds = motion.fadeInTime
                        it.fadeOutSeconds = motion.fadeOutTime
                        it.setEffectIds(
                            eyeBlinkParameterIds =
                                modelJson.groups.find { it.name == "EyeBlink" }?.ids!!.map { value ->
                                    CubismIdManager.id(
                                        value
                                    )
                                },
                            lipSyncParameterIds =
                                modelJson.groups.find { it.name == "LipSync" }?.ids!!.map { value ->
                                    CubismIdManager.id(
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

        motionManager.stopAllMotions()
    }

    override fun doUpdate(deltaSeconds: Float) {

        dragManager.update(deltaSeconds)

        // モーションによるパラメーター更新の有無
        var isMotionUpdated = false
        // Idle动画
        run {
            model.loadParameters()
            run {
                if (motionManager.isFinished) {
                    startMotion(
                        name_2_motionList[IDLE]!![2],
                        MotionPriority.IDLE
                    )
                } else {
                    isMotionUpdated = motionManager.updateMotion(model, deltaSeconds)
                }
            }
            model.saveParameters()
        }

        // eye blink
        if (!isMotionUpdated) {
            eyeBlink?.updateParameters(model, deltaSeconds)
        }


        // expression
        expressionManager.updateMotion(model, deltaSeconds)


        /*
            drag
            TODO:: move to dragManager?
         */
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

        // Breath Function
        breath?.updateParameters(model, deltaSeconds)

        // Physics Setting
        physics?.evaluate(model, deltaSeconds)


        // Lip Sync Setting
        // TODO:: move to where
//        if (true) {
//            // リアルタイムでリップシンクを行う場合、システムから音量を取得して0~1の範囲で値を入力します
//            val value = 0.0f
//
//            for (i in lipSyncIds.indices) {
//                val lipSyncId: CubismId = lipSyncIds.get(i)
//                model.addParameterValue(lipSyncId, value, 0.8f)
//            }
//        }


        // Pose Setting
        pose?.updateParameters(model, deltaSeconds)

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
        motion: CubismMotion,
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