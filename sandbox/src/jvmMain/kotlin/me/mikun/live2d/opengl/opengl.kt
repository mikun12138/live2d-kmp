package me.mikun.live2d.opengl

import me.mikun.live2d.framework.Live2DFramework
import me.mikun.live2d.ex.model.Live2DUserModelImpl
import me.mikun.sandbox.Timer
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46.*

fun opengl(
    resDirMoc: String,
    mocName: String,
) {
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

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_LINEAR
        )
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_LINEAR
        )
        glEnable(GL_BLEND)
        glBlendFunc(
            GL_SRC_ALPHA,
            GL_ONE_MINUS_SRC_ALPHA
        )

        val model = Live2DUserModelImpl()
        model.init(resDirMoc, "$mocName.model3.json")

        val renderer = OpenGLRenderer(model, 1)

        Timer.update()
        while (!glfwWindowShouldClose(handle)) {
            glClearColor(
                0.0f,
                0.0f,
                0.0f,
                1.0f
            )
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glClearDepthf(1.0f)

            Timer.update()

            val canvasMatrix = Matrix4f().scale(
                1080.0f / 1920.0f,
                1.0f,
                1.0f
            )

            val modelMatrix = Matrix4f().scale(
                2.0f / model.model.canvasHeight,
                2.0f / model.model.canvasHeight,
                1.0f
            )

            model.update(Timer.deltaF)
            renderer.frame(
                modelMatrix.mul(canvasMatrix, Matrix4f()).get(FloatArray(16))
            )

            glfwSwapBuffers(handle)
            glfwPollEvents()
        }
    }
}

