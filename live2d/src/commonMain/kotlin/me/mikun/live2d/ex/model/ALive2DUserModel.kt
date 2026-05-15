package me.mikun.live2d.ex.model

import me.mikun.live2d.framework.utils.math.CubismModelMatrix
import me.mikun.live2d.framework.model.Live2DMoc
import me.mikun.live2d.framework.model.Live2DModel
import me.mikun.live2d.framework.plugin.motion.IBeganMotionCallback
import me.mikun.live2d.framework.plugin.motion.IFinishedMotionCallback
import me.mikun.live2d.framework.plugin.motion.expression.Live2DExpressionMotion
import me.mikun.live2d.framework.plugin.motion.motion.Live2DMotion
import me.mikun.live2d.framework.plugin.physics.Live2DPhysics
import me.mikun.live2d.framework.plugin.pose.Live2DPose
import me.mikun.live2d.framework.plugin.userdata.Live2DModelUserData
import me.mikun.live2d.framework.utils.Live2DLogger

abstract class ALive2DUserModel protected constructor() {

    var totalSeconds = 0.0f
    var lastTotalSeconds = 0.0f

    protected fun loadModel(buffer: ByteArray, shouldCheckMocConsistency: Boolean = true) {
        val moc = Live2DMoc(buffer, shouldCheckMocConsistency)

        this.model = moc.instantiateModel()

        modelMatrix = CubismModelMatrix(this.model.canvasWidth, this.model.canvasHeight)
    }

    protected fun loadPose(buffer: ByteArray) {
        try {
            pose = Live2DPose(buffer).apply {
                init(model)
            }
        } catch (e: Exception) {
            Live2DLogger.error("Failed to loadPose(). ${e.message}")
        }
    }

    protected fun loadMotion(
        buffer: ByteArray,
        onFinishedMotionHandler: IFinishedMotionCallback = IFinishedMotionCallback { },
        onBeganMotionHandler: IBeganMotionCallback = IBeganMotionCallback { },
    ): Live2DMotion? {
        try {
            return Live2DMotion(buffer, onFinishedMotionHandler, onBeganMotionHandler)
        } catch (e: Exception) {
            e.printStackTrace()
            Live2DLogger.error("Failed to loadMotion(). ${e.message}")
            return null
        }
    }

    protected fun loadExpression(buffer: ByteArray): Live2DExpressionMotion? {
        try {
            return Live2DExpressionMotion(buffer)
        } catch (e: Exception) {
            Live2DLogger.error("Failed to loadExpressionMotion(). ${e.message}")
            return null
        }
    }

    protected fun loadPhysics(buffer: ByteArray) {
        try {
            physics = Live2DPhysics(buffer)
        } catch (e: Exception) {
            Live2DLogger.error("Failed to loadPhysics(). ${e.message}")
        }
    }

    protected fun loadUserData(buffer: ByteArray) {
        try {
            modelUserData = Live2DModelUserData(buffer)
        } catch (e: Exception) {
            Live2DLogger.error("Failed to loadUserData(). ${e.message}")
        }
    }

    fun update(deltaSeconds: Float) {
        lastTotalSeconds = totalSeconds
        totalSeconds += deltaSeconds

        doUpdate(deltaSeconds)

        model.update()
    }

    protected abstract fun doUpdate(deltaSeconds: Float)


    lateinit var model: Live2DModel
        protected set

    var modelMatrix: CubismModelMatrix? = null

    /*
        System
     */
    protected var pose: Live2DPose? = null
    protected val name_2_motionList: MutableMap<String, MutableList<Live2DMotion>> = mutableMapOf()
    protected val name_2_expression: MutableMap<String, Live2DExpressionMotion?> = mutableMapOf()
    protected var physics: Live2DPhysics? = null
    protected var modelUserData: Live2DModelUserData? = null

    val textures: MutableList<ByteArray> = mutableListOf()


}