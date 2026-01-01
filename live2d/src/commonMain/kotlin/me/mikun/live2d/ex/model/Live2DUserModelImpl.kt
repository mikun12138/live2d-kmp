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
import me.mikun.live2d.framework.effect.Live2DEffect
import me.mikun.live2d.framework.model.ALive2DUserModel
import me.mikun.live2d.framework.model.Live2DMoc
import me.mikun.live2d.framework.model.Live2DModel
import java.io.InputStream
import javax.xml.crypto.dsig.Transform
import kotlin.collections.getOrPut
import kotlin.collections.iterator
import kotlin.io.path.Path
import kotlin.io.path.readBytes

open class Live2DUserModelImpl : ALive2DUserModel {

    internal constructor(model: Live2DModel, moc: Live2DMoc) : super(moc) {
        this.model = model
        setupModel()
    }

    protected var motionManager: Live2DMotionManager = Live2DMotionManager()
    protected var expressionManager: Live2DExpressionManager = Live2DExpressionManager()

    protected var effectManager: MutableMap<String, Live2DEffect> = mutableMapOf(
        Live2DEffect.BuildIn.breath to Live2DBreath(),
        Live2DEffect.BuildIn.eyeBlink to Live2DEyeBlink(moc.modelJson),
        Live2DEffect.BuildIn.lipSync to Live2DLipSync(moc.modelJson),
    )

    // TODO:: load from memory
    private fun setupModel(
        modelJson: ModelJson,
        textureFileTransform: (String) -> InputStream,
        poseFileTransform: (String) -> InputStream,
        motionFileTransform: (String) -> InputStream,
        expressionFileTransform: (String) -> InputStream,
        physicsFileTransform: (String) -> InputStream,
        userDataFileTransform: (String) -> InputStream,
    ) {

        modelJson.fileReferences.textures.forEach {
            textureFileTransform(it).readBytes().let { buffer ->
                textures.add(buffer)
            }
        }

        modelJson.fileReferences.pose?.let {
            poseFileTransform(it).readBytes().let { buffer ->
                loadPose(buffer)
            }
        }

        modelJson.fileReferences.motionGroups.forEach { (name, motionGroup) ->
            motionGroup.forEachIndexed { index, motion ->
                motionFileTransform(motion.file).readBytes().let { buffer ->
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

        modelJson.fileReferences.expressions.forEach { expression ->
            expression.file.let {
                expressionFileTransform(it).readBytes().let { buffer ->
                    name_2_expression[expression.name] = loadExpression(buffer)
                }
            }
        }

        modelJson.fileReferences.physics.let {
            physicsFileTransform(it).readBytes().let { buffer ->
                loadPhysics(buffer)
            }
        }

        modelJson.fileReferences.userData?.let {
            userDataFileTransform(it).readBytes().let { buffer ->
                loadUserData(buffer)
            }
        }

        // TODO:: layout
//        modelJson.layout

//        model.saveParameters()
    }

    private fun setupModel() {
        with(moc) {
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

            modelJson.fileReferences.motionGroups.forEach { (name, motionGroup) ->
                motionGroup.forEach { motion ->
                    Path(dir, motion.file).readBytes().let { buffer ->
                        loadMotion(buffer)?.let {
                            it.fadeInSeconds = motion.fadeInTime
                            it.fadeOutSeconds = motion.fadeOutTime
                            it.setEffectIds(
                                eyeBlinkParameterIds = modelJson.groups.find { it.name == "EyeBlink" }?.ids!!.map { value ->
                                    Live2DIdManager.id(
                                        value
                                    )
                                },
                                lipSyncParameterIds = modelJson.groups.find { it.name == "LipSync" }?.ids!!.map { value ->
                                    Live2DIdManager.id(
                                        value
                                    )
                                })
                            name_2_motionList.getOrPut(name) {
                                mutableListOf()
                            }.add(it)
                        }
                    }
                }
            }

            modelJson.fileReferences.expressions.forEach { expression ->
                Path(dir, expression.file).readBytes().let { buffer ->
                    name_2_expression[expression.name] = loadExpression(buffer)
                }
            }

            modelJson.fileReferences.physics.let {
                Path(dir, it).readBytes().let { buffer ->
                    loadPhysics(buffer)
                }
            }


            modelJson.fileReferences.userData?.let {
                Path(dir, it).readBytes().let { buffer ->
                    loadUserData(buffer)
                }
            }

            // TODO:: layout
//        modelJson.layout

//            model.saveParameters()
        }
    }

    override fun doUpdate(deltaSeconds: Float) {

        // Pose Setting
        pose?.update(model, deltaSeconds)

        // モーションによるパラメーター更新の有無
        var isMotionUpdated = false
//        model.loadParameters()
        run {
            if (motionManager.isFinished) {
                startRandomMotion(
                    IDLE, MotionPriority.IDLE
                )
            } else {
                isMotionUpdated = motionManager.update(model, deltaSeconds)
            }
        }
//        model.saveParameters()

        // expression
        expressionManager.update(model, deltaSeconds)

        // physics
        physics?.update(model, deltaSeconds)

        // userData
        // TODO:: userdata

        // eye blink

        if (!isMotionUpdated) {
            effectManager[Live2DEffect.BuildIn.eyeBlink]!!.update(model, deltaSeconds)
        }

        effectManager[Live2DEffect.BuildIn.lipSync]!!.update(model, deltaSeconds)

        // Breath Function
        effectManager[Live2DEffect.BuildIn.breath]!!.update(model, deltaSeconds)
    }

    fun startRandomMotion(
        motionGroupName: String,
        priority: MotionPriority,
        onBeganMotionHandler: IBeganMotionCallback = IBeganMotionCallback { },
        onFinishedMotionHandler: IFinishedMotionCallback = IFinishedMotionCallback { },
    ) {
        name_2_motionList[motionGroupName]?.let {
            startMotion(
                it.random(), priority, onBeganMotionHandler, onFinishedMotionHandler
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
        NONE(0), IDLE(1), NORMAL(2), FORCE(3);
    }

}


