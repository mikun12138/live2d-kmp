package me.mikun.live2d.render

import java.nio.FloatBuffer
import java.nio.ShortBuffer

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