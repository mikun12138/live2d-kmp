/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.pose.CubismPose
import com.live2d.sdk.cubism.framework.math.CubismModelMatrix
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import com.live2d.sdk.cubism.framework.motion.expression.CubismExpressionMotion
import com.live2d.sdk.cubism.framework.motion.motion.CubismMotion
import com.live2d.sdk.cubism.framework.physics.CubismPhysics
import com.live2d.sdk.cubism.framework.rendering.ALive2DTexture
import com.live2d.sdk.cubism.framework.userdata.CubismModelUserData
import com.live2d.sdk.cubism.framework.utils.Live2DLogger

abstract class Live2DUserModel protected constructor() {

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
        val moc = Live2DMoc(buffer, shouldCheckMocConsistency)

        this.model = moc.instantiateModel()

        this.model.saveParameters()
        modelMatrix = CubismModelMatrix.create(this.model.canvasWidth, this.model.canvasHeight)
    }

    protected fun loadPose(buffer: ByteArray) {
        try {
            pose = CubismPose(buffer)
        } catch (e: Exception) {
            Live2DLogger.error("Failed to loadPose(). ${e.message}")
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
            Live2DLogger.error("Failed to loadMotion(). ${e.message}")
            return null
        }
    }

    protected fun loadExpression(buffer: ByteArray): CubismExpressionMotion? {
        try {
            return CubismExpressionMotion(buffer)
        } catch (e: Exception) {
            Live2DLogger.error("Failed to loadExpressionMotion(). ${e.message}")
            return null
        }
    }

    protected fun loadPhysics(buffer: ByteArray) {
        try {
            physics = CubismPhysics(buffer)
        } catch (e: Exception) {
            Live2DLogger.error("Failed to loadPhysics(). ${e.message}")
        }
    }

    protected fun loadUserData(buffer: ByteArray) {
        try {
            modelUserData = CubismModelUserData(buffer)
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
    protected var pose: CubismPose? = null
    protected val name_2_motionList: MutableMap<String, MutableList<CubismMotion>> = mutableMapOf()
    protected val name_2_expression: MutableMap<String, CubismExpressionMotion?> = mutableMapOf()
    protected var physics: CubismPhysics? = null
    protected var modelUserData: CubismModelUserData? = null
    // TODO:: texture也算renderSystem 可以把蒙版什么的也放一块
    protected val textures: MutableMap<Int, ALive2DTexture> = HashMap(32)
}
