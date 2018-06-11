package ch.hsr.ifs.gcs.util

import android.graphics.Color
import java.util.*

fun createRandomColorArgb() : Int {
    val random = Random()
    fun rand(from: Int, to: Int) : Int {
        return random.nextInt(to - from) + from
    }
    val color = Color.argb(255, rand(0, 255), rand(0, 255), rand(0, 255))
    return color
}