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
import com.live2d.sdk.cubism.framework.id.CubismId
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
     * Do a standard process at firing the event.
     *
     *
     * This method deals with the case where an Event occurs during the playback process.
     * It is basically overrided by inherited class.
     * If it is not overrided, output log.
     *
     * @param eventValue the string data of the fired event
     */
    fun motionEventFired(eventValue: String?) {
        cubismLogInfo(eventValue)
    }

    /**
     * Set an initializing setting.
     *
     * @param isInitialized initializing status
     */
    fun isInitialized(isInitialized: Boolean) {
        this.isInitialized = isInitialized
    }

    /**
     * Set an updating status.
     *
     * @param isUpdated updating status
     */
    fun isUpdated(isUpdated: Boolean) {
        this.isUpdated = isUpdated
    }

    /**
     * Set an information of mouse dragging.
     *
     * @param x X-position of the cursor being dragging
     * @param y Y-position of the cursor being dragging
     */
    fun setDragging(x: Float, y: Float) {
        dragManager.set(x, y)
    }

    /**
     * Set an acceleration information.
     *
     * @param x Acceleration in X-axis direction
     * @param y Acceleration in Y-axis direction
     * @param z Acceleration in Z-axis direction
     */
    fun setAcceleration(x: Float, y: Float, z: Float) {
        accelerationX = x
        accelerationY = y
        accelerationZ = z
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
    protected fun loadModel(buffer: ByteArray?, shouldCheckMocConsistency: Boolean = false) {
        val moc = CubismMoc.create(buffer!!, shouldCheckMocConsistency)

        if (moc == null) {
            cubismLogError("Failed to create CubismMoc instance.")
            return
        }

        this.moc = moc
        val model = this.moc!!.createModel()

        if (model == null) {
            cubismLogError("Failed to create the model.")
            return
        }

        this.model = model

        this.model!!.saveParameters()
        modelMatrix = CubismModelMatrix.create(this.model!!.canvasWidth, this.model!!.canvasHeight)
    }

    /**
     * Delete Moc and Model instances.
     */
    protected fun delete() {
        if (moc == null || model == null) {
            return
        }
        moc!!.deleteModel(model!!)

        moc!!.delete()
        model!!.close()
        renderer.close()

        moc = null
        model = null
        renderer = null
    }

    /**
     * Load a motion data.
     *
     * @param buffer a buffer where motion3.json file is loaded.
     * @param onFinishedMotionHandler the callback method called at finishing motion play. If it is null, callbacking methods is not conducting.
     * @param onBeganMotionHandler the callback method called at beginning motion play. If it is null, callbacking methods is not conducting.
     * @return motion class
     */
    protected fun loadMotion(
        buffer: ByteArray?,
        onFinishedMotionHandler: IFinishedMotionCallback?,
        onBeganMotionHandler: IBeganMotionCallback?
    ): CubismMotion? {
        try {
            return CubismMotion.create(buffer, onFinishedMotionHandler, onBeganMotionHandler)
        } catch (e: Exception) {
            cubismLogError("Failed to loadMotion(). %s", e.message)
            return null
        }
    }

    /**
     * Load a motion data.
     *
     * @param buffer a buffer where motion3.json file is loaded.
     * @return motion class
     */
    protected fun loadMotion(buffer: ByteArray?): CubismMotion? {
        return loadMotion(buffer, null, null)
    }

    /**
     * Load a expression data.
     *
     * @param buffer a buffer where exp3.json is loaded
     * @return motion class
     */
    protected fun loadExpression(buffer: ByteArray?): CubismExpressionMotion? {
        try {
            return CubismExpressionMotion.create(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadExpressionMotion(). %s", e.message)
            return null
        }
    }

    /**
     * Load pose data.
     *
     * @param buffer a buffer where pose3.json is loaded.
     */
    protected fun loadPose(buffer: ByteArray?) {
        try {
            pose = CubismPose.create(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadPose(). %s", e.message)
        }
    }

    /**
     * Load physics data.
     *
     * @param buffer a buffer where physics3.json is loaded.
     */
    protected fun loadPhysics(buffer: ByteArray?) {
        try {
            physics = CubismPhysics.create(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadPhysics(). %s", e.message)
        }
    }

    /**
     * Load a user data attached the model.
     *
     * @param buffer a buffer where userdata3.json is loaded.
     */
    protected fun loadUserData(buffer: ByteArray?) {
        try {
            modelUserData = com.live2d.sdk.cubism.framework.model.CubismModelUserData.create(buffer)
        } catch (e: Exception) {
            cubismLogError("Failed to loadUserData(). %s", e.message)
        }
    }

    /**
     * A Moc data,
     */
    protected var moc: CubismMoc? = null
    /*
     * Get the model.
     *
     * @return the model
     */
    /**
     * A model instance
     */
    var model: CubismModel? = null
        protected set

    /**
     * A motion manager
     */
    protected var motionManager: CubismMotionManager = CubismMotionManager()

    /**
     * A expression manager
     */
    protected var expressionManager: CubismExpressionMotionManager = CubismExpressionMotionManager()

    /**
     * Auto eye-blink
     */
    protected var eyeBlink: CubismEyeBlink? = null

    /**
     * Breathing
     */
    protected var breath: CubismBreath? = null

    /**
     * A model matrix
     */
    protected var modelMatrix: CubismModelMatrix? = null

    /**
     * m
     * Pose manager
     */
    protected var pose: CubismPose? = null

    /**
     * A mouse dragging manager
     */
    protected var dragManager: CubismTargetPoint = CubismTargetPoint()

    /**
     * physics
     */
    protected var physics: CubismPhysics? = null

    /**
     * A user data
     */
    protected var modelUserData: com.live2d.sdk.cubism.framework.model.CubismModelUserData? = null

    /**
     * Get initializing status.
     *
     * @return If this class is initialized, return true.
     */
    /**
     * An initializing status
     */
    var isInitialized: Boolean = false
        protected set
    /**
     * Get the updating status.
     *
     * @return If this class is updated, return true.
     */
    /**
     * An updating status
     */
    var isUpdated: Boolean = false
        protected set
    /**
     * Get the opacity.
     *
     * @return the opacity
     */
    /**
     * Set an opacity.
     *
     * @param opacity an opacity
     */
    /**
     * Opacity
     */
    var opacity: Float = 1.0f

    /**
     * A lip-sync status
     */
    protected var lipSync: Boolean = true

    /**
     * A control value of the last lip-sync
     */
    protected var lastLipSyncValue: Float = 0f

    /**
     * An X-position of mouse dragging
     */
    protected var dragX: Float = 0f

    /**
     * An Y-position of mouse dragging
     */
    protected var dragY: Float = 0f

    /**
     * An acceleration in X-axis direction
     */
    protected var accelerationX: Float = 0f

    /**
     * An acceleration in Y-axis direction
     */
    protected var accelerationY: Float = 0f

    /**
     * An acceleration in Z-axis direction
     */
    protected var accelerationZ: Float = 0f

    /**
     * MOC3の整合性を検証するか。検証するならtrue。
     */
    protected var mocConsistency: Boolean = false

    /**
     * Whether it is debug mode
     */
    protected var debugMode: Boolean = false

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
