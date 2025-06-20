package me.mikun.sandbox

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