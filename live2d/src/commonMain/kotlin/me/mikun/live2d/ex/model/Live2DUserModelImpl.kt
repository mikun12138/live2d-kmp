package me.mikun.live2d.ex.model

import me.mikun.live2d.framework.id.Live2DDefaultParameterId
import me.mikun.live2d.framework.data.ModelJson
import me.mikun.live2d.framework.effect.Live2DBreath
import me.mikun.live2d.framework.effect.Live2DBreath.BreathParameterData
import me.mikun.live2d.framework.effect.Live2DEyeBlink
import me.mikun.live2d.framework.effect.Live2DLipSync
import me.mikun.live2d.framework.id.Live2DIdManager
import me.mikun.live2d.ex.model.Live2DUserModelImpl.MotionGroup.IDLE
import me.mikun.live2d.framework.motion.IBeganMotionCallback
import me.mikun.live2d.framework.motion.IFinishedMotionCallback
import me.mikun.live2d.ex.model.motion.motion.Live2DExpressionManager
import me.mikun.live2d.framework.motion.motion.Live2DMotion
import me.mikun.live2d.ex.model.motion.expression.Live2DMotionManager
import kotlinx.serialization.json.Json
import kotlin.collections.getOrPut
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.io.path.readBytes

open class Live2DUserModelImpl : ALive2DUserModel() {

    // TODO::
//    var isUsingHighPrecisionMask: Boolean = false
    protected var motionManager: Live2DMotionManager = Live2DMotionManager()
    protected var expressionManager: Live2DExpressionManager = Live2DExpressionManager()

    // effects
    protected var breath: Live2DBreath = Live2DBreath(
        BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_X.id),
            0.0f,
            15.0f,
            6.5345f,
            0.5f
        ),
        BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_Y.id),
            0.0f,
            8.0f,
            3.5345f,
            0.5f
        ),
        BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.ANGLE_Z.id),
            0.0f,
            10.0f,
            5.5345f,
            0.5f
        ),
        BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.BODY_ANGLE_X.id),
            0.0f,
            4.0f,
            15.5345f,
            0.5f
        ),
        BreathParameterData(
            Live2DIdManager.id(Live2DDefaultParameterId.ParameterId.BREATH.id),
            0.5f,
            0.5f,
            3.2345f,
            0.5f,
        )
    )
    lateinit var eyeBlink: Live2DEyeBlink
    lateinit var lipSync: Live2DLipSync


    fun init(dir: String, modelJsonFileName: String) {
        val buffer = Path(dir, modelJsonFileName).readBytes()
        val modelJson = Json.decodeFromString<ModelJson>(String(buffer))
        setupModel(dir, modelJson)
    }

    private fun setupModel(dir: String, modelJson: ModelJson) {
        Path(dir, modelJson.fileReferences.moc).readBytes().let { buffer ->
            loadModel(buffer, true)
        }

        modelJson.fileReferences.textures.forEach {
            Path(dir, it).readBytes().let { buffer ->
                textures.add(buffer)
            }
        }

        modelJson.fileReferences.pose?.let {
            Path(dir, it).readBytes().let { buffer ->
                loadPose(buffer)
            }
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

        modelJson.fileReferences.userData?.let {
            Path(dir, it).readBytes().let { buffer ->
                loadUserData(buffer)
            }
        }

        eyeBlink = Live2DEyeBlink(modelJson)

        lipSync = Live2DLipSync(modelJson)

        // TODO:: layout
//        modelJson.layout

        model.saveParameters()
    }

    override fun doUpdate(deltaSeconds: Float) {

        // Pose Setting
        pose?.update(model, deltaSeconds)

        // モーションによるパラメーター更新の有無
        var isMotionUpdated = false
        model.loadParameters()
        run {
            if (motionManager.isFinished) {
                startRandomMotion(
                    IDLE,
                    MotionPriority.IDLE
                )
            } else {
                isMotionUpdated = motionManager.update(model, deltaSeconds)
            }
        }
        model.saveParameters()

        // expression
        expressionManager.update(model, deltaSeconds)

        // physics
        physics?.evaluate(model, deltaSeconds)

        // userData
        // TODO::

        // eye blink
        if (!isMotionUpdated) {
            eyeBlink.update(model, deltaSeconds)
        }

        lipSync.update(model, deltaSeconds)

        // Breath Function
        breath.update(model, deltaSeconds)
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


