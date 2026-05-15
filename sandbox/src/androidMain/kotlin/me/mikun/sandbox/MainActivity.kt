package me.mikun.sandbox

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.EGL14
import android.opengl.GLES20.GL_ARRAY_BUFFER
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_CCW
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_CULL_FACE
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.GL_DYNAMIC_DRAW
import android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_NEAREST
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_SCISSOR_TEST
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.GL_STATIC_DRAW
import android.opengl.GLES20.GL_STENCIL_TEST
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_SHORT
import android.opengl.GLES20.glBindBuffer
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glBufferData
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glClearDepthf
import android.opengl.GLES20.glColorMask
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glDrawElements
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glFrontFace
import android.opengl.GLES20.glGenBuffers
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGenerateMipmap
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLES30.glBindVertexArray
import android.opengl.GLES30.glGenVertexArrays
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.os.Bundle
import androidx.activity.ComponentActivity
import me.mikun.live2d.framework.utils.math.CubismMatrix44
import me.mikun.live2d.ex.model.Live2DUserModelImpl
import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.Live2DDrawableContext
import me.mikun.live2d.framework.Live2DFramework
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


fun copyAssets(context: Context, path: String) {
    val assets = context.assets.list(path) ?: return
    if (assets.isEmpty()) {
        val target = File(context.filesDir, path)
        target.parentFile?.mkdirs()
        context.assets.open(path).use { input ->
            target.outputStream().use { input.copyTo(it) }
        }
    } else {
        for (asset in assets) {
            copyAssets(context, if (path.isEmpty()) asset else "$path/$asset")
        }
    }
}

val userModel = Live2DUserModelImpl()

class MainActivity : ComponentActivity() {

    private var _glSurfaceView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        copyAssets(this, "")
        userModel.init("${filesDir.absolutePath}/Mao", "Mao.model3.json")

        _glSurfaceView = GLSurfaceView(this)
        _glSurfaceView!!.setEGLContextClientVersion(3)
        _glSurfaceView!!.setRenderer(GLRenderer)
        _glSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        setContentView(_glSurfaceView)
    }
}

