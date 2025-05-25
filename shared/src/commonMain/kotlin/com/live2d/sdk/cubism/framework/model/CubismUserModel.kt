/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.model

import com.live2d.sdk.cubism.framework.CubismFramework.VERTEX_OFFSET
import com.live2d.sdk.cubism.framework.CubismFramework.VERTEX_STEP
import com.live2d.sdk.cubism.framework.effect.CubismBreath
import com.live2d.sdk.cubism.framework.effect.CubismEyeBlink
import com.live2d.sdk.cubism.framework.effect.CubismPose
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.math.CubismModelMatrix
import com.live2d.sdk.cubism.framework.math.CubismTargetPoint
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotion
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotionManager
import com.live2d.sdk.cubism.framework.motion.CubismMotion
import com.live2d.sdk.cubism.framework.motion.CubismMotionManager
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import com.live2d.sdk.cubism.framework.physics.CubismPhysics
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogError
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogInfo

/**
 * This is the base class of the model that the user actually utilizes. The user defined model class inherits this class.
 */
abstract class CubismUserModel protected constructor() {
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
    fun isHit(drawableId: CubismId, pointX: Float, pointY: Float): Boolean {
        val drawIndex = model!!.getDrawableIndex(drawableId)

        // If there are no hit Drawable, return false
        if (drawIndex < 0) {
            return false
        }

        val count = model!!.getDrawableVertexCount(drawIndex)
        val vertices = model!!.getDrawableVertices(drawIndex)

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

    /**
     * 生成されたレンダラーを受け取って初期化する。<br></br>
     * クリッピングマスクの描画に使うバッファの枚数をデフォルトの1枚より増やしたい場合は、このメソッドを使用する。
     *
     * @note 第1引数にnullが与えられた場合`NullPointerException`が投げられる。
     *
     * @param renderer CubismRendererを継承したレンダラークラスのインスタンス
     * @param maskBufferCount 生成したいマスクバッファの枚数
     */
    fun setupRenderer(renderer: CubismRenderer?, maskBufferCount: Int = 1) {
        this.renderer = renderer

        // Bind a renderer with a model instance
        this.renderer.initialize(model, maskBufferCount)
    }

    /**
     * モデルデータを読み込む。
     *
     * @param buffer MOC3ファイルが読み込まれているバイト配列バッファ
     */
    /**
     * モデルデータを読み込む。
     * NOTE: デフォルトではMOC3の整合性をチェックしない。
     *
     * @param buffer MOC3ファイルが読み込まれているバイト配列バッファ
     */
    protected fun loadModel(buffer: ByteArray, shouldCheckMocConsistency: Boolean = false) {
        val moc = CubismMoc().init(buffer, shouldCheckMocConsistency)

        if (moc == null) {
            cubismLogError("Failed to create CubismMoc instance.")
            return
        }

        this.moc = moc
        val model = this.moc!!.initModel()

        if (model == null) {
            cubismLogError("Failed to create the model.")
            return
        }

        this.model = model

        this.model!!.saveParameters()
        modelMatrix = CubismModelMatrix.create(this.model!!.canvasWidth, this.model!!.canvasHeight)
    }

/*
    protected fun delete() {
        if (moc == null || model == null) {
            return
        }
        moc!!.deleteModel(model!!)

        moc!!.close()
        model!!.close()
        renderer.close()

        moc = null
        model = null
        renderer = null
    }
*/

    protected fun loadPose(buffer: ByteArray) {
        try {
            pose = CubismPose(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadPose(). ${e.message}")
        }
    }

    protected fun loadMotion(
        buffer: ByteArray?,
        onFinishedMotionHandler: IFinishedMotionCallback? = null,
        onBeganMotionHandler: IBeganMotionCallback? = null
    ): CubismMotion? {
        try {
            return CubismMotion.create(buffer, onFinishedMotionHandler, onBeganMotionHandler)
        } catch (e: Exception) {
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

    protected var moc: CubismMoc? = null
    /**
     * A model instance
     */
    var model: CubismModel? = null
        protected set

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

    /**
     * A model matrix
     */
    protected var modelMatrix: CubismModelMatrix? = null


    /**
     * A mouse dragging manager
     */
    protected var dragManager: CubismTargetPoint = CubismTargetPoint()

    protected var motionManager: CubismMotionManager = CubismMotionManager()
    protected var expressionManager: CubismExpressionMotionManager = CubismExpressionMotionManager()
    protected var physics: CubismPhysics? = null
    protected var modelUserData: CubismModelUserData? = null

    /**
     * An XY-position of mouse dragging
     */
    protected var dragX: Float = 0f
    protected var dragY: Float = 0f

    /**
     * An acceleration in XYZ-axis direction
     */
    protected var accelerationX: Float = 0f
    protected var accelerationY: Float = 0f
    protected var accelerationZ: Float = 0f

    /**
     * A renderer
     */
    private var renderer: CubismRenderer? = null

    /**
     * Constructor
     */
    init {
        // Because this class inherits MotionQueueManager, the usage is the same.
        motionManager.setEventCallback(cubismDefaultMotionEventCallback, this)
    }

    companion object {
        /**
         * A callback for registering with CubismMotionQueueManager for an event.
         * Call the EventFired which is inherited from CubismUserModel.
         *
         * @param eventValue the string data of the fired event
         * @param model an instance inherited with CubismUserModel
         */
        fun cubismDefaultMotionEventCallback(eventValue: String?, model: CubismUserModel?) {
            model?.motionEventFired(eventValue)
        }

        /**
         * An entity of CubismMotionEventFunction.
         */
        private val cubismDefaultMotionEventCallback: ICubismMotionEventFunction =
            object : ICubismMotionEventFunction() {
                public override fun apply(
                    caller: CubismMotionQueueManager?,
                    eventValue: String?,
                    customData: Any?
                ) {
                    if (customData != null) {
                        (customData as CubismUserModel).motionEventFired(eventValue)
                    }
                }
            }
    }
}
