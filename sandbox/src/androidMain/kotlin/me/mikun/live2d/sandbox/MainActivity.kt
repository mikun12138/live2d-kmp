package me.mikun.live2d.sandbox

import android.content.Context
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glClearDepthf
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import me.mikun.live2d.render.renderer.Live2DRendererPreClip
import me.mikun.live2d.ex.model.Live2DUserModelImpl
import me.mikun.live2d.render.context.Live2DModelClipContext
import me.mikun.live2d.render.context.Live2DModelRenderContext
import me.mikun.live2d.render.renderer.Live2DRenderer
import java.io.File
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

private val model = Live2DUserModelImpl()

private val live2DModelRenderContext: Live2DModelRenderContext by lazy {
    Live2DModelRenderContext(model)
}
private val live2DModelClipContext: Live2DModelClipContext by lazy {
    Live2DModelClipContext(1, live2DModelRenderContext)
}

private val live2DRendererPreClip = Live2DRendererPreClip()
private val live2DRenderer = Live2DRenderer()

class MainActivity : ComponentActivity() {

    private var _glSurfaceView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        copyAssets(this, "")
        model.init("${filesDir.absolutePath}/Hiyori", "Hiyori.model3.json")

        _glSurfaceView = GLSurfaceView(this)
        _glSurfaceView!!.setEGLContextClientVersion(3)
        _glSurfaceView!!.setRenderer(GLSurfaceRenderer)
        _glSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        setContentView(_glSurfaceView)
    }
}

object GLSurfaceRenderer : GLSurfaceView.Renderer {
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

        model.update(Timer.deltaF)
        live2DModelRenderContext.update()
//        live2DRendererPreClip.frame(live2DModelRenderContext, live2DModelClipContext)
        live2DRenderer.frame(live2DModelRenderContext)
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

        glEnable(GL_BLEND)
        glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
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
                println("frame: $v")
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

