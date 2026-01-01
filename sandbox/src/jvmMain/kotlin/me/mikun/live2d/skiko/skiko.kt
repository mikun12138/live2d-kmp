package me.mikun.live2d.skiko

import me.mikun.live2d.framework.model.Live2DMoc
import me.mikun.live2d.ex.rendering.Live2DColor
import me.mikun.live2d.ex.rendering.Live2DDrawableContext
import me.mikun.sandbox.Timer
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Path
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.jetbrains.skiko.SkikoRenderDelegate
import org.joml.Matrix4f
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

lateinit var skiaLayer: SkiaLayer

fun skiko(
    resDirMoc: String,
    mocName: String,
) {
    val moc = Live2DMoc(resDirMoc, "$mocName.model3.json")
    val model = moc.instantiateModel()
    val renderer = SkikoRenderer(model)

    Timer.update()
    skiaLayer = SkiaLayer()
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(
        skiaLayer,
        renderDelegate = object : SkikoRenderDelegate {
            override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
                canvas.clear(Color.BLACK)

                Timer.update()
                run {
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

                    run {
                        model.update(Timer.deltaF)
                        renderer.frame(
                            canvas,
                            width,
                            height,
                            modelMatrix.mul(canvasMatrix, Matrix4f()).get(FloatArray(16))
                        )
                    }
                }
            }
        }
    )

    SwingUtilities.invokeLater {
        val window = JFrame("Hello Skiko! ").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(1920, 1080)
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
    }

}

