/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package com.live2d.sdk.cubism.framework.utils

import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFramework.coreLogFunction
import com.live2d.sdk.cubism.framework.CubismFramework.loggingLevel
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.CSM_LOG_LEVEL

/**
 * A utility class for debugging.
 *
 *
 * Log output, dump byte, and so on.
 */
object CubismDebug {
    /**
     * Output log. Set log level to 1st argument.
     * At using [CubismFramework.initialize] function, if the log level is lower than set log output level, log output is not executed.
     *
     * @param logLevel log level setting
     * @param message format string
     * @param args variadic arguments
     */
    fun print(
        logLevel: CubismFrameworkConfig.LogLevel,
        message: String?
    ) {
        // If the log level is lower than set log output level in Option class, log outputting is not executed.
        if (logLevel.id < loggingLevel.id) {
            return
        }

        coreLogFunction(message)
    }

    /**
     * Dump out a specified length of data.
     *
     *
     * If the log output level is below the level set in the option at [CubismFramework.initialize], it will not be logged.
     *
     * @param logLevel setting of log level
     * @param data data to dump
     * @param length length of dumping
     */
    fun dumpBytes(logLevel: CubismFrameworkConfig.LogLevel, data: ByteArray, length: Int) {
        for (i in 0..<length) {
            if (i % 16 == 0 && i > 0) {
                print(logLevel, "\n")
            } else if (i % 8 == 0 && i > 0) {
                print(logLevel, " ")
            }
            print(logLevel, toHexByte(data[i].toInt()))
        }
    }

    private fun toHexByte(value: Int): String =
        (value and 0xFF).toString(16).uppercase().padStart(2, '0')

    /**
     * Display the normal message.
     *
     * @param message message
     */
    fun cubismLogPrint(
        logLevel: CubismFrameworkConfig.LogLevel,
        message: String?,
    ) {
        CubismDebug.print(logLevel, "[CSM]$message")
    }

    /**
     * Display a newline message.
     *
     * @param message message
     */
    fun cubismLogPrintln(
        logLevel: CubismFrameworkConfig.LogLevel,
        message: String?,
    ) {
        cubismLogPrint(
            logLevel,
            message.toString() + "\n",
        )
    }

    /**
     * Show detailed message.
     *
     * @param message message
     */
    fun cubismLogVerbose(message: String?) {
        if (CSM_LOG_LEVEL.id <= CubismFrameworkConfig.LogLevel.VERBOSE.id) {
            cubismLogPrintln(
                CubismFrameworkConfig.LogLevel.VERBOSE,
                "[V]$message",
            )
        }
    }

    /**
     * Display the debug message.
     *
     * @param message message
     */
    fun cubismLogDebug(message: String?) {
        if (CSM_LOG_LEVEL.id <= CubismFrameworkConfig.LogLevel.DEBUG.id) {
            cubismLogPrintln(
                CubismFrameworkConfig.LogLevel.DEBUG,
                "[D]$message"
            )
        }
    }

    /**
     * Display informational messages.
     *
     * @param message message
     */
    fun cubismLogInfo(message: String?) {
        if (CSM_LOG_LEVEL.id <= CubismFrameworkConfig.LogLevel.INFO.id) {
            cubismLogPrintln(
                CubismFrameworkConfig.LogLevel.INFO,
                "[I]$message",
            )
        }
    }

    /**
     * Display a warning message.
     *
     * @param message message
     */
    fun cubismLogWarning(message: String?) {
        if (CSM_LOG_LEVEL.id <= CubismFrameworkConfig.LogLevel.WARNING.id) {
            cubismLogPrintln(
                CubismFrameworkConfig.LogLevel.WARNING,
                "[W]$message",
            )
        }
    }

    /**
     * Display a error message.
     *
     * @param message message.
     */
    fun cubismLogError(message: String?) {
        if (CSM_LOG_LEVEL.id <= CubismFrameworkConfig.LogLevel.ERROR.id) {
            cubismLogPrintln(
                CubismFrameworkConfig.LogLevel.ERROR,
                "[E]$message",
            )
        }
    }
}
