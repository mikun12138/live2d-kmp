package me.mikun.live2d.skiko

import me.mikun.live2d.framework.model.ALive2DUserModel
import me.mikun.live2d.ex.model.Live2DUserModelImpl
import me.mikun.live2d.ex.rendering.ALive2DRenderer
import me.mikun.live2d.ex.rendering.Live2DColor
import me.mikun.live2d.ex.rendering.Live2DDrawableContext
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ClipMode
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.Matrix44
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.Shader
import org.jetbrains.skia.VertexMode

class SkikoRenderer(
    appModel: ALive2DUserModel,
) : ALive2DRenderer(
    appModel,
) {
    lateinit var mvp: FloatArray

    lateinit var canvas: Canvas
    var width: Int = -1
    var height: Int = -1

    class Texture {
        val shader: Shader

        constructor(bytes: ByteArray) {
            val image = Image.makeFromEncoded(bytes)
            shader = image.makeShader(
                FilterTileMode.CLAMP,
                FilterTileMode.CLAMP,
                SamplingMode.LINEAR
            )
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

    val drawableTextureArray: Array<Texture> = Array(appModel.model.drawableCount) {
        val drawableContext = drawableContextArray[it]
        Texture.create(
            drawableContext.textureIndex,
            appModel.textures[drawableContext.textureIndex]
        )
    }

    fun frame(
        canvas: Canvas,
        width: Int,
        height: Int,
        mvp: FloatArray,
    ) {
        this.canvas = canvas
        this.width = width
        this.height = height
        this.mvp = mvp
        doFrame()
    }

    override fun doRender() {
        canvas.save()
        run {
            canvas.translate(width / 2f, height / 2f)
            canvas.scale(width / 2f, -height / 2f)

            canvas.concat(
                Matrix44(
                    *mvp
                )
            )

            val sortedDrawableContextArray = drawableContextArray.sortedWith(
                compareBy { it.renderOrder }
            )

            sortedDrawableContextArray.forEach {
                if (!it.isVisible) return@forEach

                canvas.save()
                it.masks.takeIf { it.isNotEmpty() }?.forEach {
                    drawableContextArray[it].let {
                        val maskPath = createMaskPath(it.vertex)
                        canvas.clipPath(maskPath, ClipMode.INTERSECT, true)
                    }
                }

                canvas.drawVertices(
                    VertexMode.TRIANGLES,
                    positions = it.vertex.positionsArray,
                    texCoords = FloatArray(it.vertex.texCoordsArray.size) { i ->
                        if (i % 2 == 0) {
                            it.vertex.texCoordsArray[i] * 2048.0f
                        } else {
                            (1.0f - it.vertex.texCoordsArray[i]) * 2048.0f
                        }
                    },
                    indices = it.vertex.indicesArray,
                    blendMode = BlendMode.SRC_OVER,
                    paint = Paint().apply {
                        shader = RuntimeShaderBuilder(fragmentShader).apply {
                            child("texture", drawableTextureArray[it.index].shader)
                            uniform("u_baseColor", it.baseColor.array())
                            uniform("u_multiplyColor", it.multiplyColor.array())
                            uniform("u_screenColor", it.screenColor.array())
                        }.makeShader()
                    }
                )
                canvas.restore()
            }
        }
        canvas.restore()
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
fun Live2DColor.array(): FloatArray {
    return floatArrayOf(r, g, b, a)
}