object GLRenderer : ALive2DRenderer.PreClip(
    userModel,
    1,
    pushViewportFun = Live2DRenderState::pushViewPort,
    pushFrameBufferFun = Live2DRenderState::pushFrameBuffer,
), GLSurfaceView.Renderer {
    override fun onDrawFrame(p0: GL10?) {
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
        val matrix = CubismMatrix44().apply {
            loadIdentity()
            scale(
                1080.0f / 1920.0f,
                1.0f
            )
        }
        CubismMatrix44.multiply(
            userModel.modelMatrix!!.tr,
            matrix.tr,
            matrix.tr
        )

        userModel.update(Timer.deltaF)

        this.mvp.set(matrix)
        doFrame()

    }

    override fun onSurfaceChanged(
        p0: GL10?,
        p1: Int,
        p2: Int,
    ) {
        glViewport(0, 0, p1, p2)
    }

    override fun onSurfaceCreated(
        p0: GL10?,
        p1: EGLConfig?,
    ) {
        val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        EGL14.eglSwapInterval(eglDisplay, 0)

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
    }

    var mvp: CubismMatrix44 = CubismMatrix44()

    override val offscreenSurfaces: Array<Live2DOffscreenSurface> by lazy {
        Array(1) {
            Live2DOffscreenSurface().apply {
                createOffscreenSurface(
                    512.0f, 512.0f
                )
            }
        }
    }

    override fun doUpdateData() {
        super.doUpdateData()
        drawableVertexArrayArray.forEachIndexed { index, vertexArray ->
            with(drawableContextArray[index].vertex) {
                vertexArray.positionsBuffer
                    .clear()
                vertexArray.positionsBuffer
                    .put(positionsArray)
                    .position(0)

                vertexArray.texCoordsBuffer
                    .clear()
                vertexArray.texCoordsBuffer
                    .put(texCoordsArray)
                    .position(0)

                vertexArray.indicesBuffer
                    ?.clear()
                vertexArray.indicesBuffer
                    ?.put(indicesArray)
                    ?.position(0)
            }
        }
    }

    override fun setupMaskDraw(
        drawableContext: Live2DDrawableContext,
        clipContext: ClipContext,
    ) {
        Live2DShader.setupMask(
            this,
            drawableContext,
            clipContext
        )

        drawMesh(
            drawableContext
        )
    }

    override fun simpleDraw(drawableContext: Live2DDrawableContext) {
        Live2DShader.drawSimple(
            this,
            drawableContext
        )

        drawMesh(
            drawableContext
        )
    }

    override fun maskDraw(drawableContext: Live2DDrawableContext) {
        Live2DShader.drawMasked(
            this,
            drawableContext
        )

        drawMesh(
            drawableContext
        )
    }

    private fun drawMesh(
        drawableContext: Live2DDrawableContext,
    ) {
        glDisable(GL_SCISSOR_TEST)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)
        glColorMask(
            true,
            true,
            true,
            true
        )

        if (drawableContext.isCulling) {
            glEnable(GL_CULL_FACE)
        } else {
            glDisable(GL_CULL_FACE)
        }
        glFrontFace(GL_CCW)

        if (drawableContext.vertex.indicesArray.isEmpty()) {
            // TODO::
            return
        }
        glDrawElements(
            GL_TRIANGLES,
            drawableContext.vertex.indicesArray.size,
            GL_UNSIGNED_SHORT,
            0
        )
    }

    class Texture {
        val id: Int
        val isPremultipliedAlpha: Boolean = false

        private constructor(bytes: ByteArray) {
            val textureIds = IntArray(1)
            glGenTextures(1, textureIds, 0)
            id = textureIds[0]

            if (id == 0) {
                throw RuntimeException("Error generating texture name.")
            }

            val bitmap = BitmapFactory.decodeByteArray(
                bytes, 0, bytes.size,
                BitmapFactory.Options().apply {
                    inPremultiplied = false
                }
            )

            glBindTexture(
                GL_TEXTURE_2D,
                id
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
            GLUtils.texImage2D(
                GL_TEXTURE_2D,
                0,
                bitmap,
                0
            )
            glGenerateMipmap(GL_TEXTURE_2D)
        }

        companion object {
            val textures: MutableMap<Int, Texture> = mutableMapOf()
            fun create(textureIndex: Int, bytes: ByteArray): Texture {
                return textures.getOrPut(textureIndex) {
                    Texture(bytes)
                }
            }
        }
    }

    val drawableTextureArray: Array<Texture> by lazy {
        Array(userModel.model.drawableCount) {
            val drawableContext = drawableContextArray[it]
            Texture.create(
                drawableContext.textureIndex,
                userModel.textures[drawableContext.textureIndex]
            )
        }
    }

    class VertexArray {
        var vao: Int = -1
        val vaos = IntArray(1)

        var vboPosition: Int = -1
        lateinit var positionsBuffer: FloatBuffer
        var vboTexCoord: Int = -1
        lateinit var texCoordsBuffer: FloatBuffer
        val vbos = IntArray(2)

        var ebo: Int = -1
        var indicesBuffer: ShortBuffer? = null
        val ebos = IntArray(1)
    }

    val drawableVertexArrayArray: Array<VertexArray> by lazy {
        Array(userModel.model.drawableCount) {
            val drawableContext = drawableContextArray[it]
            VertexArray().apply {
                glGenVertexArrays(1, vaos, 0)

                vao = vaos[0]

                glBindVertexArray(vao)
                glGenBuffers(2, vbos, 0)
                drawableContext.vertex.apply {
                    vboPosition = vbos[0]

                    val dataSize = (positionsArray.size * 4).toLong()

                    glBindBuffer(GL_ARRAY_BUFFER, vboPosition)
                    positionsBuffer = ByteBuffer.allocateDirect(dataSize.toInt())
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
                    positionsBuffer.put(0, positionsArray)
                    glBufferData(
                        GL_ARRAY_BUFFER,
                        dataSize.toInt(),
                        positionsBuffer,
                        GL_DYNAMIC_DRAW
                    )

                    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
                    glEnableVertexAttribArray(0)
                }

                drawableContext.vertex.apply {
                    vboTexCoord = vbos[1]
                    val texSize = (texCoordsArray.size * 4).toLong()

                    glBindBuffer(GL_ARRAY_BUFFER, vboTexCoord)
                    texCoordsBuffer = ByteBuffer.allocateDirect(texSize.toInt())
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer()
                    texCoordsBuffer.put(0, texCoordsArray)
                    glBufferData(
                        GL_ARRAY_BUFFER,
                        texSize.toInt(),
                        texCoordsBuffer,
                        GL_DYNAMIC_DRAW
                    )

                    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
                    glEnableVertexAttribArray(1)
                }

                drawableContext.vertex.apply {
                    glGenBuffers(1, ebos, 0)
                    ebo = ebos[0]
                    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)

                    if (indicesArray.isNotEmpty()) {
                        val dataSize = (indicesArray.size * 2) // Short

                        indicesBuffer = ByteBuffer.allocateDirect(dataSize.toInt())
                            .order(ByteOrder.nativeOrder())
                            .asShortBuffer()
                        indicesBuffer!!.put(0, indicesArray)

                        glBufferData(
                            GL_ELEMENT_ARRAY_BUFFER,
                            dataSize,
                            indicesBuffer,
                            GL_STATIC_DRAW
                        )
                    }
                }

                glBindVertexArray(0)
            }
        }
    }
}

object Timer {
    val start = System.nanoTime()

    object Fps {
        var lastC = start
        var v = 0
        var now = start
    }

    fun update() {
        current = System.nanoTime()
        delta = current - last
        last = current

        with(Fps) {
            v++
            if (now - lastC > 1000000000) {
                lastC += 1000000000
                println(v)
                v = 0
            }
            now = System.nanoTime()
        }
    }

    var current: Long = 0
    var last: Long = 0
    var delta: Long = 0

    val deltaF: Float
        get() = delta / 1000000000.0f

}
