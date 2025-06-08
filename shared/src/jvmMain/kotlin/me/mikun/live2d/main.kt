package me.mikun.live2d

import com.live2d.sdk.cubism.framework.Live2DFramework
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.AAppModel
import com.live2d.sdk.cubism.framework.rendering.Live2DRenderState
import com.live2d.sdk.cubism.framework.rendering.ALive2DRenderer
import com.live2d.sdk.cubism.framework.rendering.create
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46.*

fun live2dMain(
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

    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE)
//    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

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

        val model = AAppModel()
        model.init(resDirMoc, "$mocName.model3.json")

        val renderer = ALive2DRenderer.create(model.model, 1)

        while (!glfwWindowShouldClose(handle)) {
//            run {
            glClearColor(
                0.0f,
                0.0f,
                0.0f,
                1.0f
            )
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            glClearDepthf(1.0f)

            Timer.update()
            // キャッシュ変数の定義を避けるために、multiplyByMatrix()ではなく、multiply()を使用する。
            val matrix = CubismMatrix44.create().apply {
                loadIdentity()
                scale(
                    1080.0f / 1920.0f,
                    1.0f
                )
            }
            CubismMatrix44.multiply(
                model.modelMatrix!!.tr,
                matrix.tr,
                matrix.tr
            )

            model.update(Timer.deltaF)
            renderer.frame(matrix)

            glfwSwapBuffers(handle)
            glfwPollEvents()
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