/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.effect.CubismBreath
import com.live2d.sdk.cubism.framework.effect.CubismEyeBlink
import com.live2d.sdk.cubism.framework.effect.CubismPose
import com.live2d.sdk.cubism.framework.math.CubismModelMatrix
import com.live2d.sdk.cubism.framework.math.CubismTargetPoint
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import com.live2d.sdk.cubism.framework.motion.expression.CubismExpressionMotion
import com.live2d.sdk.cubism.framework.motion.expression.CubismExpressionMotionManager
import com.live2d.sdk.cubism.framework.motion.motion.CubismMotion
import com.live2d.sdk.cubism.framework.motion.motion.CubismMotionManager
import com.live2d.sdk.cubism.framework.physics.CubismPhysics
import com.live2d.sdk.cubism.framework.userdata.CubismModelUserData
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogError

/**
 * This is the base class of the model that the user actually utilizes. The user defined model class inherits this class.
 */
abstract class CubismUserModel protected constructor() {

    var totalSeconds = 0.0f
    var lastTotalSeconds = 0.0f

    /**
     * Get the collision detection.
     *
     *
     * Get whether the Drawable has been hit at the specified position.
     *
     * @param drawableId Drawable ID which will be verified.
     * @param pointX X-position
     * @param pointY Y-position
     * @return true      If it is hit, return true.
     */
    /*
        fun isHit(drawableId: CubismId, pointX: Float, pointY: Float): Boolean {
            val drawIndex = model.getDrawableIndex(drawableId)

            // If there are no hit Drawable, return false
            if (drawIndex < 0) {
                return false
            }

            val count = model.getDrawableVertexCount(drawIndex)
            val vertices = model.getDrawableVertices(drawIndex)

            var left = vertices!![0]
            var right = vertices[0]
            var top = vertices[1]
            var bottom = vertices[1]


            for (i in 1..<count) {
                val x = vertices[VERTEX_OFFSET + i * VERTEX_STEP]
                val y = vertices[VERTEX_OFFSET + i * VERTEX_STEP + 1]

                if (x < left) {
                    // Min x
                    left = x
                }

                if (x > right) {
                    // Max x
                    right = x
                }

                if (y < top) {
                    // Min y
                    top = y
                }

                if (y > bottom) {
                    // Max y
                    bottom = y
                }
            }

            val tx: Float = modelMatrix.invertTransformX(pointX)
            val ty: Float = modelMatrix.invertTransformY(pointY)

            return (left <= tx) && (tx <= right) && (top <= ty) && (ty <= bottom)
        }
        */

    protected fun loadModel(buffer: ByteArray, shouldCheckMocConsistency: Boolean = false) {
        val moc = Moc().init(buffer, shouldCheckMocConsistency)

        if (moc == null) {
            cubismLogError("Failed to create CubismMoc instance.")
            return
        }

        val model = moc.initModel()

        if (model == null) {
            cubismLogError("Failed to create the model.")
            return
        }

        this.model = model

        this.model.saveParameters()
        modelMatrix = CubismModelMatrix.create(this.model.canvasWidth, this.model.canvasHeight)
    }

    protected fun loadPose(buffer: ByteArray) {
        try {
            pose = CubismPose(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadPose(). ${e.message}")
        }
    }

    protected fun loadMotion(
        buffer: ByteArray,
        onFinishedMotionHandler: IFinishedMotionCallback = IFinishedMotionCallback { },
        onBeganMotionHandler: IBeganMotionCallback = IBeganMotionCallback { },
    ): CubismMotion? {
        try {
            return CubismMotion(buffer, onFinishedMotionHandler, onBeganMotionHandler)
        } catch (e: Exception) {
            e.printStackTrace()
            cubismLogError("Failed to loadMotion(). ${e.message}")
            return null
        }
    }

    protected fun loadExpression(buffer: ByteArray): CubismExpressionMotion? {
        try {
            return CubismExpressionMotion(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadExpressionMotion(). ${e.message}")
            return null
        }
    }

    protected fun loadPhysics(buffer: ByteArray) {
        try {
            physics = CubismPhysics(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadPhysics(). ${e.message}")
        }
    }

    protected fun loadUserData(buffer: ByteArray) {
        try {
            modelUserData = CubismModelUserData(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadUserData(). ${e.message}")
        }
    }

    fun update(deltaSeconds: Float) {
        lastTotalSeconds = totalSeconds
        totalSeconds += deltaSeconds

        doUpdate(deltaSeconds)

        model.update()
    }

    protected abstract fun doUpdate(deltaSeconds: Float)


    /**
     * A model instance
     */
    lateinit var model: Model
        protected set

    /**
     * A model matrix
     */
    var modelMatrix: CubismModelMatrix? = null

    /*
        System
     */

    /**
     * Auto eye-blink
     */
    protected var eyeBlink: CubismEyeBlink? = null

    /**
     * Breathing
     */
    protected var breath: CubismBreath? = null

    /**
     * pose
     */
    protected var pose: CubismPose? = null

    protected var motionManager: CubismMotionManager = CubismMotionManager()
    protected var expressionManager: CubismExpressionMotionManager = CubismExpressionMotionManager()
    protected var physics: CubismPhysics? = null
    protected var modelUserData: CubismModelUserData? = null

    /**
     * A mouse dragging manager
     */
    protected var dragManager: CubismTargetPoint = CubismTargetPoint()
}
