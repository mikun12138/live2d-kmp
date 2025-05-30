/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework

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
     * @param config Option Class's instance
     * @return if preparing process has finished, return true
     */
    fun startUp(config: CubismFrameworkConfig){
        if (isStarted) {
            cubismLogInfo("CubismFramework.startUp() is already done.")
            return
        }
        isStarted = true

        this.config = config
        Live2DCubismCore.logger = config.logFunction


        // Display the version information of Live2D Cubism Core.
        cubismLogInfo("Live2D Cubism Core version: ${Live2DCubismCore.version}")

        init()
    }

    fun reinit() {
        if (!isStarted) {
            cubismLogWarning("CubismFramework is not started.")
            return
        }
        init()
    }

    private fun init() {
        CubismIdManager.clear()
    }

    /**
     * Execute log function bound Core API
     *
     * @param message log message
     */
    fun coreLogFunction(message: String) {
        Live2DCubismCore.logger.print(message)
    }

    private var isStarted = false

    lateinit var config: CubismFrameworkConfig

}

class CubismFrameworkConfig(
    val logLevel: LogLevel = LogLevel.VERBOSE,
    //TODO:: 和 Live2DCubismCore 内的 2 select 1
    internal val logFunction: ICubismLogger = ICubismLogger { }
) {

    /**
     * ログ出力レベルを定義する列挙体。
     */
    enum class LogLevel(val id: Int) {
        VERBOSE(0),
        DEBUG(1),
        INFO(2),
        WARNING(3),
        ERROR(4),
        OFF(5)
    }
}
