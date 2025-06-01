package me.mikun.live2d

import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig
import com.live2d.sdk.cubism.framework.data.ModelJson
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.AAppModel
import com.live2d.sdk.cubism.framework.rendering.Live2DRenderer
import com.live2d.sdk.cubism.framework.rendering.create
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import kotlin.io.path.Path
import kotlin.io.path.readBytes

fun live2dMain() {
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
        CubismFramework.startUp(CubismFrameworkConfig())
        CubismFramework.reinit()


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

        val renderer = Live2DRenderer.create()
        val model = object : AAppModel() {
            override fun setupTextures(
                dir: String,
                modelJson: ModelJson,
            ) {
                modelJson.fileReferences.textures.forEach { texturePath ->
                    Path(dir, texturePath).readBytes().let { buffer ->
                        renderer.textures.add(
                            loadTexture(
                                MemoryUtil.memAlloc(buffer.size).put(buffer).flip()
                            )
                        )
                    }
                }
            }
        }
        model.init("Hiyori", "Hiyori" + ".model3.json")
        renderer.init(model.model, 1)

        while (!glfwWindowShouldClose(handle)) {
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
            renderer.mvpMatrix.setMatrix(matrix)
            renderer.drawModel(model.model)

            glfwSwapBuffers(handle)
            glfwPollEvents()
        }
    }
}

fun loadTexture(bytes: ByteBuffer): Int {
    return MemoryStack.stackPush().use { stack ->
        val width = stack.mallocInt(1)
        val height = stack.mallocInt(1)
        val channels = stack.mallocInt(1)
        STBImage.stbi_load_from_memory(
            bytes,
            width,
            height,
            channels,
            4
        ).let { buffer ->
            glGenTextures().apply {
                glBindTexture(
                    GL_TEXTURE_2D,
                    this
                )
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER,
                    GL_NEAREST
                )
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER,
                    GL_NEAREST
                )
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_BORDER
                )
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T,
                    GL_CLAMP_TO_BORDER
                )
                glTexParameterfv(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_BORDER_COLOR,
                    floatArrayOf(
                        0.0f,
                        0.0f,
                        0.0f,
                        0.0f
                    )
                )
                glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA,
                    width.get(),
                    height.get(),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    buffer
                )
                glGenerateMipmap(GL_TEXTURE_2D)
            }
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