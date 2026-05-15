package me.mikun.sandbox

import me.mikun.live2d.Live2DModelClipContext
import me.mikun.live2d.Live2DModelRenderContext
import me.mikun.live2d.framework.Live2DFramework
import me.mikun.live2d.ex.model.Live2DUserModelImpl
import me.mikun.live2d.Live2DRenderer
import me.mikun.live2d.TestRenderer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46.*

fun live2dMain(
    resDirMoc: String,
    mocName: String,
) {
    glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11)
    if (!glfwInit()) {
        throw Error("Unable to initialize GLFW")
    }

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)

    glfwWindowHint(GLFW_SAMPLES, 4)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6)

//    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

    val handle = glfwCreateWindow(1920, 1080, "hello world", 0, 0).takeIf {
        it != 0L
    } ?: run {
        throw IllegalStateException("Failed to create the GLFW window")
    }

    glfwMakeContextCurrent(handle)

    glfwShowWindow(handle)

    GL.createCapabilities()

    run {
        Live2DFramework.init()

        // model logic
        val model = Live2DUserModelImpl().apply {
            init(resDirMoc, "$mocName.model3.json")
        }
        // model render
        val live2DModelRenderContext = Live2DModelRenderContext(model)
        val live2DModelClipContext = Live2DModelClipContext(1, live2DModelRenderContext)
        val live2DRenderer = Live2DRenderer()
        val testRenderer = TestRenderer()

        Timer.update()
        while (!glfwWindowShouldClose(handle)) {
            run {
                glfwSwapBuffers(handle)
                glfwPollEvents()

                Timer.update()

                glClearColor(
                    0.0f,
                    0.0f,
                    0.0f,
                    1.0f
                )
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
                glClearDepthf(1.0f)
            }

            model.update(Timer.deltaF)

            live2DModelRenderContext.update()
//            live2DRenderer.frame(live2DModelRenderContext, live2DModelClipContext)
            testRenderer.frame(live2DModelRenderContext)

        }
    }
}

object Timer {
    fun update() {
        current = System.nanoTime()
        delta = current - last
        last = current
    }

    var current: Long = 0
    var last: Long = 0
    var delta: Long = 0

    val deltaF: Float
        get() = delta / 1000000000.0f

}