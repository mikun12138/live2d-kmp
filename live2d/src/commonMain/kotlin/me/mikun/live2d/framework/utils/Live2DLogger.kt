/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */
package me.mikun.live2d.framework.utils

import me.mikun.live2d.framework.Live2DFrameworkConfig

interface Live2DLogger {

    fun verbose(message: String)
    fun debug(message: String)
    fun info(message: String)
    fun core(message: String)
    fun warning(message: String)
    fun error(message: String)


    enum class Level(
        val id: Int,
    ) {
        VERBOSE(0),
        DEBUG(1),
        INFO(2), CORE(2),
        WARNING(3),
        ERROR(4),
        OFF(5)
    }

    companion object : Live2DLogger by Live2DFrameworkConfig.logger

    object Default : Live2DLogger {

        private fun print(
            message: String
        ) {
            println(message)
        }
        private fun print(
            level: Level,
            message: String,
        ) {
            // If the log level is lower than set log output level in Option class, log outputting is not executed.
            if (level.id < Live2DFrameworkConfig.logLevel.id) {
                return
            }

            print(message)
        }

        fun dumpBytes(level: Level, data: ByteArray, length: Int) {
            for (i in 0..<length) {
                if (i % 16 == 0 && i > 0) {
                    print(level, "\n")
                } else if (i % 8 == 0 && i > 0) {
                    print(level, " ")
                }
                print(level, toHexByte(data[i].toInt()))
            }
        }

        private fun toHexByte(value: Int): String =
            (value and 0xFF).toString(16).uppercase().padStart(2, '0')

        fun logPrint(
            level: Level,
            message: String,
        ) {
            print(level, "[CSM]$message")
        }

        fun cubismLogPrintln(
            level: Level,
            message: String,
        ) {
            logPrint(
                level,
                message + "\n",
            )
        }

        override fun verbose(message: String) {
            cubismLogPrintln(
                Level.VERBOSE,
                "[V]$message",
            )
        }

        override fun debug(message: String) {
            cubismLogPrintln(
                Level.DEBUG,
                "[D]$message"
            )
        }

        override fun info(message: String) {
            cubismLogPrintln(
                Level.INFO,
                "[I]$message",
            )
        }

        override fun core(message: String) {
            cubismLogPrintln(
                Level.CORE,
                "[CORE]$message",
            )
        }

        override fun warning(message: String) {
            cubismLogPrintln(
                Level.WARNING,
                "[W]$message",
            )
        }

        override fun error(message: String) {
            cubismLogPrintln(
                Level.ERROR,
                "[E]$message",
            )
        }
    }
}
