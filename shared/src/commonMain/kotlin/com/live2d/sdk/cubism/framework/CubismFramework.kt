/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework

import com.live2d.sdk.cubism.core.CubismCoreVersion
import com.live2d.sdk.cubism.core.ICubismLogger
import com.live2d.sdk.cubism.core.Live2DCubismCore
import com.live2d.sdk.cubism.framework.id.CubismIdManager
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogInfo
import com.live2d.sdk.cubism.framework.utils.CubismDebug.cubismLogWarning

/**
 * Entrypoint of Live2D Cubism Original Workflow SDK.
 *
 *
 * Beginning to use this Framework, call CubismFramework.initialize() method. Terminating the application, call CubismFramework.dispose() method.
 */
object CubismFramework {
    /**
     * Offset value for mesh vertices
     */
    const val VERTEX_OFFSET: Int = 0

    /**
     * Step value for mesh vertices
     */
    const val VERTEX_STEP: Int = 2


    /**
     * Enable Cubism Framework API.
     * Required to run this method before using API.
     * Once prepared, if you run this again, the inner processes are skipped.
     *
     * @param option Option Class's instance
     * @return if preparing process has finished, return true
     */
    fun startUp(option: Option?): Boolean {
        if (s_isStarted) {
            cubismLogInfo("CubismFramework.startUp() is already done.")

            return s_isStarted
        }

        s_option = option

        if (s_option != null) {
            Live2DCubismCore.logger = option!!.logFunction
        }

        s_isStarted = true

        // Display the version information of Live2D Cubism Core.
        val version: CubismCoreVersion = Live2DCubismCore.version

        cubismLogInfo("Live2D Cubism Core version: $version")

        cubismLogInfo("CubismFramework.startUp() is complete.")

        return s_isStarted
    }

    /**
     * Clear each parameter in CubismFramework initialized by startUp() method.
     * Use this method at reusing CubismFramework done dispose() method.
     */
    fun cleanUp() {
        s_isStarted = false
        s_isInitialized = false
        s_option = null
        s_cubismIdManager = null
    }

    val isStarted: Boolean
        /**
         * Whether Cubism Framework API has been prepared already.
         *
         * @return if API has been already prepared, return true
         */
        get() = s_isStarted

    /**
     * Initializing resources in Cubism Framework, the model is enabled to display.
     * If you would like to use initialize() method again, first you need to run dispose() method.
     */
    fun initialize() {
        assert(s_isStarted)

        if (!s_isStarted) {
            cubismLogWarning("CubismFramework is not started.")
            return
        }

        // Disturb consecutive securing resources.
        if (s_isInitialized) {
            cubismLogWarning("CubismFramework.initialize() skipped, already initialized.")
            return
        }

        // ----- Static Release -----
        s_cubismIdManager = CubismIdManager()
        s_isInitialized = true

        cubismLogInfo("CubismFramework::Initialize() is complete.")
    }

    /**
     * Releases all resources in the Cubism Framework.
     */
    fun dispose() {
        assert(s_isStarted)

        if (!s_isStarted) {
            cubismLogWarning("CubismFramework is not started.")
            return
        }

        // If you use dispose() method, it is required to run initialize() method firstly.
        if (!s_isInitialized) {
            cubismLogWarning("CubismFramework.dispose() skipped, not initialized.")
            return
        }

        //---- static release ----
        s_cubismIdManager = null
        // Release static resources of renderer(cf. Shader programs)
        Live2DRendererWindows.staticRelease()

        s_isInitialized = false

        cubismLogInfo("CubismFramework.dispose() is complete.")
    }

    val isInitialized: Boolean
        /**
         * Whether having already initialized CubismFramework's resources.
         *
         * @return If securing resources have already done, return true
         */
        get() = s_isInitialized

    /**
     * Execute log function bound Core API
     *
     * @param message log message
     */
    fun coreLogFunction(message: String?) {
        Live2DCubismCore.logger?.print(message)
    }

    val loggingLevel: CubismFrameworkConfig.LogLevel
        /**
         * Return the current value of log output level setting.
         *
         * @return the current value of log output level setting
         */
        get() {
            return s_option?.loggingLevel ?: CubismFrameworkConfig.LogLevel.OFF
        }

    val idManager: CubismIdManager
        /**
         * Get the instance of ID manager.
         *
         * @return CubismIdManager class's instance
         */
        get() = s_cubismIdManager

    /**
     * Flag whether the framework has been started or not.
     */
    private var s_isStarted = false

    /**
     * Flag whether the framework has been initialized or not.
     */
    private var s_isInitialized = false

    /**
     * Option object
     */
    private var s_option: Option? = null

    /**
     * CubismIDManager object
     */
    private var s_cubismIdManager: CubismIdManager? = null

    /**
     * Inner class that define optional elements to be set in CubismFramework.
     */
    class Option {
        /**
         * Set the log output function.
         *
         * @param logger log output function
         */
        fun setLogFunction(logger: ICubismLogger) {
            requireNotNull(logger) { "logger is null." }
            logFunction = logger
        }

        /**
         * Functional interface of logging.
         */
        var logFunction: ICubismLogger? = null

        /**
         * Log output level.
         * (Default value is OFF(Log outputting is not executed.))
         */
        var loggingLevel: CubismFrameworkConfig.LogLevel? = CubismFrameworkConfig.LogLevel.OFF
    }
}
