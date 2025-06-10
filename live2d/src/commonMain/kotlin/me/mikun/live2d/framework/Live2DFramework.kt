/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework

import me.mikun.live2d.core.Live2D
import me.mikun.live2d.framework.id.Live2DIdManager
import me.mikun.live2d.framework.utils.Live2DLogger

object Live2DFramework {
    const val VERTEX_OFFSET: Int = 0
    const val VERTEX_STEP: Int = 2

    fun init() {
        if (isStarted) {
            Live2DLogger.info("CubismFramework.startUp() is already done.")
            return
        }
        isStarted = true

        // Display the version information of Live2D Cubism Core.
        Live2DLogger.info("Live2D Cubism Core version: ${Live2D.coreVersion}")

        doInit()
    }

    fun reinit() {
        if (!isStarted) {
            Live2DLogger.warning("CubismFramework is not started.")
            return
        }
        doInit()
    }

    private fun doInit() {
        Live2DIdManager.clear()
    }

    private var isStarted = false

}

object Live2DFrameworkConfig {
    var logger: Live2DLogger = Live2DLogger.Default
    var logLevel: Live2DLogger.Level = Live2DLogger.Level.VERBOSE
}



