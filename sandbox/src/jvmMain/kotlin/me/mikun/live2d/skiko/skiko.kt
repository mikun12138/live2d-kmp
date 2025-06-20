package me.mikun.live2d.skiko

import me.mikun.live2d.ex.model.Live2DUserModelImpl
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
    val model = Live2DUserModelImpl()
    model.init(resDirMoc, "$mocName.model3.json")
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

fun createMaskPath(vertex: Live2DDrawableContext.Vertex): Path {
    return Path().apply {
        for (i in vertex.indicesArray.indices step 3) {
            val i0 = vertex.indicesArray[i].toInt() * 2
            val i1 = vertex.indicesArray[i + 1].toInt() * 2
            val i2 = vertex.indicesArray[i + 2].toInt() * 2

            moveTo(vertex.positionsArray[i0], vertex.positionsArray[i0 + 1])
            lineTo(vertex.positionsArray[i1], vertex.positionsArray[i1 + 1])
            lineTo(vertex.positionsArray[i2], vertex.positionsArray[i2 + 1])
            closePath()
        }
    }
}

val fragmentShaderCode = """
uniform shader texture;
uniform half4 u_baseColor;
uniform half4 u_multiplyColor;
uniform half4 u_screenColor;

half4 main(float2 fragCoord) {
    half4 texColor = texture.eval(fragCoord);
    texColor.rgb *= u_multiplyColor.rgb;
    texColor.rgb = (texColor.rgb + u_screenColor.rgb * texColor.a) - (texColor.rgb * u_screenColor.rgb);
    return texColor * u_baseColor;
}
""".trimIndent()
val fragmentShader = RuntimeEffect.makeForShader(fragmentShaderCode)


fun Color.make(color: Live2DColor): Int {
    return Color.makeARGB(
        (255 * color.a).toInt(),
        (255 * color.r).toInt(),
        (255 * color.g).toInt(),
        (255 * color.b).toInt()
    )
}

fun Live2DColor.array(): FloatArray {
    return floatArrayOf(r, g, b, a)
}