package ch.hsr.ifs.gcs.util

import android.graphics.Color
import java.util.*

fun createRandomColorArgb() =
        with(Random()) {
            fun rand(from: Int, to: Int): Int {
                return nextInt(to - from) + from
            }
            Color.argb(255, rand(0, 255), rand(0, 255), rand(0, 255))
        }
